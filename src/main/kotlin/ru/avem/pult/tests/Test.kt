package ru.avem.pult.tests

import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.communication.model.devices.LatrStuckException
import ru.avem.pult.controllers.TestController
import ru.avem.pult.database.entities.Protocol
import ru.avem.pult.utils.CallbackTimer
import ru.avem.pult.utils.LogTag
import ru.avem.pult.utils.TestStateColors
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_4
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_3_VOLTAGE
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.errorNotification
import tornadofx.controlsfx.warningNotification
import java.lang.Thread.sleep
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
        AMPERAGE_OVERLOAD("Ток превысил заданный"),
        LATR_CONTROLLER_ERROR("Ошибка контроллера АРН"),
        CANCELED("Испытание отменено оператором"),
        DEVICES_NOT_RESPONDING("Устройства не отвечают"),
        DOOR_WAS_OPENED("Была открыта дверь зоны"),
        LATR_STUCK("АРН застрял. Обратитесь к производителю."),
        MANUAL_MODE_TIMEOUT("Превышено допустимое время испытаний в ручном режиме"),
        HI_POWER_SWITCH_LOCKED("Рубильник <Видимый разрыв замкнут>"),
        HI_POWER_SWITCH_TIMEOUT("Закончилось время ожидания реакции оператора на перевод рубильника <Видимый разрыв> в рабочее положение. Возможно рубильник неисправен."),
        BUTTON_START_NOT_PRESSED("Кнопка СТАРТ не была нажата в течение 15 секунд. Возможно кнопка СТАРТ неисправна."),
        SHORTLOCKER_NOT_WORKING_20KV("Короткозамыкатель 20кВ не сработал"),
        TEST_CONTACTOR_NOT_WORKING_200V("Контактор испытания $TYPE_1_VOLTAGE В не замкнулся"),
        TEST_CONTACTOR_NOT_WORKING_20KV("Контактор испытания $TYPE_2_VOLTAGE В не замкнулся"),
        TEST_CONTACTOR_NOT_WORKING_50KV("Контактор испытания $TYPE_3_VOLTAGE В не замкнулся"),
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
        model.selectedConnectionPoints.values.forEach {
            it.isNeedToUpdate = true
        }
        controller.tableValues.forEach {
            it.testTime.value = ""
        }
    }

    private fun finalizeTest() {
        thread {
            controller.deinitPollingModules()
            try {
                view.setTestStatusColor(TestStateColors.WAIT)
                view.setExperimentProgress(-1)
                controller.latrDevice.reset()
            } catch (ignored: LatrStuckException) {
                cause = CauseDescriptor.LATR_STUCK
            }
            controller.owenPrDD3.offBathChannel1()
            controller.owenPrDD3.offBathChannel2()
            controller.owenPrDD3.offBathChannel3()
            controller.owenPrDD3.offBathChannel4()
            controller.owenPrDD2.onStepDownTransformer()
            when {
                model.testObject.value.objectTransformer == TYPE_1_VOLTAGE.toString() -> {
                    view.appendMessageToLog(LogTag.MESSAGE, "Разборка схемы испытания $TYPE_1_VOLTAGE В")
                    controller.disassembleType1Scheme()
                }
                model.testObject.value.objectTransformer == TYPE_2_VOLTAGE.toString() -> {
                    view.appendMessageToLog(LogTag.MESSAGE, "Разборка схемы испытания $TYPE_2_VOLTAGE В")
                    controller.disassembleType2Scheme()
                }
                model.testObject.value.objectTransformer == TYPE_3_VOLTAGE.toString() -> {
                    view.appendMessageToLog(LogTag.MESSAGE, "Разборка схемы испытания $TYPE_3_VOLTAGE В")
                    controller.disassembleType3Scheme()
                }
            }
            controller.deinitWritingModules()
            controller.voltmeterDevice.writeRuntimeKTR(1f)
            controller.owenPrDD2.turnOffLampLess1000()
            controller.owenPrDD2.turnOffLampMore1000()
            controller.owenPrDD2.offLightSign()
            controller.owenPrDD2.offTimer()
            when (cause) {
                CauseDescriptor.AMPERAGE_OVERLOAD,
                CauseDescriptor.CONTROL_UNIT_NOT_RESPOND,
                CauseDescriptor.AMPERAGE_PROTECTION_TRIGG,
                CauseDescriptor.LATR_CONTROLLER_ERROR,
                CauseDescriptor.DEVICES_NOT_RESPONDING,
                CauseDescriptor.DOOR_WAS_OPENED,
                CauseDescriptor.MANUAL_MODE_TIMEOUT,
                CauseDescriptor.LATR_STUCK,
                CauseDescriptor.HI_POWER_SWITCH_TIMEOUT,
                CauseDescriptor.HI_POWER_SWITCH_LOCKED,
                CauseDescriptor.BUTTON_START_NOT_PRESSED,
                CauseDescriptor.SHORTLOCKER_NOT_WORKING_20KV,
                CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_200V,
                CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_20KV,
                CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_50KV,
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
                    if (model.module.value == MODULE_2) {
                        model.selectedConnectionPoints[CONNECTION_1]?.let {
                            controller.tableValues[0].result.value = "Провал"
                        }
                        model.selectedConnectionPoints[CONNECTION_2]?.let {
                            controller.tableValues[1].result.value = "Провал"
                        }
                        model.selectedConnectionPoints[CONNECTION_3]?.let {
                            controller.tableValues[2].result.value = "Провал"
                        }
                        model.selectedConnectionPoints[CONNECTION_4]?.let {
                            controller.tableValues[3].result.value = "Провал"
                        }
                    } else {
                        controller.tableValues[0].result.value = "Провал"
                    }
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
                    if (model.module.value == MODULE_2) {
                        model.selectedConnectionPoints[CONNECTION_1]?.let {
                            controller.tableValues[0].result.value = "Отменено"
                        }
                        model.selectedConnectionPoints[CONNECTION_2]?.let {
                            controller.tableValues[1].result.value = "Отменено"
                        }
                        model.selectedConnectionPoints[CONNECTION_3]?.let {
                            controller.tableValues[2].result.value = "Отменено"
                        }
                        model.selectedConnectionPoints[CONNECTION_4]?.let {
                            controller.tableValues[3].result.value = "Отменено"
                        }
                    } else {
                        controller.tableValues[0].result.value = "Отменено"
                    }
                }
                CauseDescriptor.ONE_OR_MORE_CHANNELS_TRIGGERED, CauseDescriptor.ALL_CHANNELS_TRIGGERED,
                CauseDescriptor.ONE_OR_MORE_CHANNELS_BAD -> {
                    if (model.module.value == MODULE_2) {
                        model.selectedConnectionPoints[CONNECTION_1]?.let {
                            if (!it.isNeedToUpdate) {
                                controller.tableValues[0].result.value = "Не годен"
                            }
                        }
                        model.selectedConnectionPoints[CONNECTION_2]?.let {
                            if (!it.isNeedToUpdate) {
                                controller.tableValues[1].result.value = "Не годен"
                            }
                        }
                        model.selectedConnectionPoints[CONNECTION_3]?.let {
                            if (!it.isNeedToUpdate) {
                                controller.tableValues[2].result.value = "Не годен"
                            }
                        }
                        model.selectedConnectionPoints[CONNECTION_4]?.let {
                            if (!it.isNeedToUpdate) {
                                controller.tableValues[3].result.value = "Не годен"
                            }
                        }
                    } else {
                        controller.tableValues[0].result.value = "Не годен"
                    }
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
                    if (model.module.value == MODULE_2) {
                        model.selectedConnectionPoints[CONNECTION_1]?.let {
                            controller.tableValues[0].result.value = "Успех"
                        }
                        model.selectedConnectionPoints[CONNECTION_2]?.let {
                            controller.tableValues[1].result.value = "Успех"
                        }
                        model.selectedConnectionPoints[CONNECTION_3]?.let {
                            controller.tableValues[2].result.value = "Успех"
                        }
                        model.selectedConnectionPoints[CONNECTION_4]?.let {
                            controller.tableValues[3].result.value = "Успех"
                        }
                    } else {
                        controller.tableValues[0].result.value = "Успех"
                    }
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

            saveProtocolToDB(cause)
            view.setExperimentProgress(0)
            runLater {
                view.buttonTestStart.isDisable = false
                view.buttonTestStop.isDisable = false
            }
            switchExperimentButtonsState()
            model.selectedConnectionPoints.values.forEach {
                it.isNeedToUpdate = false
            }
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
                view.buttonBack.disableProperty().set(true)
                view.buttonFixLighting.isDisable = false
            } else {
                view.buttonBack.disableProperty().set(false)
                view.buttonFixLighting.isDisable = true
            }
        }
    }

    private fun saveProtocolToDB(cause: CauseDescriptor) {
        val dateFormatter = SimpleDateFormat("dd.MM.y")
        val timeFormatter = SimpleDateFormat("HH:mm:ss")

        val unixTime = System.currentTimeMillis()

        when {
            model.testObject.value.objectModule == MODULE_1 || model.testObject.value.objectModule == MODULE_3 -> {
                transaction {
                    Protocol.new {
                        date = dateFormatter.format(unixTime)
                        time = timeFormatter.format(unixTime)
                        factoryNumber = model.factoryNumber.value
                        objectName = model.testObject.value.objectName
                        specifiedU = controller.tableValues[0].specifiedVoltage.value
                        specifiedI = controller.tableValues[0].specifiedAmperage.value
                        objectU0 = controller.tableValues[0].measuredVoltage.value
                        objectI0 = controller.tableValues[0].measuredAmperage.value
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
                        tester = model.user.value.fullName
                    }
                }
            }
            model.testObject.value.objectModule == MODULE_2 -> {
                transaction {
                    Protocol.new {
                        date = dateFormatter.format(unixTime)
                        time = timeFormatter.format(unixTime)
                        factoryNumber = model.factoryNumber.value
                        objectName = model.testObject.value.objectName
                        model.selectedConnectionPoints[CONNECTION_1]?.let {
                            specifiedU = controller.tableValues[0].specifiedVoltage.value
                            specifiedI = controller.tableValues[0].specifiedAmperage.value
                            objectU0 = controller.tableValues[0].measuredVoltage.value
                            objectI0 = controller.tableValues[0].measuredAmperage.value
                            experimentTime0 = controller.tableValues[0].testTime.value
                            result0 = controller.tableValues[0].result.value
                        }
                        model.selectedConnectionPoints[CONNECTION_2]?.let {
                            specifiedU = controller.tableValues[1].specifiedVoltage.value
                            specifiedI = controller.tableValues[1].specifiedAmperage.value
                            objectU1 = controller.tableValues[1].measuredVoltage.value
                            objectI1 = controller.tableValues[1].measuredAmperage.value
                            experimentTime1 = controller.tableValues[1].testTime.value
                            result1 = controller.tableValues[1].result.value
                        }
                        model.selectedConnectionPoints[CONNECTION_3]?.let {
                            specifiedU = controller.tableValues[2].specifiedVoltage.value
                            specifiedI = controller.tableValues[2].specifiedAmperage.value
                            objectU2 = controller.tableValues[2].measuredVoltage.value
                            objectI2 = controller.tableValues[2].measuredAmperage.value
                            experimentTime2 = controller.tableValues[2].testTime.value
                            result2 = controller.tableValues[2].result.value
                        }
                        model.selectedConnectionPoints[CONNECTION_4]?.let {
                            specifiedU = controller.tableValues[3].specifiedVoltage.value
                            specifiedI = controller.tableValues[3].specifiedAmperage.value
                            objectU3 = controller.tableValues[3].measuredVoltage.value
                            objectI3 = controller.tableValues[3].measuredAmperage.value
                            experimentTime3 = controller.tableValues[3].testTime.value
                            result3 = controller.tableValues[3].result.value
                        }
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
                        tester = model.user.value.fullName
                    }
                }
            }
        }
        view.appendMessageToLog(LogTag.MESSAGE, "Протокол сохранён")
    }

    fun waitForUserStart() {
        var conf: Alert? = null
        runLater {
            conf = Alert(Alert.AlertType.WARNING).apply {
                title = "Запуск"
                headerText = "Переведите рубильник <Видимый разрыв> в рабочее положение или нажмите СТОП для отмены"
                contentText =
                    "Если реакции от оператора не последует в течение 15 секунд, то испытание автоматически отменится"
                dialogPane.style {
                    fontSize = 16.px
                }
                dialogPane.lookupButton(ButtonType.OK).isVisible = false
                show()
            }
        }

        val hiSwitchTimer = CallbackTimer(tickPeriod = 0.1.seconds, tickTimes = 150, onStartJob = {
            view.appendMessageToLog(
                LogTag.MESSAGE,
                "Переведите рубильник <Видимый разрыв> в рабочее положение или нажмите СТОП для отмены"
            )
        }, tickJob = {
            if (!isTestRunning) {
                runLater {
                    conf!!.close()
                }
                it.stop()
            }
            if (controller.owenPrDD2.isHiSwitchTurned()) {
                runLater {
                    conf!!.close()
                }
                it.stop()
            }
            if (controller.owenPrDD2.isStopPressed()) {
                runLater {
                    conf!!.close()
                }
                cause = CauseDescriptor.CANCELED
                it.stop()
            }
        }, onFinishJob = {
            runLater {
                conf!!.close()
            }
            cause = CauseDescriptor.HI_POWER_SWITCH_TIMEOUT
        })

        while (hiSwitchTimer.isRunning) {
            sleep(500)
        }

        if (isTestRunning) {
            controller.owenPrDD2.resetTriggers()
            controller.owenPrDD2.onButtonPostPower()
            view.appendMessageToLog(
                LogTag.MESSAGE,
                "Нажмите кнопку ПУСК на кнопочном посте для старта испытания, или СТОП для отмены"
            )

            runLater {
                conf = Alert(Alert.AlertType.WARNING).apply {
                    title = "Запуск"
                    headerText =
                        "Нажмите кнопку ПУСК на кнопочном посте для старта испытания, или СТОП для отмены"
                    contentText =
                        "Если реакции от оператора не последует в течение 15 секунд, то испытание автоматически отменится"
                    dialogPane.style {
                        fontSize = 16.px
                    }
                    dialogPane.lookupButton(ButtonType.OK).isVisible = false
                    show()
                }
            }
            val startButtonTimer = CallbackTimer(tickPeriod = 0.1.seconds, tickTimes = 150, tickJob = {
                if (!isTestRunning) {
                    runLater {
                        conf!!.close()
                    }
                    it.stop()
                }
                if (controller.owenPrDD2.isLatrContactorSwitched()) {
                    runLater {
                        conf!!.close()
                    }
                    it.stop()
                }
                if (controller.owenPrDD2.isStopPressed()) {
                    runLater {
                        conf!!.close()
                    }
                    cause = CauseDescriptor.CANCELED
                    it.stop()
                }
            }, onFinishJob = {
                runLater {
                    conf!!.close()
                }
                cause = CauseDescriptor.BUTTON_START_NOT_PRESSED
            })

            while (startButtonTimer.isRunning) {
                sleep(500)
            }
        }
    }

    abstract fun getNotRespondingMessageFromTest(): String
}
