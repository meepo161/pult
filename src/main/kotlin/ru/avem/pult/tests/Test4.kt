package ru.avem.pult.tests

import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.pult.controllers.TestController
import ru.avem.pult.utils.CallbackTimer
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_4
import tornadofx.seconds
import java.lang.Thread.sleep

//Испытание защитных средств из диэлектрической резины
class Test4(model: MainViewModel, view: TestView, controller: TestController) : Test(model, view, controller) {
    var isUsingAccurate = true

    override fun start() {
        super.start()

        isTestRunning = true
        switchExperimentButtonsState()

        if (!controller.owenPrDD3.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
        }
        if (controller.owenPrDD2.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (controller.owenPrDD2.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }

        if (isTestRunning && isDevicesResponding()) {
            waitForUserStart()
            if (isTestRunning && isDevicesResponding()) {
                controller.owenPrDD2.onLightSign()
                if (model.testObject.value.objectVoltage.toInt() >= 1000) {
                    controller.owenPrDD2.turnOnLampMore1000()
                } else {
                    controller.owenPrDD2.turnOnLampLess1000()
                }
                controller.owenPrDD2.onSoundAlarm()
                sleep(3000)
                controller.owenPrDD2.offSoundAlarm()
                controller.owenPrDD3.togglePowerSupplyMode()
                controller.owenPrDD2.onShortlocker20kV()
                sleep(1000)
                if (!controller.owenPrDD2.is20kVshortlockerSwitched()) {
                    cause = CauseDescriptor.SHORTLOCKER_NOT_WORKING_20KV
                }
                if (model.testObject.value.objectVoltage.toInt() > 3000) {
                    controller.owenPrDD2.offStepDownTransformer()
                    isUsingAccurate = false
                }
                controller.owenPrDD2.onTransformer20kV()
                sleep(1000)
                if (!controller.owenPrDD2.is20kVcontactorSwitched()) {
                    cause = CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_20KV
                }
            }
        }

        if (isTestRunning && isDevicesResponding()) {
            controller.owenPrDD3.togglePowerSupplyMode()
            if (model.connectionPoint1.property.value) {
                controller.owenPrDD3.onBathChannel1()
            }
            if (model.connectionPoint2.property.value) {
                controller.owenPrDD3.onBathChannel2()
            }
            if (model.connectionPoint3.property.value) {
                controller.owenPrDD3.onBathChannel3()
            }
            if (model.connectionPoint4.property.value) {
                controller.owenPrDD3.onBathChannel4()
            }
        }

        checkProtections()

        if (isTestRunning && model.manualVoltageRegulation.value) {
            view.appendMessageToLog(LogTag.MESSAGE, "Запуск АРН в режиме ручного регулирования напряжения")
            controller.latrDevice.startManual(
                (model.testObject.value.objectVoltage.toFloat() * if (isUsingAccurate) 7.3f else 1f) / controller.ktrSettable
            )

            val manualTimer =
                CallbackTimer(tickPeriod = 1.seconds, tickTimes = TestController.MANUAL_TICK_COUNT, tickJob = {
                    if (!isTestRunning) {
                        it.stop()
                    }
                    if (controller.owenPrDD2.isTimerStartPressed()) {
                        it.stop()
                    }
                }, onFinishJob = {
                    cause = CauseDescriptor.MANUAL_MODE_TIMEOUT
                })

            while (manualTimer.isRunning) {
                checkProtections()
                sleep(200)
            }

            controller.latrDevice.offManualMode()
        } else {
            if (isTestRunning && isDevicesResponding()) {
                view.appendMessageToLog(LogTag.MESSAGE, "Конфигурирование и запуск АРН")
                val latrConfiguration = controller.buildLatrConfiguration(model.getLatrParameters())
                controller.latrDevice.presetParameters(latrConfiguration)
                controller.latrDevice.start(
                    (model.testObject.value.objectVoltage.toFloat() * if (isUsingAccurate) 7.3f else 1f) / controller.ktrSettable
                )
            }

            while (isTestRunning && isDevicesResponding() &&
                controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                model.testObject.value.objectVoltage.toFloat() * 0.9f
            ) {
                view.appendOneMessageToLog(LogTag.MESSAGE, "Грубая регулировка напряжения")
                checkProtections()
            }

            if (isTestRunning) {
                controller.latrDevice.presetAccurateParameters()
                while (isTestRunning && isDevicesResponding() &&
                    controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                    model.testObject.value.objectVoltage.toFloat() * 0.97f ||
                    controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                    model.testObject.value.objectVoltage.toFloat() * 1.03f
                ) {
                    checkProtections()
                    view.appendOneMessageToLog(LogTag.MESSAGE, "Точная регулировка напряжения")
                    if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                        model.testObject.value.objectVoltage.toFloat() * 0.97f
                    ) {
                        controller.latrDevice.plusVoltage()
                        sleep(200)
                    }
                    if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() * 1.03f
                    ) {
                        controller.latrDevice.minusVoltage()
                        sleep(200)
                    }
                }
            }

            controller.latrDevice.stop()
        }

        if (isTestRunning) {
            val timer =
                CallbackTimer(tickPeriod = 1.seconds, tickTimes = model.testObject.value.objectTime.toInt(),
                    onStartJob = {
                        controller.owenPrDD2.onTimer()
                    },
                    tickJob = {
                        if (isTestRunning) {
                            if (model.connectionPoint1.isNeedToUpdate) {
                                controller.tableValues[0].testTime.value = it.getCurrentTicks().toString()
                            }
                            if (model.connectionPoint2.isNeedToUpdate) {
                                controller.tableValues[1].testTime.value = it.getCurrentTicks().toString()
                            }
                            if (model.connectionPoint3.isNeedToUpdate) {
                                controller.tableValues[2].testTime.value = it.getCurrentTicks().toString()
                            }
                            if (model.connectionPoint4.isNeedToUpdate) {
                                controller.tableValues[3].testTime.value = it.getCurrentTicks().toString()
                            }
                            view.setExperimentProgress(it.getCurrentTicks(), model.testObject.value.objectTime.toInt())
                        } else {
                            it.stop()
                        }
                    })

            while (timer.isRunning && !controller.owenPrDD2.isTimerStopPressed()) {
                checkProtections()
            }
        }
        controller.owenPrDD2.offTimer()
        if (cause != CauseDescriptor.CANCELED) {
            markChannels()
            cause = when {
                isOneOrMoreChannelsTriggered() -> {
                    CauseDescriptor.ONE_OR_MORE_CHANNELS_TRIGGERED
                }
                isOneOrMoreChannelsBad() -> {
                    CauseDescriptor.ONE_OR_MORE_CHANNELS_BAD
                }
                else -> {
                    CauseDescriptor.SUCCESS
                }
            }
        }
    }

    private fun markChannels() {
        model.selectedConnectionPoints[CONNECTION_1]?.let {
            if (controller.owenPrDD3.isBathChannelTriggered1() ||
                controller.ammeterDeviceP3.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                cause != CauseDescriptor.EMPTY
            ) {
                controller.tableValues[0].result.value = "Провал"
            } else {
                controller.tableValues[0].result.value = "Успех"
            }
        }
        model.selectedConnectionPoints[CONNECTION_2]?.let {
            if (controller.owenPrDD3.isBathChannelTriggered2() ||
                controller.ammeterDeviceP4.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                cause != CauseDescriptor.EMPTY
            ) {
                controller.tableValues[1].result.value = "Провал"
            } else {
                controller.tableValues[1].result.value = "Успех"
            }
        }
        model.selectedConnectionPoints[CONNECTION_3]?.let {
            if (controller.owenPrDD3.isBathChannelTriggered3() ||
                controller.ammeterDeviceP5.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                cause != CauseDescriptor.EMPTY
            ) {
                controller.tableValues[2].result.value = "Провал"
            } else {
                controller.tableValues[2].result.value = "Успех"
            }
        }
        model.selectedConnectionPoints[CONNECTION_4]?.let {
            if (controller.owenPrDD3.isBathChannelTriggered4() ||
                controller.ammeterDeviceP6.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                cause != CauseDescriptor.EMPTY
            ) {
                controller.tableValues[3].result.value = "Провал"
            } else {
                controller.tableValues[3].result.value = "Успех"
            }
        }
    }

    private fun checkProtections() {
        if (controller.owenPrDD2.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }
        if (!isDevicesResponding()) {
            cause = CauseDescriptor.DEVICES_NOT_RESPONDING
        }
        if (!controller.owenPrDD3.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
        }
        if (controller.owenPrDD2.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (!controller.owenPrDD2.isHiSwitchTurned()) {
            cause = CauseDescriptor.HI_POWER_SWITCH_LOCKED
        }
        if (controller.owenPrDD2.isStopPressed()) {
            cause = CauseDescriptor.CANCELED
        }
        if (controller.isLatrInErrorMode()) {
            cause = CauseDescriptor.LATR_CONTROLLER_ERROR
        }

        var allChannelsTrigger = model.selectedConnectionPoints.isNotEmpty()
        model.selectedConnectionPoints[CONNECTION_1]?.let {
            allChannelsTrigger = allChannelsTrigger && controller.owenPrDD3.isBathChannelTriggered1()
        }
        model.selectedConnectionPoints[CONNECTION_2]?.let {
            allChannelsTrigger = allChannelsTrigger && controller.owenPrDD3.isBathChannelTriggered2()
        }
        model.selectedConnectionPoints[CONNECTION_3]?.let {
            allChannelsTrigger = allChannelsTrigger && controller.owenPrDD3.isBathChannelTriggered3()
        }
        model.selectedConnectionPoints[CONNECTION_4]?.let {
            allChannelsTrigger = allChannelsTrigger && controller.owenPrDD3.isBathChannelTriggered4()
        }
        if (allChannelsTrigger) {
            cause = CauseDescriptor.ALL_CHANNELS_TRIGGERED
        }
        actualizeChannels()
    }

    private fun isDevicesResponding() =
        controller.owenPrDD2.isResponding &&
                controller.owenPrDD3.isResponding &&
                controller.ammeterDeviceP3.isResponding &&
                controller.ammeterDeviceP4.isResponding &&
                controller.ammeterDeviceP5.isResponding &&
                controller.ammeterDeviceP6.isResponding &&
                controller.latrDevice.isResponding &&
                controller.voltmeterDevice.isResponding

    private fun actualizeChannels() {
        if (controller.owenPrDD3.isBathChannelTriggered1()) {
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.P12, Avem7Model.AMPERAGE)
            model.connectionPoint1.isNeedToUpdate = false
            controller.tableValues[0].result.value = "Провал"
        }
        if (controller.owenPrDD3.isBathChannelTriggered2()) {
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.P13, Avem7Model.AMPERAGE)
            model.connectionPoint2.isNeedToUpdate = false
            controller.tableValues[1].result.value = "Провал"
        }
        if (controller.owenPrDD3.isBathChannelTriggered3()) {
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.P14, Avem7Model.AMPERAGE)
            model.connectionPoint3.isNeedToUpdate = false
            controller.tableValues[2].result.value = "Провал"
        }
        if (controller.owenPrDD3.isBathChannelTriggered4()) {
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.P15, Avem7Model.AMPERAGE)
            model.connectionPoint4.isNeedToUpdate = false
            controller.tableValues[3].result.value = "Провал"
        }
    }

    private fun isOneOrMoreChannelsTriggered(): Boolean {
        return controller.owenPrDD3.isBathChannelTriggered1() ||
                controller.owenPrDD3.isBathChannelTriggered2() ||
                controller.owenPrDD3.isBathChannelTriggered3() ||
                controller.owenPrDD3.isBathChannelTriggered4()
    }

    private fun isOneOrMoreChannelsBad(): Boolean {
        return controller.ammeterDeviceP3.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                controller.ammeterDeviceP4.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                controller.ammeterDeviceP5.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
                controller.ammeterDeviceP6.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat()
    }

    override fun getNotRespondingMessageFromTest() =
        if (controller.owenPrDD2.isResponding) "" else "Owen PR (DD2)" +
                if (controller.ammeterDeviceP3.isResponding) "" else ", Ammeter (P3)" +
                        if (controller.ammeterDeviceP4.isResponding) "" else ", Ammeter (P4)" +
                                if (controller.ammeterDeviceP5.isResponding) "" else ", Ammeter (P5)" +
                                        if (controller.ammeterDeviceP6.isResponding) "" else ", Ammeter (P6)" +
                                                if (controller.latrDevice.isResponding) "" else ", ATR (GV240)" +
                                                        if (controller.voltmeterDevice.isResponding) "" else ", Voltmeter (PV21)"
}
