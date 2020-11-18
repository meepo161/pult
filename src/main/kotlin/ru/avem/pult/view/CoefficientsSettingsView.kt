package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.ActionEvent
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.stage.WindowEvent
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.CoefficientSettingsFragmentModel
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_1_FRAGMENT
import ru.avem.pult.viewmodels.CoefficientsSettingsViewModel.Companion.MODULE_2_FRAGMENT
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.*
import tornadofx.controlsfx.errorNotification
import java.io.File
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class CoefficientsSettingsView : View("Настройки измерений") {
    private val model = CoefficientsSettingsViewModel()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(
                centerOnScreen = true
            )
            it.consume()
        }

    }

    override val root = vbox(spacing = 8.0) {
        prefWidth = 1366.0
        prefHeight = 768.0

        hbox(spacing = 64) {
            paddingTop = 8.0
            paddingLeft = 8.0
            button("Назад") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT).apply {
                    glyphSize = 18
                }
                action {
                    currentWindow?.onCloseRequest?.handle(WindowEvent(currentWindow, EventType.ROOT))
                }
            }
            button("Сохранить") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.SAVE).apply {
                    glyphSize = 18
                }
                action {
                    if (model.isDataValid) {
                        val ctx = JAXBContext.newInstance(CoefficientSettingsFragmentModel::class.java)
                        val marshaller = ctx.createMarshaller()
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                        with(File("coef")) {
                            if (this.isDirectory) {
                                model.fragments.forEach { (t, u) ->
                                    marshaller.marshal(u.model, File("coef/$t.xml"))
                                }
                            } else {
                                this.mkdir()
                                this@button.onAction.handle(ActionEvent())
                            }
                        }
                    } else {
                        errorNotification(
                            "Ошибка",
                            "Проверьте заполнение полей и повторите снова",
                            Pos.BOTTOM_CENTER
                        )
                    }
                }
            }
        }

        tabpane {
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            tab("$TYPE_1_VOLTAGE В") {
                content = model.fragments.getValue(MODULE_1_FRAGMENT).root
            }
            tab("$TYPE_2_VOLTAGE В") {
                content = model.fragments.getValue(MODULE_2_FRAGMENT).root
            }
        }.addClass(Styles.regularLabels)
    }

    class FormFragment : Fragment() {
        val model = CoefficientSettingsFragmentModel()
        override val root = form {
            fieldset("Коэффициенты") {
                field("Напряжение на АРНе (В):") {
                    textfield {
                        callKeyBoard()
                        stripNonNumeric(".")
                        filterInput {
                            it.controlNewText.length <= 6
                        }
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) error("Обязательное поле") else null
                        }
                    }.bind(model.latr)
                }
                field("Напряжение с отвода (В):") {
                    textfield {
                        callKeyBoard()
                        stripNonNumeric(".")
                        filterInput {
                            it.controlNewText.length <= 6
                        }
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) error("Обязательное поле") else null
                        }
                    }.bind(model.tap)
                }
                field("Напряжение на объекте (В):") {
                    textfield {
                        callKeyBoard()
                        stripNonNumeric(".")
                        filterInput {
                            it.controlNewText.length <= 9
                        }
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) error("Обязательное поле") else null
                        }
                    }.bind(model.obj)
                }
            }
        }
    }
}
