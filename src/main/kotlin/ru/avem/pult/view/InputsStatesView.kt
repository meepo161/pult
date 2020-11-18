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
import tornadofx.*
import kotlin.concurrent.thread

class InputsStatesView : View("Состояние входов БСУ") {
    private var connection1Relay: Circle by singleAssign()
    private var connection2Relay: Circle by singleAssign()
    private var connection3Relay: Circle by singleAssign()
    private var connection4Relay: Circle by singleAssign()
    private var bathDoor: Circle by singleAssign()
    private var bathDoorSwitch: Circle by singleAssign()
    private var buttonStartPump: Circle by singleAssign()

    override fun onDock() {
        thread {
            CommunicationModel.startPoll(CommunicationModel.DeviceID.DD1, OwenPrModel.DI_01_16_RAW) {}
            val dd1 = CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD1)
                .getRegisterById(OwenPrModel.DI_01_16_RAW)
            while (isDocked) {
                if (dd1.value.toInt().getRange(0).toBoolean()) {
                    connection1Relay.fill = c("green")
                } else {
                    connection1Relay.fill = c("red")
                }
                if (dd1.value.toInt().getRange(1).toBoolean()) {
                    connection2Relay.fill = c("green")
                } else {
                    connection2Relay.fill = c("red")
                }
                if (dd1.value.toInt().getRange(2).toBoolean()) {
                    connection3Relay.fill = c("green")
                } else {
                    connection3Relay.fill = c("red")
                }
                if (dd1.value.toInt().getRange(3).toBoolean()) {
                    connection4Relay.fill = c("green")
                } else {
                    connection4Relay.fill = c("red")
                }
                if (dd1.value.toInt().getRange(4).toBoolean()) {
                    bathDoor.fill = c("green")
                } else {
                    bathDoor.fill = c("red")
                }
                if (dd1.value.toInt().getRange(5).toBoolean()) {
                    bathDoorSwitch.fill = c("green")
                } else {
                    bathDoorSwitch.fill = c("red")
                }
                if (dd1.value.toInt().getRange(6).toBoolean()) {
                    buttonStartPump.fill = c("green")
                } else {
                    buttonStartPump.fill = c("red")
                }
            }
            CommunicationModel.removePollingRegister(CommunicationModel.DeviceID.DD1, OwenPrModel.DI_01_16_RAW)
        }
    }

    override val root = hbox(spacing = 16.0) {
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
