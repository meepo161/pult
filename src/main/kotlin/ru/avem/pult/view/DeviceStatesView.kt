package ru.avem.pult.view

import javafx.geometry.Pos
import javafx.scene.shape.Circle
import ru.avem.pult.communication.model.CommunicationModel
import tornadofx.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class DeviceStatesView : View("Состояние устройств") {
    private var circlePR100: Circle by singleAssign()
    private var circleLATRController: Circle by singleAssign()
    private var circleAvemAmmeter0: Circle by singleAssign()
    private var circleAvemVoltmeter: Circle by singleAssign()

    override fun onDock() {
        thread {
            while (isDocked) {
                CommunicationModel.checkDevices()
                circlePR100.fill =
                    if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD1).isResponding) c("green") else c(
                        "red"
                    )
                circleLATRController.fill =
                    if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.GV240).isResponding) c("green") else c(
                        "red"
                    )
                circleAvemVoltmeter.fill =
                    if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.PV21).isResponding) c("green") else c(
                        "red"
                    )
                circleAvemAmmeter0.fill =
                    if (CommunicationModel.getDeviceById(CommunicationModel.DeviceID.PA11).isResponding) c("green") else c(
                        "red"
                    )
                sleep(100)
            }
        }
    }

    override val root = anchorpane {
        prefHeight = 200.0

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 48.0
                rightAnchor = 16.0
                topAnchor = 16.0
                bottomAnchor = 16.0
            }
            alignment = Pos.CENTER

            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                circlePR100 = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("БСУ ПР100")
            }

            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                circleLATRController = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("Контроллер АРН")
            }

            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                circleAvemVoltmeter = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("АВЭМ4-03")
            }

            hbox(spacing = 16.0) {
                alignment = Pos.CENTER_LEFT
                circleAvemAmmeter0 = circle(radius = 8) {
                    isSmooth = true
                    stroke = c("black")
                }
                label("АВЭМ-7 Испытательный канал - 1")
            }
        }.addClass(Styles.regularLabels)
    }
}
