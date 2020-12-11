package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.ActionEvent
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.stage.WindowEvent
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.LatrSettingsFragmentModel
import ru.avem.pult.viewmodels.LatrSettingsViewModel
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_LAST_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U11_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U13_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U15_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U17_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U1_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U3_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U5_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE2_U8_FRAGMENT
import ru.avem.pult.viewmodels.LatrSettingsViewModel.Companion.MODULE_1_FRAGMENT
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.*
import tornadofx.controlsfx.errorNotification
import java.io.File
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller

class LatrSettingsView : View("Настройки АРН") {
    private val model = LatrSettingsViewModel()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(
                centerOnScreen = true
            )
            it.consume()
        }
    }

    override val root = vbox(spacing = 8.0) {
        hbox(spacing = 32) {
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
                        val ctx = JAXBContext.newInstance(LatrSettingsFragmentModel::class.java)
                        val marshaller = ctx.createMarshaller()
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true)
                        with(File("latr")) {
                            if (this.isDirectory) {
                                model.fragments.forEach { (t, u) ->
                                    marshaller.marshal(u.model, File("latr/$t.xml"))
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
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            side = Side.LEFT
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

            tab("$TYPE_2_VOLTAGE В") {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("U ≤ ${TYPE_2_VOLTAGE}В") {
                        content = model.fragments.getValue(MODULE_1_FRAGMENT).root
                    }
                }
            }
            tab("$TYPE_1_VOLTAGE В") {
                tabpane {
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    tab("U ≤ 1кВ") {
                        content = model.fragments.getValue(MODULE2_U1_FRAGMENT).root
                    }
                    tab("U ≤ 3кВ") {
                        content = model.fragments.getValue(MODULE2_U3_FRAGMENT).root
                    }
                    tab("U ≤ 5кВ") {
                        content = model.fragments.getValue(MODULE2_U5_FRAGMENT).root
                    }
                    tab("U ≤ 8кВ") {
                        content = model.fragments.getValue(MODULE2_U8_FRAGMENT).root
                    }
                    tab("U ≤ 11кВ") {
                        content = model.fragments.getValue(MODULE2_U11_FRAGMENT).root
                    }
                    tab("U ≤ 13кВ") {
                        content = model.fragments.getValue(MODULE2_U13_FRAGMENT).root
                    }
                    tab("U ≤ 15кВ") {
                        content = model.fragments.getValue(MODULE2_U15_FRAGMENT).root
                    }
                    tab("U ≤ 17кВ") {
                        content = model.fragments.getValue(MODULE2_U17_FRAGMENT).root
                    }
                    tab("U ≤ ${TYPE_2_VOLTAGE / 1000}кВ") {
                        content = model.fragments.getValue(MODULE2_LAST_FRAGMENT).root
                    }
                }
            }
        }.addClass(Styles.regularLabels)
    }

    class FormFragment : Fragment() {
        var model = LatrSettingsFragmentModel()

        override fun onDock() {
            model.validationCtx.validate()
        }

        override val root = form {
            fieldset {
                field("Шим грубой регулировки (%):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 3
                        }
                        stripNonNumeric("")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((1..100).contains(it.toInt())) null else error("Значение должно быть в диапазоне 1..100")
                            }
                        }
                    }.bind(model.maxDutty)
                }
                field("Шим точной регулировки (%):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 3
                        }
                        stripNonNumeric("")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((1..100).contains(it.toInt())) null else error("Значение должно быть в диапазоне 1..100")
                            }
                        }
                    }.bind(model.minDutty)
                }
                field("Корридор (%):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 4 && it.controlNewText.isDouble()
                        }
                        stripNonNumeric(".")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((0.01..0.99).contains(it.toDouble())) null else error("Значение должно быть в диапазоне 0.01..0.99")
                            }
                        }
                    }.bind(model.corridor)
                }
                field("Точность (%):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 4 && it.controlNewText.isDouble()
                        }
                        stripNonNumeric(".")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((0.01..0.99).contains(it.toDouble())) null else error("Значение должно быть в диапазоне 0.01..0.99")
                            }
                        }
                    }.bind(model.delta)
                }
                field("Время подачи ШИМа в грубой регулировке (мс.):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 3
                        }
                        stripNonNumeric("")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((5..600).contains(it.toInt())) null else error("Значение должно быть в диапазоне 5..600")
                            }
                        }
                    }.bind(model.timeMaxPulse)
                }
                field("Время подачи ШИМа в точной регулировке (мс.):") {
                    textfield {
                        callKeyBoard()
                        filterInput {
                            it.controlNewText.length <= 3
                        }
                        stripNonNumeric("")
                        model.validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) {
                                error("Обязательное значение")
                            } else {
                                if ((5..600).contains(it.toInt())) null else error("Значение должно быть в диапазоне 5..600")
                            }
                        }
                    }.bind(model.timeMinPulse)
                }
            }
        }
    }

}
