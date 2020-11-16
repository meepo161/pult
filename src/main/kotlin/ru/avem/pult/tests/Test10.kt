package ru.avem.pult.tests

import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.pult.controllers.TestController
import ru.avem.pult.utils.CallbackTimer
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.seconds
import java.lang.Thread.sleep

//Испытание повышенным напряжением рабочей части указателя напряжения до 200 В
class Test10(model: MainViewModel, view: TestView, controller: TestController) : Test(model, view, controller) {
    override fun start() {
        super.start()
        isTestRunning = true
        switchExperimentButtonsState()

        if (controller.owenPrDD2.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (controller.owenPrDD2.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }
        if (!controller.owenPrDD3.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
        }

        if (isTestRunning && isDevicesResponding()) {
            waitForUserStart()
            if (isTestRunning && isDevicesResponding()) {
                controller.owenPrDD2.turnOnLampLess1000()
                controller.owenPrDD2.onLightSign()
                controller.owenPrDD2.offStepDownTransformer()
                controller.owenPrDD2.onSoundAlarm()
                sleep(3000)
                controller.owenPrDD2.offSoundAlarm()
                controller.owenPrDD3.togglePowerSupplyMode()
                controller.owenPrDD2.onShortlocker20kV()
                sleep(1000)
                if (!controller.owenPrDD2.is20kVshortlockerSwitched()) {
                    cause = CauseDescriptor.SHORTLOCKER_NOT_WORKING_20KV
                }
                controller.owenPrDD2.onTransformer200V()
                sleep(1000)
                if (!controller.owenPrDD2.is200VcontactorSwitched()) {
                    cause = CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_200V
                }
            }

            checkProtections()

            if (isTestRunning && model.manualVoltageRegulation.value) {
                view.appendMessageToLog(LogTag.MESSAGE, "Запуск АРН в режиме ручного регулирования напряжения")
                controller.latrDevice.startManual(model.testObject.value.objectVoltage.toFloat())

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
                    controller.latrDevice.start(model.testObject.value.objectVoltage.toFloat())
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
                        model.testObject.value.objectVoltage.toFloat() * 0.98f ||
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() * 1.02f
                    ) {
                        checkProtections()
                        view.appendOneMessageToLog(LogTag.MESSAGE, "Точная регулировка напряжения")
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                            model.testObject.value.objectVoltage.toFloat() * 0.98f
                        ) {
                            controller.latrDevice.plusVoltage()
                            sleep(200)
                        }
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                            model.testObject.value.objectVoltage.toFloat() * 1.02f
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
                    CallbackTimer(
                        tickPeriod = 1.seconds,
                        tickTimes = model.testObject.value.objectTime.toInt(),
                        onStartJob = {
                            controller.owenPrDD2.onTimer()
                        },
                        tickJob = {
                            if (isTestRunning) {
                                controller.tableValues[0].testTime.value = it.getCurrentTicks().toString()
                                view.setExperimentProgress(
                                    it.getCurrentTicks(),
                                    model.testObject.value.objectTime.toInt()
                                )
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
                markChannel()
                cause = when {
                    isChannelBad() -> {
                        CauseDescriptor.ONE_OR_MORE_CHANNELS_BAD
                    }
                    else -> {
                        CauseDescriptor.SUCCESS
                    }
                }
            }
        }
    }

    private fun markChannel() {
        if (controller.owenPrDD3.isBathChannelTriggered1() ||
            controller.ammeterDeviceP2.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
            cause != CauseDescriptor.EMPTY
        ) {
            controller.tableValues[0].result.value = "Провал"
        } else {
            controller.tableValues[0].result.value = "Успех"
        }
    }

    private fun isChannelBad(): Boolean {
        return controller.ammeterDeviceP2.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat()
    }

    private fun checkProtections() {
        if (controller.owenPrDD2.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }
        if (controller.owenPrDD2.isGeneralAmmeterRelayTriggered()) {
            cause = CauseDescriptor.GENERAL_AMMETER_RELAY
        }
        if (!isDevicesResponding()) {
            cause = CauseDescriptor.DEVICES_NOT_RESPONDING
        }
        if (controller.owenPrDD2.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (!controller.owenPrDD3.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
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
    }

    private fun isDevicesResponding() =
        controller.owenPrDD2.isResponding && controller.ammeterDeviceP2.isResponding && controller.latrDevice.isResponding && controller.voltmeterDevice.isResponding

    override fun getNotRespondingMessageFromTest() =
        if (controller.owenPrDD2.isResponding) "" else "Owen PR (DD2)" +
                if (controller.ammeterDeviceP2.isResponding) "" else ", Ammeter (P2)" +
                        if (controller.latrDevice.isResponding) "" else ", ATR (GV240)" +
                                if (controller.voltmeterDevice.isResponding) "" else ", Voltmeter (PV21)"
}