package ru.avem.pult.tests

import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.communication.model.devices.LatrStuckException
import ru.avem.pult.controllers.TestController
import ru.avem.pult.database.entities.Protocol
import ru.avem.pult.utils.LogTag
import ru.avem.pult.utils.TestStateColors
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.errorNotification
import tornadofx.controlsfx.warningNotification
import tornadofx.runLater
import tornadofx.seconds
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

abstract class Test(
    val model: MainViewModel,
    val view: TestView,
    val controller: TestController
) {
    enum class CauseDescriptor(val description: String) {
        CONTROL_UNIT_NOT_RESPOND("БСУ не отвечает"),
        AMPERAGE_PROTECTION_TRIGG("Изделие было пробито. Сработала токовая защита"),
        KA1_TRIGGERED("Сработала токовая защита до АРН"),
        KA2_TRIGGERED("Сработала токовая защита после АРН"),
        AMPERAGE_OVERLOAD("Ток превысил заданный"),
        LATR_CONTROLLER_ERROR("Ошибка контроллера АРН"),
        CANCELED("Испытание отменено оператором"),
        DEVICES_NOT_RESPONDING("Устройства не отвечают"),
        DOOR_WAS_OPENED("Была открыта дверь зоны"),
        SECTION_DOOR_WAS_OPENED("Была открыта дверь секции"),
        LATR_STUCK("АРН застрял. Обратитесь к производителю."),
        MANUAL_MODE_TIMEOUT("Превышено допустимое время испытаний в ручном режиме"),
        HI_POWER_SWITCH_LOCKED("Рубильник <Видимый разрыв замкнут>"),
        HI_POWER_SWITCH_TIMEOUT("Закончилось время ожидания реакции оператора на перевод рубильника <Видимый разрыв> в рабочее положение. Возможно рубильник неисправен."),
        BUTTON_START_NOT_PRESSED("Кнопка СТАРТ не была нажата в течение 15 секунд. Возможно кнопка СТАРТ неисправна."),
        SHORTLOCKER_NOT_WORKING_20KV("Короткозамыкатель 20кВ не сработал"),
        TEST_CONTACTOR_NOT_WORKING_200V("Контактор испытания $TYPE_1_VOLTAGE В не замкнулся"),
        TEST_CONTACTOR_NOT_WORKING_20KV("Контактор испытания $TYPE_2_VOLTAGE В не замкнулся"),
        VIU_CONTACTOR_NOT_WORKING("Контактор КМ3 не замкнулся"),
        IMPULSE_CONTACTOR_NOT_WORKING("Контактор КМ2 не замкнулся"),
        GENERAL_AMMETER_RELAY("Сработало реле амперметра общего тока утечки"),
        OPERATOR_IS_IDLE("Индикатор не зажёгся или оператор не зафиксировал зажигание"),
        BATH_DOOR_NOT_CLOSED("Открыта дверь ванны или не сработал удерживающий замок"),
        ONE_OR_MORE_CHANNELS_TRIGGERED("Одно или несколько изделий были пробиты"),
        ONE_OR_MORE_CHANNELS_BAD("Одно или несколько изделий превысили уставку тока"),
        ALL_CHANNELS_TRIGGERED("Все изделия были пробиты"),
        SUCCESS("Испытание успешно завершено"),
        LIGHT_FIXED(""),
        EMPTY("")
    }

    var listOfValuesU = mutableListOf<String>()
    var listOfValuesI = mutableListOf<String>()

    var cause: CauseDescriptor = CauseDescriptor.EMPTY
        set(value) {
            if (field == CauseDescriptor.EMPTY || value == CauseDescriptor.EMPTY) {
                field = value
                if (field != CauseDescriptor.EMPTY) {
                    isTestRunning = false
                }
            }
        }

    var isTestRunning = false
        set(value) {
            field = value
            if (!field) {
                view.setTestStatusColor(TestStateColors.WAIT)
                finalizeTest()
            }
        }

    open fun start() {
        view.logBuffer = null
        cause = CauseDescriptor.EMPTY
        controller.clearTableResults()
        view.setTestStatusColor(TestStateColors.GO)
        view.setExperimentProgress(-1)
        controller.tableValues.forEach {
            it.testTime.value = ""
        }
    }

    private fun finalizeTest() {
        thread {
            view.buttonStartTimer.isDisable = true
            controller.deinitPollingModules()
            try {
                view.setTestStatusColor(TestStateColors.WAIT)
                view.setExperimentProgress(-1)
                controller.latrDevice.reset()
            } catch (ignored: LatrStuckException) {
                cause = CauseDescriptor.LATR_STUCK
            }
            when {
                model.testObject.value.objectTransformer == TYPE_1_VOLTAGE.toString() -> {
                    view.appendMessageToLog(LogTag.MESSAGE, "Разборка схемы испытания $TYPE_1_VOLTAGE В")
                    controller.disassembleType1Scheme()
                }
                model.testObject.value.objectTransformer == TYPE_2_VOLTAGE.toString() -> {
                    view.appendMessageToLog(LogTag.MESSAGE, "Разборка схемы испытания $TYPE_2_VOLTAGE В")
                    controller.disassembleType2Scheme()
                }
            }
            controller.deinitWritingModules()
            controller.voltmeterDevice.writeRuntimeKTR(1f)
            when (cause) {
                CauseDescriptor.KA1_TRIGGERED,
                CauseDescriptor.KA2_TRIGGERED,
                CauseDescriptor.AMPERAGE_OVERLOAD,
                CauseDescriptor.CONTROL_UNIT_NOT_RESPOND,
                CauseDescriptor.AMPERAGE_PROTECTION_TRIGG,
                CauseDescriptor.LATR_CONTROLLER_ERROR,
                CauseDescriptor.DEVICES_NOT_RESPONDING,
                CauseDescriptor.DOOR_WAS_OPENED,
                CauseDescriptor.SECTION_DOOR_WAS_OPENED,
                CauseDescriptor.MANUAL_MODE_TIMEOUT,
                CauseDescriptor.LATR_STUCK,
                CauseDescriptor.HI_POWER_SWITCH_TIMEOUT,
                CauseDescriptor.HI_POWER_SWITCH_LOCKED,
                CauseDescriptor.BUTTON_START_NOT_PRESSED,
                CauseDescriptor.SHORTLOCKER_NOT_WORKING_20KV,
                CauseDescriptor.VIU_CONTACTOR_NOT_WORKING,
                CauseDescriptor.IMPULSE_CONTACTOR_NOT_WORKING,
                CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_200V,
                CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_20KV,
                CauseDescriptor.BATH_DOOR_NOT_CLOSED,
                CauseDescriptor.GENERAL_AMMETER_RELAY,
                CauseDescriptor.OPERATOR_IS_IDLE -> {
                    runLater {
                        errorNotification(
                            title = "Ошибка",
                            text = "Испытание не завершено",
                            position = Pos.CENTER,
                            hideAfter = 3.seconds
                        )
                    }
                    view.appendMessageToLog(
                        LogTag.ERROR,
                        "Испытание не завершено: ${cause.description}${getNotRespondingDevicesMessage()}"
                    )
                    view.setTestStatusColor(TestStateColors.ERROR)
                    controller.tableValues[0].result.value = "Провал"
                    controller.owenPrDevice.onProtectionsLamp()
                }
                CauseDescriptor.CANCELED -> {
                    runLater {
                        warningNotification(
                            title = "Отменено",
                            text = "Испытание отменено",
                            position = Pos.CENTER,
                            hideAfter = 3.seconds
                        )
                    }
                    view.appendMessageToLog(LogTag.ERROR, "Испытание не завершено: ${cause.description}")
                    view.setTestStatusColor(TestStateColors.GO)
                    controller.tableValues[0].result.value = "Отменено"
                }
                CauseDescriptor.ONE_OR_MORE_CHANNELS_TRIGGERED, CauseDescriptor.ALL_CHANNELS_TRIGGERED,
                CauseDescriptor.ONE_OR_MORE_CHANNELS_BAD -> {
                    controller.tableValues[0].result.value = "Не годен"
                    runLater {
                        warningNotification(
                            title = "Предупреждение",
                            text = "Испытание завершено с предупреждениями",
                            position = Pos.CENTER,
                            hideAfter = 3.seconds
                        )
                    }
                    view.appendMessageToLog(
                        LogTag.ERROR,
                        "Испытание завершено с предупреждениями: ${cause.description}"
                    )
                    view.setTestStatusColor(TestStateColors.ERROR)
                }
                CauseDescriptor.SUCCESS, CauseDescriptor.EMPTY, CauseDescriptor.LIGHT_FIXED -> {
                    controller.tableValues[0].result.value = "Успех"
                    runLater {
                        confirmNotification(
                            title = "Успешно",
                            text = "Испытание успешно завершено",
                            position = Pos.CENTER,
                            hideAfter = 3.seconds
                        )
                    }
                    view.appendMessageToLog(LogTag.MESSAGE, "Испытание успешно завершено")
                    view.setTestStatusColor(TestStateColors.GO)
                }
            }

            controller.isTimerStart = false

            if (listOfValuesU.isNotEmpty()) {
                saveProtocolToDB(cause)
            }

            view.setExperimentProgress(0)
            switchExperimentButtonsState()
        }
    }

    private fun getNotRespondingDevicesMessage() =
        if (cause == CauseDescriptor.DEVICES_NOT_RESPONDING) {
            getNotRespondingMessageFromTest()
        } else {
            ""
        }

    fun switchExperimentButtonsState() {
        runLater {
            if (isTestRunning) {
                view.buttonTestStart.isDisable = true
                view.buttonTestStop.isDisable = false
                view.buttonBack.isDisable = true
                view.buttonStartTimer.isDisable = false
            } else {
                view.buttonTestStart.isDisable = false
                view.buttonTestStop.isDisable = true
                view.buttonBack.isDisable = false
                view.buttonStartTimer.isDisable = true
            }
        }
    }

    private fun saveProtocolToDB(cause: CauseDescriptor) {
        val dateFormatter = SimpleDateFormat("dd.MM.y")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")

        val unixTime = System.currentTimeMillis()

        transaction {
            Protocol.new {
                date = dateFormatter.format(unixTime)
                time = timeFormatter.format(unixTime)
                factoryNumber = model.factoryNumber.value
                objectName = model.testObject.value.objectName
                specifiedU = controller.tableValues[0].specifiedVoltage.value
                specifiedI = controller.tableValues[0].specifiedAmperage.value
                when (model.test.value) {
                    TEST_1 -> {
                        objectU0 = controller.tableValues[0].measuredVoltage.value
                        objectI0 = controller.tableValues[0].measuredAmperage.value
                    }
                    TEST_2 -> {
                        objectU0 = controller.impulseTableValues[0].measuredVoltage.value
                        objectI0 = controller.impulseTableValues[0].measuredAmperage.value
                    }
                }
                experimentTime0 = controller.tableValues[0].testTime.value

                result0 = controller.tableValues[0].result.value
                result = when (cause) {
                    CauseDescriptor.SUCCESS, CauseDescriptor.EMPTY, CauseDescriptor.LIGHT_FIXED -> {
                        "Успех"
                    }
                    CauseDescriptor.CANCELED -> {
                        "Отменено"
                    }
                    else -> {
                        "Провал"
                    }
                }
                tester = model.user.value?.fullName ?: ""

                graphU = listOfValuesU.toString()
                graphI = listOfValuesI.toString()
            }
        }
        view.appendMessageToLog(LogTag.MESSAGE, "Протокол сохранён")
    }

    abstract fun getNotRespondingMessageFromTest(): String
}
