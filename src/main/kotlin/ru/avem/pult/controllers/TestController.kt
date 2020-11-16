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
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_1_FRAGMENT
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_2_FRAGMENT
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_3_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsFragmentModel
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_4
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_10
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_4
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_5
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_6
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_7
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_8
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_9
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_3_VOLTAGE
import ru.avem.pult.tests.*
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

    val owenPrDD2 = getDeviceById(DeviceID.DD2) as OwenPrController
    val owenPrDD3 = getDeviceById(DeviceID.DD3) as OwenPrController
    val latrDevice = getDeviceById(DeviceID.GV240) as AvemLatrController
    val voltmeterDevice = getDeviceById(DeviceID.PV21) as Avem4Controller
    val ammeterDeviceP2 = getDeviceById(DeviceID.P11) as Avem7Controller
    val ammeterDeviceP3 = getDeviceById(DeviceID.P12) as Avem7Controller
    val ammeterDeviceP4 = getDeviceById(DeviceID.P13) as Avem7Controller
    val ammeterDeviceP5 = getDeviceById(DeviceID.P14) as Avem7Controller
    val ammeterDeviceP6 = getDeviceById(DeviceID.P15) as Avem7Controller

    val deviceControllers = listOf(
        owenPrDD2,
        owenPrDD3,
        latrDevice,
        voltmeterDevice,
        ammeterDeviceP2,
        ammeterDeviceP3,
        ammeterDeviceP4,
        ammeterDeviceP5,
        ammeterDeviceP6
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
        if (model.module.value == MODULE_2) {
            model.selectedConnectionPoints[CONNECTION_1]?.let {
                tableValues[0].specifiedVoltage.value = model.testObject.value.objectVoltage
                tableValues[0].specifiedAmperage.value = model.testObject.value.objectAmperage
                tableValues[0].testTime.value = model.testObject.value.objectTime
                tableValues[0].ktr.value = fillKtrCell()
            }
            model.selectedConnectionPoints[CONNECTION_2]?.let {
                tableValues[1].specifiedVoltage.value = model.testObject.value.objectVoltage
                tableValues[1].specifiedAmperage.value = model.testObject.value.objectAmperage
                tableValues[1].testTime.value = model.testObject.value.objectTime
                tableValues[1].ktr.value = fillKtrCell()
            }
            model.selectedConnectionPoints[CONNECTION_3]?.let {
                tableValues[2].specifiedVoltage.value = model.testObject.value.objectVoltage
                tableValues[2].specifiedAmperage.value = model.testObject.value.objectAmperage
                tableValues[2].testTime.value = model.testObject.value.objectTime
                tableValues[2].ktr.value = fillKtrCell()
            }
            model.selectedConnectionPoints[CONNECTION_4]?.let {
                tableValues[3].specifiedVoltage.value = model.testObject.value.objectVoltage
                tableValues[3].specifiedAmperage.value = model.testObject.value.objectAmperage
                tableValues[3].testTime.value = model.testObject.value.objectTime
                tableValues[3].ktr.value = fillKtrCell()
            }
        } else {
            tableValues[0].specifiedVoltage.value = model.testObject.value.objectVoltage
            tableValues[0].specifiedAmperage.value = model.testObject.value.objectAmperage
            tableValues[0].testTime.value = model.testObject.value.objectTime
            tableValues[0].ktr.value = fillKtrCell()
        }
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
            model.testObject.value.objectTransformer == TYPE_3_VOLTAGE.toString() -> {
                with(model.coefficientsSettingsModel.fragments[MODULE_3_FRAGMENT]?.model) {
                    (this?.obj?.value?.toDouble()!! / this.tap.value?.toDouble()!!).autoformat()
                }
            }
            else -> ""
        }
    }

    fun initTest() {
        thread {
            clearLog()
            when {
                model.testObject.value.objectModule == MODULE_1 -> initModule1()
                model.testObject.value.objectModule == MODULE_2 -> initModule2()
                model.testObject.value.objectModule == MODULE_3 -> initModule3()
            }
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

    private fun initOwenPR(deviceID: DeviceID, controller: OwenPrController) {
        if (deviceID == DeviceID.DD2) {
            controller.presetGeneralProtectionsMasks()
        } else {
            controller.presetBathProtectionsMasks()
        }
        CommunicationModel.addWritingRegister(deviceID, OwenPrModel.CMD, 1.toShort())
        controller.resetTriggers()

        CommunicationModel.startPoll(deviceID, OwenPrModel.DI_01_16_RAW, {})
        CommunicationModel.startPoll(deviceID, OwenPrModel.DI_01_16_TRIG, {})
        CommunicationModel.startPoll(deviceID, OwenPrModel.DI_01_16_TRIG_INV, {})
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
        initOwenPR(DeviceID.DD2, owenPrDD2)
        ammeterDeviceP2.toggleProgrammingMode()
        ammeterDeviceP2.writeRegister(ammeterDeviceP2.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP2.writeRegister(ammeterDeviceP2.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())

        fillTableVoltage = { value ->
            tableValues[0].measuredVoltage.value = value.toDouble().autoformat()
        }

        CommunicationModel.startPoll(DeviceID.P11, Avem7Model.AMPERAGE) { value ->
            tableValues[0].measuredAmperage.value = value.toDouble().autoformat()
        }
    }

    private fun initModule2() {
        initOwenPR(DeviceID.DD2, owenPrDD2)
        initOwenPR(DeviceID.DD3, owenPrDD3)
        ammeterDeviceP3.toggleProgrammingMode()
        ammeterDeviceP4.toggleProgrammingMode()
        ammeterDeviceP5.toggleProgrammingMode()
        ammeterDeviceP6.toggleProgrammingMode()
        ammeterDeviceP3.writeRegister(ammeterDeviceP3.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP3.writeRegister(ammeterDeviceP3.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())
        ammeterDeviceP4.writeRegister(ammeterDeviceP4.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP4.writeRegister(ammeterDeviceP4.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())
        ammeterDeviceP5.writeRegister(ammeterDeviceP5.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP5.writeRegister(ammeterDeviceP5.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())
        ammeterDeviceP6.writeRegister(ammeterDeviceP6.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP6.writeRegister(ammeterDeviceP6.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())

        fillTableVoltage = { value ->
            model.selectedConnectionPoints[CONNECTION_1]?.let {
                if (it.isNeedToUpdate) {
                    tableValues[0].measuredVoltage.value = value.toDouble().autoformat()
                }
            }
            model.selectedConnectionPoints[CONNECTION_2]?.let {
                if (it.isNeedToUpdate) {
                    tableValues[1].measuredVoltage.value = value.toDouble().autoformat()
                }
            }
            model.selectedConnectionPoints[CONNECTION_3]?.let {
                if (it.isNeedToUpdate) {
                    tableValues[2].measuredVoltage.value = value.toDouble().autoformat()
                }
            }
            model.selectedConnectionPoints[CONNECTION_4]?.let {
                if (it.isNeedToUpdate) {
                    tableValues[3].measuredVoltage.value = value.toDouble().autoformat()
                }
            }
        }

        model.selectedConnectionPoints[CONNECTION_1]?.let {
            CommunicationModel.startPoll(DeviceID.P12, Avem7Model.AMPERAGE) { value ->
                tableValues[0].measuredAmperage.value = value.toDouble().autoformat()
            }
        }
        model.selectedConnectionPoints[CONNECTION_2]?.let {
            CommunicationModel.startPoll(DeviceID.P13, Avem7Model.AMPERAGE) { value ->
                tableValues[1].measuredAmperage.value = value.toDouble().autoformat()
            }
        }
        model.selectedConnectionPoints[CONNECTION_3]?.let {
            CommunicationModel.startPoll(DeviceID.P14, Avem7Model.AMPERAGE) { value ->
                tableValues[2].measuredAmperage.value = value.toDouble().autoformat()
            }
        }
        model.selectedConnectionPoints[CONNECTION_4]?.let {
            CommunicationModel.startPoll(DeviceID.P15, Avem7Model.AMPERAGE) { value ->
                tableValues[3].measuredAmperage.value = value.toDouble().autoformat()
            }
        }
    }

    private fun initModule3() {
        initOwenPR(DeviceID.DD2, owenPrDD2)
        ammeterDeviceP2.toggleProgrammingMode()
        ammeterDeviceP2.writeRegister(ammeterDeviceP2.getRegisterById(Avem7Model.SHUNT), 10f)
        ammeterDeviceP2.writeRegister(ammeterDeviceP2.getRegisterById(Avem7Model.PGA_MODE), 7.toShort())

        fillTableVoltage = { value ->
            tableValues[0].measuredVoltage.value = value.toDouble().autoformat()
        }

        CommunicationModel.startPoll(DeviceID.P11, Avem7Model.AMPERAGE) { value ->
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
            TYPE_3_VOLTAGE.toString() -> {
                model.coefficientsSettingsModel.fragments.getValue(MODULE_3_FRAGMENT).model
            }
            else -> null
        }
        ktr = coefficientSettingsModel!!.obj.value.toFloat() / coefficientSettingsModel.tap.value.toFloat()
        ktrSettable = coefficientSettingsModel.obj.value.toFloat() / coefficientSettingsModel.latr.value.toFloat()
        voltmeterDevice.writeRuntimeKTR(ktr)
    }

    private fun startTest() {
        when {
            model.testObject.value.objectModule == MODULE_1 -> startModule1()
            model.testObject.value.objectModule == MODULE_2 -> startModule2()
            model.testObject.value.objectModule == MODULE_3 -> startModule3()
        }
    }

    private fun startModule1() {
        currentTest = when {
            model.test.value == TEST_1 -> Test1(model, view, this)
            model.test.value == TEST_2 -> Test2(model, view, this)
            model.test.value == TEST_10 -> Test10(model, view, this)
            else -> GeneralTest(model, view, this)
        }
        currentTest.start()
    }

    private fun startModule2() {
        currentTest = when {
            model.test.value == TEST_3 -> Test3(model, view, this)
            model.test.value == TEST_4 -> Test4(model, view, this)
            model.test.value == TEST_5 -> Test5(model, view, this)
            else -> GeneralTest(model, view, this)
        }
        currentTest.start()
    }

    private fun startModule3() {
        currentTest = when {
            model.test.value == TEST_1 -> Test1(model, view, this)
            model.test.value == TEST_3 -> Test3(model, view, this)
            model.test.value == TEST_6 -> Test6(model, view, this)
            model.test.value == TEST_7 -> Test7(model, view, this)
            model.test.value == TEST_8 -> Test8(model, view, this)
            model.test.value == TEST_9 -> Test9(model, view, this)
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
        owenPrDD2.offTransformer200V()
        owenPrDD2.offSoundAlarm()
        owenPrDD2.offShortlocker20kV()
        owenPrDD2.offButtonPostPower()
    }

    fun disassembleType2Scheme() {
        owenPrDD2.offTransformer20kV()
        owenPrDD2.offSoundAlarm()
        owenPrDD2.offShortlocker20kV()
        owenPrDD2.offButtonPostPower()
    }

    fun disassembleType3Scheme() {
        owenPrDD2.offTransformer50kV()
        owenPrDD2.offSoundAlarm()
        owenPrDD2.offShortlocker50kV()
        owenPrDD2.offButtonPostPower()
    }
}
