package ru.avem.pult.tests

import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.pult.controllers.TestController
import ru.avem.pult.controllers.TestController.Companion.MANUAL_TICK_COUNT
import ru.avem.pult.utils.CallbackTimer
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.seconds
import java.lang.Thread.sleep

//Испытание повышенным напряжением рабочей части указателя напряжения до 3000 В
class Test1(model: MainViewModel, view: TestView, controller: TestController) : Test(model, view, controller) {
    var isUsingAccurate = true

    override fun start() {
        super.start()
        isTestRunning = true
        switchExperimentButtonsState()

        if (controller.owenPrDevice.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (controller.owenPrDevice.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }
        if (!controller.owenPrDevice.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
        }

        if (isTestRunning && isDevicesResponding()) {
            waitForUserStart()
            if (isTestRunning) {
                controller.owenPrDevice.onLightSign()
                if (model.testObject.value.objectVoltage.toInt() >= 1000) {
                    controller.owenPrDevice.turnOnLampMore1000()
                } else {
                    controller.owenPrDevice.turnOnLampLess1000()
                }
            }

            when (model.testObject.value.objectTransformer) {
                TYPE_2_VOLTAGE.toString() -> {
                    if (isTestRunning && isDevicesResponding()) {
                        controller.owenPrDevice.onSoundAlarm()
                        sleep(3000)
                        controller.owenPrDevice.offSoundAlarm()
                        controller.owenPrDevice.togglePowerSupplyMode()
                        controller.owenPrDevice.onShortlocker20kV()
                        sleep(1000)
                        if (!controller.owenPrDevice.is20kVshortlockerSwitched()) {
                            cause = CauseDescriptor.SHORTLOCKER_NOT_WORKING_20KV
                        }
                        if (model.testObject.value.objectVoltage.toInt() > 3000) {
                            controller.owenPrDevice.offStepDownTransformer()
                            isUsingAccurate = false
                        }
                        controller.owenPrDevice.onTransformer20kV()
                        sleep(1000)
                        if (!controller.owenPrDevice.is20kVcontactorSwitched()) {
                            cause = CauseDescriptor.TEST_CONTACTOR_NOT_WORKING_20KV
                        }
                    }
                }
            }
        }

        checkProtections()

        if (isTestRunning && model.isManualVoltageRegulation.value) {
            view.appendMessageToLog(LogTag.MESSAGE, "Запуск АРН в режиме ручного регулирования напряжения")
            controller.latrDevice.startManual(
                (model.testObject.value.objectVoltage.toFloat() * if (isUsingAccurate) 7.3f else 1f) / controller.ktrSettable
            )

            val manualTimer = CallbackTimer(tickPeriod = 1.seconds, tickTimes = MANUAL_TICK_COUNT, tickJob = {
                if (!isTestRunning) {
                    it.stop()
                }
                if (controller.owenPrDevice.isTimerStartPressed()) {
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
                        controller.owenPrDevice.onTimer()
                    },
                    tickJob = {
                        if (isTestRunning) {
                            controller.tableValues[0].testTime.value = it.getCurrentTicks().toString()
                            view.setExperimentProgress(it.getCurrentTicks(), model.testObject.value.objectTime.toInt())
                        } else {
                            it.stop()
                        }
                    })

            while (timer.isRunning && !controller.owenPrDevice.isTimerStopPressed()) {
                checkProtections()
            }
        }
        controller.owenPrDevice.offTimer()
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

    private fun checkProtections() {
        if (controller.owenPrDevice.isTotalAmperageProtectionTriggered()) {
            cause = CauseDescriptor.AMPERAGE_PROTECTION_TRIGG
        }
        if (controller.owenPrDevice.isGeneralAmmeterRelayTriggered()) {
            controller.owenPrDevice.offButtonPostPower()
            cause = CauseDescriptor.GENERAL_AMMETER_RELAY
        }
        if (!isDevicesResponding()) {
            cause = CauseDescriptor.DEVICES_NOT_RESPONDING
        }
        if (controller.owenPrDevice.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (!controller.owenPrDevice.isBathDoorClosed()) {
            cause = CauseDescriptor.BATH_DOOR_NOT_CLOSED
        }
        if (!controller.owenPrDevice.isHiSwitchTurned()) {
            cause = CauseDescriptor.HI_POWER_SWITCH_LOCKED
        }
        if (controller.owenPrDevice.isStopPressed()) {
            cause = CauseDescriptor.CANCELED
        }
        if (controller.isLatrInErrorMode()) {
            cause = CauseDescriptor.LATR_CONTROLLER_ERROR
        }
    }

    private fun markChannel() {
        if (controller.owenPrDevice.isBathChannelTriggered1() ||
            controller.ammeterDevice.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() ||
            cause != CauseDescriptor.EMPTY
        ) {
            controller.tableValues[0].result.value = "Провал"
        } else {
            controller.tableValues[0].result.value = "Успех"
        }
    }

    private fun isChannelBad(): Boolean {
        return controller.ammeterDevice.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat()
    }

    private fun isDevicesResponding() =
        controller.owenPrDevice.isResponding && controller.ammeterDevice.isResponding && controller.latrDevice.isResponding && controller.voltmeterDevice.isResponding

    override fun getNotRespondingMessageFromTest() =
        if (controller.owenPrDevice.isResponding) "" else "Owen PR (DD2)" +
                if (controller.ammeterDevice.isResponding) "" else ", Ammeter (P2)" +
                        if (controller.latrDevice.isResponding) "" else ", ATR (GV240)" +
                                if (controller.voltmeterDevice.isResponding) "" else ", Voltmeter (PV21)"
}
