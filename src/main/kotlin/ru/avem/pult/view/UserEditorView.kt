package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.stage.WindowEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.avem.pult.database.entities.User
import ru.avem.pult.database.entities.Users
import ru.avem.pult.database.entities.Users.login
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*

class UserEditorView : View("Редактор пользователей") {
    var tableviewUsers: TableView<User> by singleAssign()
    private val model: MainViewModel by inject()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(centerOnScreen = true)
            it.consume()
        }
        tableviewUsers.items = model.usersList
    }

    override val root = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0
        tableviewUsers = tableview {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                bottomAnchor = 16.0
                topAnchor = 128.0
            }

            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            items = model.usersList

            onEditStart {
                callKeyBoard()
            }
            onEditCommit {
                tableviewUsers.items = model.usersList
            }
            column("Логин", User::login)
            column("Пароль", User::password) {
                onEditCommit = EventHandler { cell ->
                    transaction {
                        Users.update({
                            login eq selectedItem!!.login
                        }) {
                            it[password] = cell.newValue
                        }
                    }
                }
            }.makeEditable()
            column("ФИО", User::fullName) {
                onEditCommit = EventHandler { cell ->
                    transaction {
                        Users.update({
                            login eq selectedItem!!.login
                        }) {
                            it[fullName] = cell.newValue
                        }
                    }
                }
            }.makeEditable()
        }

        hbox(spacing = 32.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
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

            button("Добавить пользователя") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                    glyphSize = 60
                }
                action {
                    find<UserAddView>().openModal(resizable = false)
                }
            }

            button("Удалить") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH).apply {
                    glyphSize = 60
                }
                action {
                    confirm(
                        "Удаление пользователя ${tableviewUsers.selectedItem!!.login}",
                        "Вы действительно хотите удалить пользователя?",
                        ButtonType("ДА"), ButtonType("НЕТ"),
                        owner = this@UserEditorView.currentWindow,
                        title = "Удаление пользователя ${tableviewUsers.selectedItem!!.login}"
                    ) {
                        transaction {
                            Users.deleteWhere { Users.id eq tableviewUsers.selectedItem!!.id }
                        }
                        tableviewUsers.items = model.usersList
                    }
                }
            }.removeWhen(tableviewUsers.selectionModel.selectedItemProperty().isNull)
        }
    }.addClass(Styles.hard)
}
