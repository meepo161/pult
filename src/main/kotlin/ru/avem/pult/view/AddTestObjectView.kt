package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.scene.control.ComboBox
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.TestObject
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.TestObjectAddViewModel
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import tornadofx.*

class AddTestObjectView : View("Создание ОИ") {
    private var experiments: ComboBox<String> by singleAssign()
    private var transformators: ComboBox<String> by singleAssign()

    private val parentView: TestObjectView by inject()

    private val mainViewModel: MainViewModel by inject()
    private var model = TestObjectAddViewModel()
    private val validationCtx = ValidationContext()

    override fun onDock() {
        validationCtx.validate()
        model.amperage.value = .0
        model.test.value = ""
        model.name.value = ""
        model.time.value = 0
    }

    override val root = form {
        fieldset("Заполните поля") {
            field("Испытание:") {
                experiments = combobox {
                    useMaxWidth = true
                    items = mainViewModel.testList
                    validationCtx.addValidator(this, property = model.test) {
                        if (it.isNullOrBlank()) error("Обязательное поле") else null
                    }
                    bind(model.test)
                }
            }

            field("Наименование ОИ:") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 255
                    }
                    prefWidth = 300.0
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) {
                            error("Обязательное поле")
                        } else null
                    }
                    bind(model.name)
                }
            }
            field("Устанавливаемое напряжение, В:") {
                textfield {
                    callKeyBoard()
                    prefWidth = 300.0
                    filterInput {
                        it.controlNewText.length <= 6 && it.controlNewText.isInt()
                    }
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) {
                            error("Обязательное поле")
                        } else {
                            when (model.test.value) {
                                TEST_1 -> {
                                    if ((0..TYPE_1_VOLTAGE).contains(it.toInt())) {
                                        null
                                    } else {
                                        error("Значение напряжения должно быть в диапазоне 0..$TYPE_1_VOLTAGE")
                                    }
                                }
                                TEST_2 -> {
                                    if ((0..TYPE_2_VOLTAGE).contains(it.toInt())) {
                                        null
                                    } else {
                                        error("Значение напряжения должно быть в диапазоне 0..$TYPE_2_VOLTAGE")
                                    }
                                }
                                else -> {
                                    error("Выберите испытание")
                                }
                            }
                        }
                    }
                    stripNonNumeric(allowedChars = emptyArray())
                    bind(model.voltage)
                }
            }
            field("Ток утечки, мА:") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 6 && it.controlNewText.isDouble()
                    }
                    prefWidth = 300.0
                    stripNonNumeric(allowedChars = arrayOf("."))
                    bind(model.amperage)
                }
            }
            field("Время испытания, с:") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 3
                    }
                    prefWidth = 300.0
                    stripNonNumeric(allowedChars = emptyArray())
                    bind(model.time)
                }
            }

            buttonbar {
                button("Добавить") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                        glyphSize = 18
                    }
                    action {
                        transaction {
                            TestObject.new {
                                objectName = model.name.value
                                objectVoltage = model.voltage.value.toString()
                                objectAmperage = model.amperage.value.toString()
                                objectTime = model.time.value.toString()
                                objectTransformer = when (model.test.value) {
                                    TEST_1 -> {
                                        TYPE_1_VOLTAGE.toString()
                                    }
                                    TEST_2 -> {
                                        TYPE_2_VOLTAGE.toString()
                                    }
                                    else -> {
                                        TYPE_1_VOLTAGE.toString()
                                    }
                                }
                                objectTest = model.test.value
                            }
                        }
                        parentView.objectsTable.items = mainViewModel.testObjectsList
                    }
                }

                button("Добавить и закрыть") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE).apply {
                        glyphSize = 18
                    }
                    action {
                        transaction {
                            TestObject.new {
                                objectName = model.name.value
                                objectAmperage = model.amperage.value.toString()
                                objectTime = model.time.value.toString()
                                objectTransformer = when (model.test.value) {
                                    TEST_1 -> {
                                        TYPE_1_VOLTAGE.toString()
                                    }
                                    TEST_2 -> {
                                        TYPE_2_VOLTAGE.toString()
                                    }
                                    else -> {
                                        TYPE_1_VOLTAGE.toString()
                                    }
                                }
                                objectVoltage = model.voltage.value.toString()
                                objectTest = model.test.value
                            }
                        }
                        parentView.objectsTable.items = mainViewModel.testObjectsList
                        close()
                    }
                }
            }.visibleWhen(validationCtx.valid)
        }.addClass(Styles.regularLabels)
    }
}
