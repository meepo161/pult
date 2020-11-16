package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.WindowEvent
import ru.avem.pult.database.entities.ProtocolField
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import java.nio.file.Path
import java.nio.file.Paths

class ProtocolSettingsView : View("Настройки протокола") {
    override val configPath: Path = Paths.get("conf/.properties")

    private val model: MainViewModel by inject()
    private val selectedField = SimpleObjectProperty<ProtocolField>()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(
                centerOnScreen = true
            )
            it.consume()
        }
    }

    override val root = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0
        hbox(spacing = 32.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 16.0
            }
            button("Назад") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT).apply {
                    glyphSize = 18
                }
                action {
                    currentWindow?.onCloseRequest?.handle(WindowEvent(currentWindow, EventType.ROOT))
                }
            }
            button("Добавить ключ") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                    glyphSize = 18
                }
                action {
                    find<AddProtocolKeyView>().openModal()
                }
            }
            button("Удалить ключ") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH).apply {
                    glyphSize = 18
                }
            }.removeWhen(selectedField.isNull)
            hbox {
                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
                alignment = Pos.CENTER_RIGHT

                button("Показать системные ключи") {
                    anchorpaneConstraints {
                        rightAnchor = 16.0
                        topAnchor = 16.0
                    }
                    graphic = FontAwesomeIconView(FontAwesomeIcon.LIST).apply {
                        glyphSize = 18
                    }
                    action {
                        replaceWith<ProtocolSystemKeysView>(
                            centerOnScreen = true
                        )
                    }
                }
            }
        }
        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 64.0
                bottomAnchor = 16.0
            }
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER
                label("Шаблон протокола")
                textfield(model.protocolTemplatePath) {
                    callKeyBoard()
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                    isMouseTransparent = true
                    isEditable = false
                }
                button("Обзор") {
                    action {
                        val template = chooseFile(
                            "Укажите путь к шаблону протокола",
                            arrayOf(FileChooser.ExtensionFilter("Таблицы Microsoft Excel", "*.xlsx"))
                        )
                        if (template.isNotEmpty()) {
                            model.protocolTemplatePath.value = template.first().absolutePath
                            config["template_path"] = template.first().absolutePath
                            config.save()
                        }
                    }
                }
            }
            tableview(model.protocolFields) {
                vboxConstraints {
                    vGrow = Priority.ALWAYS
                }
                columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                placeholder = Text("Добавьте ключ")
                enableCellEditing()
                bindSelected(selectedField)
                column("Ключ", ProtocolField::fieldKey).fixedWidth(200).makeEditable()
                column("Значение", ProtocolField::fieldValue).makeEditable()
            }
        }
    }.addClass(Styles.regularLabels)
}
