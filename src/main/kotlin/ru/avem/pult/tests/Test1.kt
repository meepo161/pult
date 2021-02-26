package ru.avem.pult.tests

import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Model
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Model
import ru.avem.pult.controllers.TestController
import ru.avem.pult.controllers.TestController.Companion.MANUAL_TICK_COUNT
import ru.avem.pult.utils.CallbackTimer
import ru.avem.pult.utils.LogTag
import ru.avem.pult.view.TestView
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.seconds
import java.lang.Thread.sleep

//"Проверка качества изоляции повышенным напряжением промышленной частоты"
class Test1(model: MainViewModel, view: TestView, controller: TestController) : Test(model, view, controller) {
    var isUsingAccurate = false

    override fun start() {
        super.start()
        switchExperimentButtonsState()
        isTestRunning = true

        listOfValuesU.clear()
        listOfValuesI.clear()


        if (controller.owenPrDevice.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (controller.owenPrDevice.isSectionDoorOpened()) {
            cause = CauseDescriptor.SECTION_DOOR_WAS_OPENED
        }
        if (controller.owenPrDevice.isKa1Triggered()) {
            cause = CauseDescriptor.KA1_TRIGGERED
        }
        if (controller.owenPrDevice.isKa2Triggered()) {
            cause = CauseDescriptor.KA2_TRIGGERED
        }

        if (isTestRunning && isDevicesResponding()) {
//            controller.owenPrDevice.onSoundAlarm()
//            sleep(3000)
//            controller.owenPrDevice.offSoundAlarm()
            controller.owenPrDevice.onViuShortlocker()
            sleep(1000)
            controller.owenPrDevice.onArnPower()
            sleep(1000)
            controller.owenPrDevice.onViuPower()
            sleep(1000)
//            if (!controller.owenPrDevice.isViuPowerOn()) {
//                cause = CauseDescriptor.VIU_CONTACTOR_NOT_WORKING
//            }
        }

        checkProtections()

        if (isTestRunning && model.isManualVoltageRegulation.value) {
            view.appendMessageToLog(LogTag.MESSAGE, "Запуск АРН в режиме ручного регулирования напряжения")
            controller.latrDevice.startManual(
                (model.testObject.value.objectVoltage.toFloat() * if (isUsingAccurate) 7.3f else 1f) / controller.ktrSettable
            )

            val manualTimer = CallbackTimer(tickPeriod = 1.seconds, tickTimes = MANUAL_TICK_COUNT, tickJob = {
                if (!isTestRunning || controller.isTimerStart) {
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

            if (model.testObject.value.objectVoltage.toFloat() <= 10000) {

                while (isTestRunning && isDevicesResponding() &&
                    controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                    model.testObject.value.objectVoltage.toFloat() - 4000
                ) {
                    view.appendOneMessageToLog(LogTag.MESSAGE, "Грубая регулировка напряжения")
                    checkProtections()
                }

                if (isTestRunning) {
                    controller.latrDevice.presetAccurateParameters()


                    while (isTestRunning && isDevicesResponding() &&
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                        model.testObject.value.objectVoltage.toFloat() - 2000 ||
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() + 2000
                    ) {
                        checkProtections()
                        view.appendOneMessageToLog(LogTag.MESSAGE, "Точная регулировка напряжения")
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                            model.testObject.value.objectVoltage.toFloat() - 2000
                        ) {
                            controller.latrDevice.plusVoltage()
                        }
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                            model.testObject.value.objectVoltage.toFloat() + 2000
                        ) {
                            controller.latrDevice.minusVoltage()
                        }
                    }

                    while (isTestRunning && isDevicesResponding() &&
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                        model.testObject.value.objectVoltage.toFloat() * 0.98f ||
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() * 1.02f
                    ) {
                        checkProtections()
                        view.appendOneMessageToLog(LogTag.MESSAGE, "Шаговая регулировка напряжения")
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                            model.testObject.value.objectVoltage.toFloat() * 0.98f
                        ) {
                            controller.latrDevice.plusVoltage()
                            sleep(100)
                            controller.latrDevice.stop()
                            sleep(200)
                        }
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                            model.testObject.value.objectVoltage.toFloat() * 1.02f
                        ) {
                            controller.latrDevice.minusVoltage()
                            sleep(100)
                            controller.latrDevice.stop()
                            sleep(200)
                        }
                    }
                }
            } else {

                while (isTestRunning && isDevicesResponding() &&
                    controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                    model.testObject.value.objectVoltage.toFloat() - 4000
                ) {
                    view.appendOneMessageToLog(LogTag.MESSAGE, "Грубая регулировка напряжения")
                    checkProtections()
                }

                if (isTestRunning) {
                    controller.latrDevice.presetAccurateParameters()


                    while (isTestRunning && isDevicesResponding() &&
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                        model.testObject.value.objectVoltage.toFloat() - 1000 ||
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() + 1000
                    ) {
                        checkProtections()
                        view.appendOneMessageToLog(LogTag.MESSAGE, "Точная регулировка напряжения")
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                            model.testObject.value.objectVoltage.toFloat() - 1000
                        ) {
                            controller.latrDevice.plusVoltage()
                        }
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                            model.testObject.value.objectVoltage.toFloat() + 1000
                        ) {
                            controller.latrDevice.minusVoltage()
                        }
                    }

                    while (isTestRunning && isDevicesResponding() &&
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                        model.testObject.value.objectVoltage.toFloat() ||
                        controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                        model.testObject.value.objectVoltage.toFloat() * 1.03f
                    ) {
                        checkProtections()
                        view.appendOneMessageToLog(LogTag.MESSAGE, "Шаговая регулировка напряжения")
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() <=
                            model.testObject.value.objectVoltage.toFloat()
                        ) {
                            controller.latrDevice.plusVoltage()
                            sleep(100)
                            controller.latrDevice.stop()
                            sleep(200)
                        }
                        if (controller.voltmeterDevice.getRegisterById(Avem4Model.RMS_VOLTAGE).value.toFloat() >=
                            model.testObject.value.objectVoltage.toFloat() * 1.03f
                        ) {
                            controller.latrDevice.minusVoltage()
                            sleep(100)
                            controller.latrDevice.stop()
                            sleep(200)
                        }
                    }
                }

            }

            controller.latrDevice.stop()
        }

        if (isTestRunning) {
            val timer =
                CallbackTimer(tickPeriod = 1.seconds, tickTimes = model.testObject.value.objectTime.toInt(),
                    tickJob = {
                        if (isTestRunning) {
                            controller.tableValues[0].testTime.value = it.getCurrentTicks().toString()
                            view.setExperimentProgress(it.getCurrentTicks(), model.testObject.value.objectTime.toInt())
                            listOfValuesU.add(
                                String.format(
                                    "%.2f",
                                    controller.tableValues[0].measuredVoltage.value.toDouble()
                                )
                            )
                            listOfValuesI.add(
                                String.format(
                                    "%.2f",
                                    controller.tableValues[0].measuredAmperage.value.toDouble()
                                )
                            )
                        } else {
                            it.stop()
                        }
                    })
            while (timer.isRunning) {
                checkProtections()
            }
        }

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
        if (controller.owenPrDevice.isKa1Triggered()) {
            cause = CauseDescriptor.KA1_TRIGGERED
        }
        if (controller.owenPrDevice.isKa2Triggered()) {
            cause = CauseDescriptor.KA2_TRIGGERED
        }
        if (!isDevicesResponding()) {
            cause = CauseDescriptor.DEVICES_NOT_RESPONDING
        }
        if (controller.owenPrDevice.isDoorOpened()) {
            cause = CauseDescriptor.DOOR_WAS_OPENED
        }
        if (controller.owenPrDevice.isSectionDoorOpened()) {
            cause = CauseDescriptor.SECTION_DOOR_WAS_OPENED
        }
        if (controller.isLatrInErrorMode()) {
            cause = CauseDescriptor.LATR_CONTROLLER_ERROR
        }
    }

    private fun markChannel() {
        if (
            controller.ammeterDevice.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() / 10 ||
            cause != CauseDescriptor.EMPTY
        ) {
            controller.tableValues[0].result.value = "Провал"
        } else {
            controller.tableValues[0].result.value = "Успех"
        }
    }

    private fun isChannelBad(): Boolean {
        return controller.ammeterDevice.getRegisterById(Avem7Model.AMPERAGE).value.toFloat() >= model.testObject.value.objectAmperage.toFloat() / 10
    }

    private fun isDevicesResponding() =
        controller.owenPrDevice.isResponding && controller.ammeterDevice.isResponding && controller.latrDevice.isResponding && controller.voltmeterDevice.isResponding

    override fun getNotRespondingMessageFromTest() =
        if (controller.owenPrDevice.isResponding) "" else "Owen PR (DD2)" +
                if (controller.ammeterDevice.isResponding) "" else ", Ammeter (P2)" +
                        if (controller.latrDevice.isResponding) "" else ", ATR (GV240)" +
                                if (controller.voltmeterDevice.isResponding) "" else ", Voltmeter (PV21)"
}
