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
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import ru.avem.pult.controllers.TestController
import ru.avem.pult.entities.ImpulseTableValues
import ru.avem.pult.entities.TableValues
import ru.avem.pult.utils.LogTag
import ru.avem.pult.utils.TestStateColors
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import tornadofx.*
import java.text.SimpleDateFormat

class TestView : View() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val controller: TestController by inject()
    private val mainViewModel: MainViewModel by inject()

    var vBoxLog: VBox by singleAssign()
    var buttonBack: Button by singleAssign()
    var buttonTestStart: Button by singleAssign()
    var buttonTestStop: Button by singleAssign()
    var buttonStartTimer: Button by singleAssign()
    var progressBarTime: ProgressBar by singleAssign()
    var labelExperimentName: Label by singleAssign()

    var tableContainer: HBox by singleAssign()

    var logBuffer: String? = null

    override fun onDock() {
        labelExperimentName.text = if (mainViewModel.isManualVoltageRegulation.value) {
            "Ручной режим. ${mainViewModel.test.value}"
        } else {
            "Автоматический режим. ${mainViewModel.test.value}"
        }

        when (mainViewModel.test.value) {
            TEST_1 -> {
                tableview(controller.tableValues) {
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
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
            }
            TEST_2 -> {
                tableview(controller.impulseTableValues) {
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                    prefHeight = 235.0
                    columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                    isMouseTransparent = true

                    column("Объекты", ImpulseTableValues::connection.getter)
                    column("Uзад ОИ, В", ImpulseTableValues::specifiedVoltage.getter)
                    column("КТР", ImpulseTableValues::ktr.getter)
                    column("Uизм ОИ, В", ImpulseTableValues::measuredVoltage.getter)
                    column("Iут. зад., мА", ImpulseTableValues::specifiedAmperage.getter)
                    column("Iут. изм., мА", ImpulseTableValues::measuredAmperage.getter)
                    column("Дат1, мА", ImpulseTableValues::dat1.getter)
                    column("Дат2, мА", ImpulseTableValues::dat2.getter)
                    column("Время, с", ImpulseTableValues::testTime.getter)
                    column("Результат", ImpulseTableValues::result.getter)
                }
            }
            else -> {
                null
            }
        }?.let {
            tableContainer.replaceChildren(
                it
            )
        }

        setExperimentProgress(0)
        controller.clearTable()
        controller.clearLog()
        controller.fillTableByEO()
        currentWindow?.setOnCloseRequest {
            it.consume()
        }
        super.onDock()
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
        labelExperimentName = label() {
            anchorpaneConstraints {
                leftAnchor = 0.0
                rightAnchor = 0.0
                topAnchor = 40.0
            }
            alignment = Pos.CENTER
        }.addClass(Styles.testHeaderLabels)

        vbox(spacing = 64.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 120.0
            }

            tableContainer = hbox {
                //Пустой контейнер, сюда попадет таблица из onDock()
            }

            hbox(spacing = 32.0) {
                anchorpaneConstraints {
                    leftAnchor = 0.0
                    rightAnchor = 16.0
                    topAnchor = 300.0
                }

                alignment = Pos.CENTER

                buttonBack = button("Назад") {
                    graphic = OctIconView(OctIcon.ARROW_LEFT).apply {
                        glyphSize = 60.0
                    }
                    prefWidth = 400.0
                    prefHeight = 120.0
                    onAction = EventHandler {
                        replaceWith<MainView>()
                    }
                }.addClass(Styles.superHard)

                buttonTestStart = button("Запуск") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLAY).apply {
                        glyphSize = 60.0
                        fill = c("green")
                    }
                    prefWidth = 400.0
                    prefHeight = 120.0
                    action {
                        isDisable = true
                        controller.initTest()
                    }
                }.addClass(Styles.superHard)

                buttonTestStop = button("Стоп") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.STOP).apply {
                        glyphSize = 60.0
                        fill = c("red")
                    }
                    isDisable = true
                    prefWidth = 400.0
                    prefHeight = 120.0
                    action {
                        isDisable = true
                        controller.stopTest()
                    }
                }.addClass(Styles.superHard)

                buttonStartTimer = button("Таймер") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.CLOCK_ALT).apply {
                        glyphSize = 60.0
                        fill = c("#039dfc")
                    }
                    prefWidth = 400.0
                    prefHeight = 120.0
                    isDisable = true
                    action {
                        isDisable = true
                        controller.isTimerStart = true
                    }
                }.removeWhen(mainViewModel.isManualVoltageRegulation.not()).addClass(Styles.superHard)
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