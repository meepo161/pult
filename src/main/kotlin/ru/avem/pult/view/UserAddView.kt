package ru.avem.pult.view

import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.User
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.UserAddViewModel
import tornadofx.*

class UserAddView : View("Добавить пользователя") {
    private val mainViewModel: MainViewModel by inject()
    private val model = UserAddViewModel()
    private val validationCtx = ValidationContext()

    private val parentView: UserEditorView by inject()

    override fun onDock() {
        model.login.value = ""
        model.password.value = ""
        model.fullName.value = ""
    }

    override val root = form {
        fieldset("Заполните поля") {
            field("Логин:") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 64
                    }
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) {
                            error("Обязательное поле")
                        } else {
                            null
                        }
                    }
                    bind(model.login)
                }
            }
            field("Пароль") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 64
                    }
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) {
                            error("Обязательное поле")
                        } else {
                            null
                        }
                    }
                    bind(model.password)
                }
            }
            field("Отображаемые ФИО:") {
                textfield {
                    callKeyBoard()
                    filterInput {
                        it.controlNewText.length <= 128
                    }
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) {
                            error("Обязательное поле")
                        } else {
                            null
                        }
                    }
                    bind(model.fullName)
                }
            }
        }
        buttonbar {
            button("Добавить") {
                action {
                    transaction {
                        User.new {
                            login = model.login.value
                            password = model.password.value
                            fullName = model.fullName.value
                        }
                    }
                    parentView.tableviewUsers.items = mainViewModel.usersList
                }
            }
            button("Добавить и закрыть") {
                action {
                    mainViewModel.usersList.add(
                        transaction {
                            User.new {
                                login = model.login.value
                                password = model.password.value
                                fullName = model.fullName.value
                            }
                        }
                    )
                    parentView.tableviewUsers.items = mainViewModel.usersList
                    close()
                }
            }
        }.visibleWhen(validationCtx.valid)
    }.addClass(Styles.regularLabels)
}
