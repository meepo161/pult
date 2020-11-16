package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import de.jensd.fx.glyphs.octicons.OctIcon
import de.jensd.fx.glyphs.octicons.OctIconView
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import ru.avem.pult.controllers.TestController
import ru.avem.pult.entities.TableValues
import ru.avem.pult.utils.LogTag
import ru.avem.pult.utils.TestStateColors
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_7
import tornadofx.*
import java.text.SimpleDateFormat

class TestView : View() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val controller: TestController by inject()
    private val mainViewModel: MainViewModel by inject()

    var vBoxLog: VBox by singleAssign()
    var circleComStatus: Circle by singleAssign()
    var buttonBack: Button by singleAssign()
    var buttonTestStart: Button by singleAssign()
    var buttonTestStop: Button by singleAssign()
    var buttonFixLighting: Button by singleAssign()
    var buttonOpenBathDoor: Button by singleAssign()
    var progressBarTime: ProgressBar by singleAssign()
    var labelExperimentName: Label by singleAssign()

    var logBuffer: String? = null

    override fun onDock() {
        title = mainViewModel.test.value
        setExperimentProgress(0)
        controller.clearTable()
        controller.clearLog()
        controller.fillTableByEO()
        currentWindow?.setOnCloseRequest {
            it.consume()
        }
    }

    fun setExperimentProgress(currentTime: Int, time: Int = 1) {
        Platform.runLater {
            progressBarTime.progress = currentTime.toDouble() / time
        }
    }

    fun setTestStatusColor(state: TestStateColors) {
        progressBarTime.style {
            baseColor = state.c
        }
    }

    private val topSide = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0
        labelExperimentName = label(mainViewModel.test) {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                topAnchor = 40.0
            }
            alignment = Pos.CENTER
        }.addClass(Styles.testHeaderLabels)

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 120.0
            }
            tableview(controller.tableValues) {
                prefHeight = 235.0
                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                isMouseTransparent = true

                column("Объекты", TableValues::connection.getter)
                column("Uзад ОИ, В", TableValues::specifiedVoltage.getter)
                column("КТР", TableValues::ktr.getter)
                column("Uизм ОИ, В", TableValues::measuredVoltage.getter)
                column("Iут. зад. мА", TableValues::specifiedAmperage.getter)
                column("Iут. изм. мА", TableValues::measuredAmperage.getter)
                column("Время, с", TableValues::testTime.getter)
                column("Результат", TableValues::result.getter)
            }

            hbox(spacing = 130.0) {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 16.0
                    topAnchor = 300.0
                }

                alignment = Pos.CENTER

                buttonBack = button("Назад") {
                    graphic = OctIconView(OctIcon.ARROW_LEFT).apply {
                        glyphSize = 35.0
                    }
                    prefWidth = 200.0

                    scaleX = 1.5
                    scaleY = 1.5

                    onAction = EventHandler {
                        replaceWith<MainView>()
                    }
                }

                buttonTestStart = button("Запуск") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLAY).apply {
                        glyphSize = 35.0
                        fill = c("green")
                    }
                    prefWidth = 200.0
                    scaleX = 1.5
                    scaleY = 1.5
                    action {
                        isDisable = true
                        controller.initTest()
                    }
                }

                buttonTestStop = button("Остановить") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.STOP).apply {
                        glyphSize = 35.0
                        fill = c("red")
                    }

                    prefWidth = 200.0
                    scaleX = 1.5
                    scaleY = 1.5
                    action {
                        isDisable = true
                        controller.stopTest()
                    }
                }

                buttonFixLighting = button("Зафиксировать") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.WRENCH).apply {
                        glyphSize = 35.0
                        fill = c("#039dfc")
                    }
                    prefWidth = 200.0
                    scaleX = 1.5
                    scaleY = 1.5
                    isDisable = true
                    action {
                        buttonTestStart.isDisable = true
                        buttonTestStop.isDisable = true
                        isDisable = true
                        controller.isLightingFixed = true
                    }
                }.removeWhen(
                    mainViewModel.test.isNotEqualTo(TEST_2) and
                            mainViewModel.test.isNotEqualTo(TEST_7)
                )

                button("Разблок. люк") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.UNLOCK).apply {
                        glyphSize = 35.0
                        fill = c("red")
                    }
                    prefWidth = 200.0
                    scaleX = 1.5
                    scaleY = 1.5
                    isDisable = true
                    action {
                        //todo Кнопка нужна в испытаниях с ванной
                    }
                }.removeFromParent()
            }
        }
    }

    private val bottomSide = anchorpane {
        tabpane {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                topAnchor = 0.0
                bottomAnchor = 0.0
            }
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("Ход испытания") {
                anchorpane {
                    scrollpane {
                        anchorpaneConstraints {
                            leftAnchor = 16.0
                            rightAnchor = 16.0
                            topAnchor = 16.0
                            bottomAnchor = 16.0
                        }

                        vBoxLog = vbox { }.addClass(Styles.regularLabels)
                        vvalueProperty().bind(vBoxLog.heightProperty())
                    }
                }
            }
        }
    }

    override val root = borderpane {
        center = splitpane(Orientation.VERTICAL, topSide, bottomSide) {
            prefWidth = 1200.0
            prefHeight = 700.0

            setDividerPositions(0.6)
        }

        bottom = anchorpane {
//            label("Связь:") {
//                anchorpaneConstraints {
//                    leftAnchor = 16.0
//                    topAnchor = 4.0
//                    bottomAnchor = 4.0
//                }
//            }
//
//            circleComStatus = circle(radius = 8.0) {
//                anchorpaneConstraints {
//                    leftAnchor = 70.0
//                    topAnchor = 9.0
//                    bottomAnchor = 2.0
//                }
//
//                stroke = c("black")
//            }

            label("Прогресс испытания:") {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    topAnchor = 4.0
                    bottomAnchor = 4.0
                }
            }

            progressBarTime = progressbar {
                anchorpaneConstraints {
                    leftAnchor = 190.0
                    rightAnchor = 16.0
                    topAnchor = 6.0
                    bottomAnchor = 4.0
                }
                progress = 0.0
                style {
                    baseColor = TestStateColors.GO.c
                }
            }
        }.addClass(Styles.anchorPaneBorders)
    }

    fun appendMessageToLog(tag: LogTag, _msg: String) {
        val msg = Text("${SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis())} | $_msg")
        msg.style {
            fill =
                when (tag) {
                    LogTag.MESSAGE -> tag.c
                    LogTag.ERROR -> tag.c
                    LogTag.DEBUG -> tag.c
                }
        }

        Platform.runLater {
            vBoxLog.add(msg)
        }
    }

    fun appendOneMessageToLog(tag: LogTag, message: String) {
        if (logBuffer == null || logBuffer != message) {
            logBuffer = message
            appendMessageToLog(tag, message)
        }
    }
}