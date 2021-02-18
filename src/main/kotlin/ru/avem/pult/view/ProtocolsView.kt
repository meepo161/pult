package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.StageStyle
import javafx.stage.WindowEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import ru.avem.pult.database.entities.Protocol
import ru.avem.pult.database.entities.Protocols
import ru.avem.pult.protocol.Saver.saveProtocolAsWorkbook
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.utils.openFile
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import java.io.File

class ProtocolsView : View("Протоколы") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    var tableProtocols: TableView<Protocol> by singleAssign()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(
                centerOnScreen = true
            )
            it.consume()
        }
        tableProtocols.items = mainModel.protocols
    }

    private val mainModel: MainViewModel by inject()
    private val selectedProtocol = SimpleObjectProperty<Protocol>()

    override val root = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0

        hbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
            }
            button("Назад") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT).apply {
                    glyphSize = 60
                }
                action {
                    currentWindow?.onCloseRequest?.handle(WindowEvent(currentWindow, EventType.ROOT))
                }
            }
            button("Открыть") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN_ALT).apply {
                    glyphSize = 60
                }
                action {
                    saveProtocolAsWorkbook(selectedProtocol.value)
                    openFile(File("temp.xlsx"))
                }
            }.removeWhen(selectedProtocol.isNull)
            button("Сохранить как…") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE).apply {
                    glyphSize = 60
                }
                action {
                    val files = chooseFile(
                        "Выберите директорию для сохранения",
                        arrayOf(FileChooser.ExtensionFilter("XSLX Files (*.xlsx)", "*.xlsx")),
                        FileChooserMode.Save,
                        currentWindow
                    ) {
                        this.initialDirectory = File(System.getProperty("user.home"))
                    }

                    if (files.isNotEmpty()) {
                        saveProtocolAsWorkbook(selectedProtocol.value, files.first().absolutePath)
                        confirmNotification(
                            "Готово",
                            "Успешно сохранено",
                            Pos.BOTTOM_CENTER
                        )
                    }
                }
            }.removeWhen(selectedProtocol.isNull)
            button("Сохранить все") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE).apply {
                    glyphSize = 60
                }
                action {
                    if (mainModel.protocols.size > 0) {
                        val dir = chooseDirectory(
                            "Выберите директорию для сохранения",
                            File(System.getProperty("user.home")),
                            currentWindow
                        )

                        if (dir != null) {
                            val find = find<ProtocolsSaveProgressView>()
                            find.dir = dir
                            find.openModal(
                                stageStyle = StageStyle.UNDECORATED,
                                modality = Modality.WINDOW_MODAL,
                                escapeClosesWindow = false,
                                owner = currentWindow,
                                resizable = false
                            )
                        }
                    }
                }
            }
            button("Удалить") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH).apply {
                    glyphSize = 60
                }
                removeWhen(selectedProtocol.isNull)
                action {
                    confirmation(
                        "Удаление",
                        "Вы действительно хотите удалить протокол?",
                        ButtonType("ДА"), ButtonType("НЕТ"),
                        owner = currentWindow,
                        title = "Удаление протокола"
                    ) { buttonType ->
                        if (buttonType.text == "ДА") {
                            mainModel.performActionByAdmin {
                                transaction {
                                    Protocols.deleteWhere {
                                        Protocols.id eq selectedProtocol.value.id
                                    }
                                }
                                tableProtocols.items = mainModel.protocols
                            }
                        }
                    }
                }
            }

            vbox {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
                alignment = Pos.CENTER_RIGHT

                textfield {
                    callKeyBoard()
                    maxWidth = 700.0
                    alignment = Pos.CENTER

                    promptText = "Фильтр"
                    textProperty().onChange { text ->
                        tableProtocols.items = mainModel.protocols.filtered {
                            it.objectName.contains(text!!) ||
                                    it.factoryNumber?.contains(text) ?: false ||
                                    it.date.contains(text) ||
                                    it.time.contains(text)
                        }
                    }
                }
            }
        }.addClass(Styles.hard)

        tableProtocols = tableview(mainModel.protocols) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 128.0
                bottomAnchor = 16.0
            }
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            bindSelected(selectedProtocol)
            column("Заводской номер", Protocol::factoryNumber) {
                isSortable = false
            }
            column("Объект", Protocol::objectName) {
                isSortable = false
            }
            column("Дата", Protocol::date) {
                isSortable = false
            }
            column("Время", Protocol::time) {
                isSortable = false
            }
            column("Результат", Protocol::result) {
                isSortable = false
            }
        }
    }
}
