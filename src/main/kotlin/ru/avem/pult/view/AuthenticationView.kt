package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.User
import ru.avem.pult.database.entities.Users
import ru.avem.pult.database.entities.Users.login
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.warningNotification

class AuthenticationView : View("Аутентификация") {
    private val model: MainViewModel by inject()
    private val validationCtx = ValidationContext()
    private val loginProperty = SimpleStringProperty()
    private val passwordProperty = SimpleStringProperty()

    override val root = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0

        vbox(spacing = 16.0) {
            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
                leftAnchor = 0.0
                rightAnchor = 0.0
            }
            alignment = Pos.CENTER

            label("Аутентификация") {
                style {
                    fontSize = 25.px
                }
            }
            hbox(spacing = 24.0) {
                alignment = Pos.CENTER

                label("Логин") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.GROUP)
                }
                textfield {
                    callKeyBoard()
                    promptText = "Логин"
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) error("Обязательное поле") else null
                    }
                }.bind(loginProperty)
            }.addClass(Styles.regularLabels)
            hbox(spacing = 16.0) {
                alignment = Pos.CENTER

                label("Пароль") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.KEY)
                }
                passwordfield {
                    callKeyBoard()
                    promptText = "Пароль"
                    validationCtx.addValidator(this) {
                        if (it.isNullOrBlank()) error("Обязательное поле") else null
                    }
                }.bind(passwordProperty)
            }.addClass(Styles.regularLabels)
            button("Вход") {
                anchorpaneConstraints {
                    leftAnchor = 16.0
                    rightAnchor = 16.0
                    topAnchor = 270.0
                }
                isDefaultButton = true
                graphic = FontAwesomeIconView(FontAwesomeIcon.SIGN_IN)
                onAction = EventHandler {
                    transaction {
                        model.authorizedUser.value = User.find {
                            (login eq loginProperty.value) and (Users.password eq passwordProperty.value)
                        }.firstOrNull()
                        if (model.authorizedUser.value == null) {
                            warningNotification(
                                "Неправильный логин или пароль", "Проверьте данные для входа и повторите снова.",
                                Pos.BOTTOM_CENTER, hideAfter = 3.seconds
                            )
                        } else {
                            confirmNotification(
                                "Авторизация",
                                "Вы вошли как: ${loginProperty.value}",
                                Pos.BOTTOM_CENTER,
                                hideAfter = 3.seconds
                            )
                            replaceWith<MainView>(
                                centerOnScreen = true
                            )
                        }
                    }
                }
                enableWhen(validationCtx.valid)
            }.addClass(Styles.regularLabels)
        }
    }
}
