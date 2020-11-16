package ru.avem.pult.view

import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.shape.Circle
import javafx.scene.text.FontWeight
import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.pult.communication.utils.toBoolean
import ru.avem.pult.utils.getRange
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_3_VOLTAGE
import tornadofx.*
import kotlin.concurrent.thread

class InputsStatesView : View("Состояние входов БСУ") {
    private var generalAmmeterRelay: Circle by singleAssign()
    private var buttonStop: Circle by singleAssign()
    private var contactorARN: Circle by singleAssign()
    private var rst: Circle by singleAssign()
    private var shortlocker20kVon: Circle by singleAssign()
    private var shortlocker20kVoff: Circle by singleAssign()
    private var zoneDoor: Circle by singleAssign()
    private var hiSwitch: Circle by singleAssign()
    private var contactor200V: Circle by singleAssign()
    private var contactor20kV: Circle by singleAssign()
    private var contactor50kV: Circle by singleAssign()
    private var connection1Relay: Circle by singleAssign()
    private var connection2Relay: Circle by singleAssign()
    private var connection3Relay: Circle by singleAssign()
    private var connection4Relay: Circle by singleAssign()
    private var bathDoor: Circle by singleAssign()
    private var bathDoorSwitch: Circle by singleAssign()
    private var buttonStartPump: Circle by singleAssign()

    override fun onDock() {
        thread {
            CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.DI_01_16_RAW) {}
            CommunicationModel.startPoll(CommunicationModel.DeviceID.DD3, OwenPrModel.DI_01_16_RAW) {}
            val dd2 = CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2)
                .getRegisterById(OwenPrModel.DI_01_16_RAW)
            val dd3 = CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD3)
                .getRegisterById(OwenPrModel.DI_01_16_RAW)
            while (isDocked) {
                if (dd2.value.toInt().getRange(0).toBoolean()) {
                    generalAmmeterRelay.fill = c("green")
                } else {
                    generalAmmeterRelay.fill = c("red")
                }
                if (dd2.value.toInt().getRange(1).toBoolean()) {
                    buttonStop.fill = c("green")
                } else {
                    buttonStop.fill = c("red")
                }
                if (dd2.value.toInt().getRange(2).toBoolean()) {
                    contactorARN.fill = c("green")
                } else {
                    contactorARN.fill = c("red")
                }
                if (dd2.value.toInt().getRange(3).toBoolean()) {
                    rst.fill = c("green")
                } else {
                    rst.fill = c("red")
                }
                if (dd2.value.toInt().getRange(4).toBoolean()) {
                    shortlocker20kVon.fill = c("green")
                } else {
                    shortlocker20kVon.fill = c("red")
                }
                if (dd2.value.toInt().getRange(5).toBoolean()) {
                    shortlocker20kVoff.fill = c("green")
                } else {
                    shortlocker20kVoff.fill = c("red")
                }
                if (dd2.value.toInt().getRange(7).toBoolean()) {
                    zoneDoor.fill = c("green")
                } else {
                    zoneDoor.fill = c("red")
                }
                if (dd2.value.toInt().getRange(8).toBoolean()) {
                    hiSwitch.fill = c("green")
                } else {
                    hiSwitch.fill = c("red")
                }
                if (dd2.value.toInt().getRange(9).toBoolean()) {
                    contactor200V.fill = c("green")
                } else {
                    contactor200V.fill = c("red")
                }
                if (dd2.value.toInt().getRange(10).toBoolean()) {
                    contactor20kV.fill = c("green")
                } else {
                    contactor20kV.fill = c("red")
                }
                if (dd2.value.toInt().getRange(11).toBoolean()) {
                    contactor50kV.fill = c("green")
                } else {
                    contactor50kV.fill = c("red")
                }

                if (dd3.value.toInt().getRange(0).toBoolean()) {
                    connection1Relay.fill = c("green")
                } else {
                    connection1Relay.fill = c("red")
                }
                if (dd3.value.toInt().getRange(1).toBoolean()) {
                    connection2Relay.fill = c("green")
                } else {
                    connection2Relay.fill = c("red")
                }
                if (dd3.value.toInt().getRange(2).toBoolean()) {
                    connection3Relay.fill = c("green")
                } else {
                    connection3Relay.fill = c("red")
                }
                if (dd3.value.toInt().getRange(3).toBoolean()) {
                    connection4Relay.fill = c("green")
                } else {
                    connection4Relay.fill = c("red")
                }
                if (dd3.value.toInt().getRange(4).toBoolean()) {
                    bathDoor.fill = c("green")
                } else {
                    bathDoor.fill = c("red")
                }
                if (dd3.value.toInt().getRange(5).toBoolean()) {
                    bathDoorSwitch.fill = c("green")
                } else {
                    bathDoorSwitch.fill = c("red")
                }
                if (dd3.value.toInt().getRange(6).toBoolean()) {
                    buttonStartPump.fill = c("green")
                } else {
                    buttonStartPump.fill = c("red")
                }
            }
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.DD2, OwenPrModel.DI_01_16_RAW)
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.DD3, OwenPrModel.DI_01_16_RAW)
        }
    }

    override val root = hbox(spacing = 16.0) {
        vbox(spacing = 16.0) {
            hboxConstraints {
                margin = Insets(16.0, 0.0, 16.0, 16.0)
            }
            label("Овен ПР-102 DD2") {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }
            alignment = Pos.CENTER

            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                generalAmmeterRelay = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Реле амперметра штанг")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                buttonStop = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("СТОП")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                contactorARN = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КМ АРН")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                rst = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("ТКЗ")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                shortlocker20kVon = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КЗ $TYPE_2_VOLTAGE вкл")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                shortlocker20kVoff = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КЗ $TYPE_2_VOLTAGE выкл")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                zoneDoor = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Дверь зоны")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                hiSwitch = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Видимый разрыв")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                contactor200V = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КМ $TYPE_1_VOLTAGE В")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                contactor20kV = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КМ $TYPE_2_VOLTAGE В")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                contactor50kV = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("КМ $TYPE_3_VOLTAGE В")
            }
        }.addClass(Styles.regularLabels)
        separator(Orientation.VERTICAL)
        vbox(spacing = 16.0) {
            hboxConstraints {
                margin = Insets(16.0, 16.0, 16.0, 16.0)
            }
            label("Овен ПР-100 DD3") {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                connection1Relay = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Реле точки 1")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                connection2Relay = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Реле точки 2")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                connection3Relay = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Реле точки 3")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                connection4Relay = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Реле точки 4")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                bathDoor = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Люк модуля")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                bathDoorSwitch = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Концевик люка")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                buttonStartPump = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Кнопка Пуск насоса")
            }
        }.addClass(Styles.regularLabels)
    }
}
