package ru.avem.pult.controllers

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.communication.model.CommunicationModel.DeviceID
import ru.avem.pult.communication.model.CommunicationModel.getDeviceById
import ru.avem.pult.communication.model.devices.LatrStuckException
import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Controller
import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Controller
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.pult.communication.model.devices.avem.latr.AvemLatrController
import ru.avem.pult.communication.model.devices.avem.latr.AvemLatrModel
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrController
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.pult.communication.utils.autoformat
import ru.avem.pult.entities.LatrControllerConfiguration
import ru.avem.pult.entities.TableValues
import ru.avem.pult.tests.GeneralTest
import ru.avem.pult.tests.Test
import ru.avem.pult.tests.Test1
import ru.avem.pult.tests.Test2
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_1_FRAGMENT
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_2_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsFragmentModel
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.Controller
import tornadofx.clear
import tornadofx.observableList
import kotlin.concurrent.thread

class TestController : Controller() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
        const val MANUAL_TICK_COUNT = 360
    }

    var isLightingFixed: Boolean = false
        set(value) {
            field = value
            if (value) {
                currentTest.cause = Test.CauseDescriptor.LIGHT_FIXED
            }
        }
    private lateinit var fillTableVoltage: (Number) -> Unit

    val owenPrDevice = getDeviceById(DeviceID.DD1) as OwenPrController
    val latrDevice = getDeviceById(DeviceID.GV240) as AvemLatrController
    val voltmeterDevice = getDeviceById(DeviceID.PV21) as Avem4Controller
    val ammeterDevice = getDeviceById(DeviceID.PA11) as Avem7Controller

    val deviceControllers = listOf(
        owenPrDevice,
        latrDevice,
        voltmeterDevice,
        ammeterDevice,
    )

    private val view: TestView by inject()
    private val model: MainViewModel by inject()

    var tableValues = observableList(
        TableValues(
            connection = SimpleStringProperty("Объект 1"),
            specifiedVoltage = SimpleStringProperty(),
            ktr = SimpleStringProperty(),
            measuredVoltage = SimpleStringProperty(),
            specifiedAmperage = SimpleStringProperty(),
            measuredAmperage = SimpleStringProperty(),
            testTime = SimpleStringProperty(),
            result = SimpleStringProperty()
        ),
        TableValues(
            connection = SimpleStringProperty("Объект 2"),
            specifiedVoltage = SimpleStringProperty(),
            ktr = SimpleStringProperty(),
            measuredVoltage = SimpleStringProperty(),
            specifiedAmperage = SimpleStringProperty(),
            measuredAmperage = SimpleStringProperty(),
            testTime = SimpleStringProperty(),
            result = SimpleStringProperty()
        ),
        TableValues(
            connection = SimpleStringProperty("Объект 3"),
            specifiedVoltage = SimpleStringProperty(),
            ktr = SimpleStringProperty(),
            measuredVoltage = SimpleStringProperty(),
            specifiedAmperage = SimpleStringProperty(),
            measuredAmperage = SimpleStringProperty(),
            testTime = SimpleStringProperty(),
            result = SimpleStringProperty()
        ),
        TableValues(
            connection = SimpleStringProperty("Объект 4"),
            specifiedVoltage = SimpleStringProperty(),
            ktr = SimpleStringProperty(),
            measuredVoltage = SimpleStringProperty(),
            specifiedAmperage = SimpleStringProperty(),
            measuredAmperage = SimpleStringProperty(),
            testTime = SimpleStringProperty(),
            result = SimpleStringProperty()
        )
    )

    var ktr = 1f
    var ktrSettable = 1f

    private lateinit var currentTest: Test

    fun clearTable() {
        tableValues.forEach {
            it.specifiedVoltage.value = ""
            it.specifiedAmperage.value = ""
            it.testTime.value = ""
            it.ktr.value = ""
            it.result.value = ""
            it.measuredAmperage.value = ""
            it.measuredVoltage.value = ""
        }
    }

    fun clearTableResults() {
        tableValues.forEach {
            it.result.value = ""
        }
    }

    fun fillTableByEO() {
        clearTable()
        tableValues[0].specifiedVoltage.value = model.testObject.value.objectVoltage
        tableValues[0].specifiedAmperage.value = model.testObject.value.objectAmperage
        tableValues[0].testTime.value = model.testObject.value.objectTime
        tableValues[0].ktr.value = fillKtrCell()
    }

    private fun fillKtrCell(): String? {
        return when {
            model.testObject.value.objectTransformer == TYPE_1_VOLTAGE.toString() -> {
                with(model.coefficientsSettingsModel.fragments[MODULE_1_FRAGMENT]?.model) {
                    (this?.obj?.value?.toDouble()!! / this.tap.value?.toDouble()!!).autoformat()
                }
            }
            model.testObject.value.objectTransformer == TYPE_2_VOLTAGE.toString() -> {
                with(model.coefficientsSettingsModel.fragments[MODULE_2_FRAGMENT]?.model) {
                    (this?.obj?.value?.toDouble()!! / this.tap.value?.toDouble()!!).autoformat()
                }

            }
            else -> ""
        }
    }

    fun initTest() {
        thread {
            clearLog()
            initModule1()
            initGeneralDevices()
            startTest()
        }
    }

    fun clearLog() {
        Platform.runLater { view.vBoxLog.clear() }
    }

    private fun initGeneralDevices() {
        setVoltmeterKTR(model.testObject.value.objectTransformer)
        CommunicationModel.startPoll(DeviceID.PV21, Avem4Model.RMS_VOLTAGE, fillTableVoltage)
        initLatr()
    }

    private fun initOwenPR() {
        owenPrDevice.presetGeneralProtectionsMasks()
        owenPrDevice.presetBathProtectionsMasks()
        CommunicationModel.addWritingRegister(DeviceID.DD1, OwenPrModel.CMD, 1.toShort())
        owenPrDevice.resetTriggers()

        CommunicationModel.startPoll(DeviceID.DD1, OwenPrModel.DI_01_16_RAW, {})
        CommunicationModel.startPoll(DeviceID.DD1, OwenPrModel.DI_01_16_TRIG, {})
        CommunicationModel.startPoll(DeviceID.DD1, OwenPrModel.DI_01_16_TRIG_INV, {})
    }

    private fun initLatr() {
        CommunicationModel.startPoll(DeviceID.GV240, AvemLatrModel.DEVICE_STATUS) {}
        try {
            latrDevice.reset() {
                view.appendOneMessageToLog(
                    LogTag.MESSAGE,
                    "В предыдущем испытании АРН не завершил свою работу нормально. АРН возвращается в начало."
                )
            }
        } catch (e: LatrStuckException) {
            view.appendMessageToLog(LogTag.ERROR, "Ошибка возврата АРН в начало. АРН застрял.")
            currentTest.cause = Test.CauseDescriptor.LATR_STUCK
        }
    }

    private fun initModule1() {
        initOwenPR()
        ammeterDevice.toggleProgrammingMode()
        ammeterDevice.writeRegister(ammeterDevice.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDevice.writeRegister(ammeterDevice.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())

        fillTableVoltage = { value ->
            tableValues[0].measuredVoltage.value = value.toDouble().autoformat()
        }

        CommunicationModel.startPoll(DeviceID.PA11, Avem7Model.AMPERAGE) { value ->
            tableValues[0].measuredAmperage.value = value.toDouble().autoformat()
        }
    }

    private fun setVoltmeterKTR(transformer: String) {
        val coefficientSettingsModel = when (transformer) {
            TYPE_1_VOLTAGE.toString() -> {
                model.coefficientsSettingsModel.fragments.getValue(MODULE_1_FRAGMENT).model
            }
            TYPE_2_VOLTAGE.toString() -> {
                model.coefficientsSettingsModel.fragments.getValue(MODULE_2_FRAGMENT).model
            }
            else -> null
        }
        ktr = coefficientSettingsModel!!.obj.value.toFloat() / coefficientSettingsModel.tap.value.toFloat()
        ktrSettable = coefficientSettingsModel.obj.value.toFloat() / coefficientSettingsModel.latr.value.toFloat()
        voltmeterDevice.writeRuntimeKTR(ktr)
    }

    private fun startTest() {
        startModule1()
    }

    private fun startModule1() {
        currentTest = when {
            model.test.value == TEST_1 -> Test1(model, view, this)
            model.test.value == TEST_2 -> Test2(model, view, this)
            else -> GeneralTest(model, view, this)
        }
        currentTest.start()
    }

    fun deinitPollingModules() {
        CommunicationModel.clearPollingRegisters()
    }

    fun deinitWritingModules() {
        CommunicationModel.clearWritingRegisters()
    }

    fun buildLatrConfiguration(latrFragmentModel: LatrSettingsFragmentModel): LatrControllerConfiguration {
        return LatrControllerConfiguration(
            minDuttyPercent = latrFragmentModel.minDutty.value.toFloat(),
            maxDuttyPercent = latrFragmentModel.maxDutty.value.toFloat(),
            timePulseMin = latrFragmentModel.timeMinPulse.value.toInt(),
            timePulseMax = latrFragmentModel.timeMaxPulse.value.toInt(),
            corridor = latrFragmentModel.corridor.value.toFloat(),
            delta = latrFragmentModel.delta.value.toFloat()
        )
    }

    fun stopTest() {
        if (this::currentTest.isInitialized) {
            currentTest.cause = Test.CauseDescriptor.CANCELED
        }
    }

    fun isLatrInErrorMode() = when (latrDevice.getRegisterById(AvemLatrModel.DEVICE_STATUS).value) {
        0x81 -> {
            view.appendMessageToLog(LogTag.ERROR, "Сработал верхний концевик при движении вверх")
            true
        }
        0x82 -> {
            view.appendMessageToLog(LogTag.ERROR, "Сработал нижний концевик при движении вниз")
            true
        }
        0x83 -> {
            view.appendMessageToLog(LogTag.ERROR, "Сработали оба концевика")
            true
        }
        0x84 -> {
            view.appendMessageToLog(LogTag.ERROR, "Время регулирования превысило заданное")
            true
        }
        0x85 -> {
            view.appendMessageToLog(LogTag.ERROR, "Застревание АРН")
            true
        }
        else -> false
    }

    fun disassembleType1Scheme() {
        owenPrDevice.offTransformer200V()
        owenPrDevice.offSoundAlarm()
        owenPrDevice.offShortlocker20kV()
        owenPrDevice.offButtonPostPower()
    }

    fun disassembleType2Scheme() {
        owenPrDevice.offTransformer20kV()
        owenPrDevice.offSoundAlarm()
        owenPrDevice.offShortlocker20kV()
        owenPrDevice.offButtonPostPower()
    }

    fun disassembleType3Scheme() {
        owenPrDevice.offTransformer50kV()
        owenPrDevice.offSoundAlarm()
        owenPrDevice.offShortlocker50kV()
        owenPrDevice.offButtonPostPower()
    }
}
