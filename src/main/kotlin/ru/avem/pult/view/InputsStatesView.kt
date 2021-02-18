package ru.avem.pult.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.shape.Circle
import javafx.scene.text.FontWeight
import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.pult.communication.utils.toBoolean
import ru.avem.pult.utils.getRange
import tornadofx.*
import kotlin.concurrent.thread

class InputsStatesView : View("Состояние входов БСУ") {
    private var km1State: Circle by singleAssign()
    private var km2State: Circle by singleAssign()
    private var km3State: Circle by singleAssign()
    private var doorState: Circle by singleAssign()
    private var ka1State: Circle by singleAssign()
    private var ka2State: Circle by singleAssign()
    private var km5State: Circle by singleAssign()

    override fun onDock() {
        thread {
            CommunicationModel.startPoll(CommunicationModel.DeviceID.DD1, OwenPrModel.DI_01_16_RAW) {}
            val dd1 = CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD1)
                .getRegisterById(OwenPrModel.DI_01_16_RAW)
            while (isDocked) {
                if (dd1.value.toInt().getRange(1).toBoolean()) {
                    km1State.fill = c("green")
                } else {
                    km1State.fill = c("red")
                }
                if (dd1.value.toInt().getRange(2).toBoolean()) {
                    km2State.fill = c("green")
                } else {
                    km2State.fill = c("red")
                }
                if (dd1.value.toInt().getRange(3).toBoolean()) {
                    km3State.fill = c("green")
                } else {
                    km3State.fill = c("red")
                }
                if (dd1.value.toInt().getRange(4).toBoolean()) {
                    doorState.fill = c("green")
                } else {
                    doorState.fill = c("red")
                }
                if (dd1.value.toInt().getRange(5).toBoolean()) {
                    ka1State.fill = c("green")
                } else {
                    ka1State.fill = c("red")
                }
                if (dd1.value.toInt().getRange(6).toBoolean()) {
                    ka2State.fill = c("green")
                } else {
                    ka2State.fill = c("red")
                }
                if (dd1.value.toInt().getRange(7).toBoolean()) {
                    km5State.fill = c("green")
                } else {
                    km5State.fill = c("red")
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
            label("Овен ПР-200 DD1") {
                style {
                    fontWeight = FontWeight.BOLD
                }
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                km1State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КМ1")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                km2State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КМ2")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                km3State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КМ3")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                doorState = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль двери")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                ka1State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КА1")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                ka2State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КА2")
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                km5State = circle(radius = 32) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроль КМ5")
            }
        }.addClass(Styles.hard)
    }
}
