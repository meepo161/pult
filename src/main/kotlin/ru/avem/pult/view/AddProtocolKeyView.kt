package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.ProtocolField
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.AddProtocolKeyModel
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class AddProtocolKeyView : View("Добавить ключ") {
    private val reservedKeys = listOf(
        "#PROTOCOL_NUMBER#", "#OBJECT#", "#SERIAL_NUMBER#",
        "#U_OBJ_1#", "#I_SET_1#", "#U_SET_1#", "#I_OBJ_1#", "#TIME_1#", "#RESULT_1#",
        "#U_OBJ_2#", "#I_SET_2#", "#U_SET_2#", "#I_OBJ_2#", "#TIME_2#", "#RESULT_2#",
        "#U_OBJ_3#", "#I_SET_3#", "#U_SET_3#", "#I_OBJ_3#", "#TIME_3#", "#RESULT_3#",
        "#U_OBJ_4#", "#I_SET_4#", "#U_SET_4#", "#I_OBJ_4#", "#TIME_4#", "#RESULT_4#",
        "#POS_1_NAME#", "#DATE#"
    )

    private val mainViewModel: MainViewModel by inject()
    private val model = AddProtocolKeyModel()
    private val validationCtx = ValidationContext()

    override fun onDock() {
        model.key.value = ""
        model.value.value = ""
        validationCtx.validate()
    }

    override val root = vbox(spacing = 16.0) {
        prefWidth = 500.0
        paddingAll = 16
        form {
            fieldset("Заполните поля") {
                field("Ключ") {
                    textfield {
                        callKeyBoard()
                        validationCtx.addValidator(this) {
                            when {
                                it.isNullOrBlank() -> error("Обязательное поле")
                                reservedKeys.contains(it) -> error("Данный ключ уже определен как системный")
                                else -> null
                            }
                        }
                    }.bind(model.key)
                }
                field("Значение") {
                    textfield {
                        callKeyBoard()
                        validationCtx.addValidator(this) {
                            if (it.isNullOrBlank()) error("Обязательное поле") else null
                        }
                    }.bind(model.value)
                }
            }
        }

        buttonbar {
            button("Добавить") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                    glyphSize = 18
                }
                action {
                    mainViewModel.protocolFields.forEach {
                        if (it.fieldKey == model.key.value) {
                            warningNotification(
                                "Ошибка",
                                "Не удалось добавить ключ. Ключ ${model.key.value} уже существует"
                            )
                            return@action
                        }
                    }
                    mainViewModel.protocolFields.add(
                        transaction {
                            ProtocolField.new {
                                fieldKey = model.key.value
                                fieldValue = model.value.value.toString()
                            }
                        }
                    )
                }
            }

            button("Добавить и закрыть") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE).apply {
                    glyphSize = 18
                }
                action {
                    mainViewModel.protocolFields.forEach {
                        if (it.fieldKey == model.key.value) {
                            warningNotification(
                                "Ошибка",
                                "Не удалось добавить ключ. Ключ ${model.key.value} уже существует"
                            )
                            return@action
                        }
                    }
                    mainViewModel.protocolFields.add(
                        transaction {
                            ProtocolField.new {
                                fieldKey = model.key.value
                                fieldValue = model.value.value.toString()
                            }
                        }
                    )
                    close()
                }
            }
        }.visibleWhen(validationCtx.valid)
    }.addClass(Styles.regularLabels)
}
