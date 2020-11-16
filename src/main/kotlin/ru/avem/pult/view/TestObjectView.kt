package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventHandler
import javafx.event.EventType
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.control.TableView
import javafx.scene.control.cell.ComboBoxTableCell
import javafx.stage.WindowEvent
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import ru.avem.pult.database.entities.TestObject
import ru.avem.pult.database.entities.TestObjects
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_1_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_2_VOLTAGE
import ru.avem.pult.viewmodels.MainViewModel.Companion.TYPE_3_VOLTAGE
import tornadofx.*
import tornadofx.controlsfx.warningNotification

class TestObjectView : View("Объекты испытания") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    var objectsTable: TableView<TestObject> by singleAssign()
    private val model: MainViewModel by inject()

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<MainView>(
                centerOnScreen = true
            )
            it.consume()
        }

        objectsTable.items = model.testObjectsList
    }

    override val root = anchorpane {
        prefWidth = 1366.0
        prefHeight = 768.0
        objectsTable = tableview() {
            anchorpaneConstraints {
                leftAnchor = 16.0
                rightAnchor = 16.0
                topAnchor = 64.0
                bottomAnchor = 16.0
            }

            columnResizePolicy = SmartResize.POLICY
            prefHeight = 82.0
            enableDirtyTracking()
            onEditCommit {
                objectsTable.items = model.testObjectsList
            }
            onEditStart {
                callKeyBoard()
            }
            column("Наименование", TestObject::objectName)
            column("Устанавливаемое напряжение, В", TestObject::objectVoltage) {
                setOnEditCommit { cell ->
                    val value = cell.newValue.toInt()
                    when (selectedItem!!.objectTransformer) {
                        TYPE_1_VOLTAGE.toString() -> {
                            if ((0..TYPE_1_VOLTAGE).contains(value)) {
                                transaction {
                                    TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                        it[objectVoltage] = cell.newValue
                                    }
                                }
                            } else {
                                this.cellDecorator {
                                    text = it
                                }
                                warningNotification(
                                    "Внимание",
                                    "Введите корректное значение. Напряжение должно быть в диапазоне 0 - $TYPE_1_VOLTAGE В",
                                    Pos.BOTTOM_CENTER
                                )
                            }
                        }
                        TYPE_2_VOLTAGE.toString() -> {
                            if ((0..TYPE_2_VOLTAGE).contains(value)) {
                                transaction {
                                    TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                        it[objectVoltage] = cell.newValue
                                    }
                                }
                            } else {
                                this.cellDecorator {
                                    text = it
                                }
                                warningNotification(
                                    "Внимание",
                                    "Введите корректное значение. Напряжение должно быть в диапазоне 0 - $TYPE_2_VOLTAGE В",
                                    Pos.BOTTOM_CENTER
                                )
                            }
                        }
                        TYPE_3_VOLTAGE.toString() -> {
                            if ((0..TYPE_3_VOLTAGE).contains(value)) {
                                transaction {
                                    TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                        it[objectVoltage] = cell.newValue
                                    }
                                }
                            } else {
                                this.cellDecorator {
                                    text = it
                                }
                                warningNotification(
                                    "Внимание",
                                    "Введите корректное значение. Напряжение должно быть в диапазоне 0 - $TYPE_3_VOLTAGE В",
                                    Pos.BOTTOM_CENTER
                                )
                            }
                        }
                    }
                }
            }.makeEditable()
            column("Ток утечки, мА", TestObject::objectAmperage) {
                onEditCommit = EventHandler { cell ->
                    try {
                        val value = cell.newValue.toFloat()
                        if ((0.0..10.0).contains(value)) {
                            transaction {
                                TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                    it[objectAmperage] = cell.newValue
                                }
                            }
                        } else {
                            throw NumberFormatException()
                        }
                    } catch (e: NumberFormatException) {
                        this.cellDecorator {
                            text = it
                        }
                        warningNotification(
                            "Внимание",
                            "Введите корректное значение. Ток утечки должен быть в диапазоне 0 - 10 мА",
                            Pos.BOTTOM_CENTER
                        )
                    }
                }
            }.makeEditable()
            column("Время, c", TestObject::objectTime) {
                onEditCommit = EventHandler { cell ->
                    try {
                        val value = cell.newValue.toInt()
                        if ((0..360).contains(value)) {
                            transaction {
                                TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                    it[objectTime] = cell.newValue
                                }
                            }
                        } else {
                            throw NumberFormatException()
                        }
                    } catch (e: NumberFormatException) {
                        this.cellDecorator {
                            text = it
                        }
                        warningNotification(
                            "Внимание",
                            "Введите корректное значение. Время должно быть в диапазоне 0 - 360 с.",
                            Pos.BOTTOM_CENTER
                        )
                    }
                }
            }.makeEditable()
            val experimentColumn = column("Испытание", TestObject::objectTest) {
                onEditCommit = EventHandler { cell ->
                    if (cell.newValue != null) {
                        transaction {
                            TestObjects.update({ TestObjects.objectName eq selectedItem!!.objectName }) {
                                it[objectTest] = cell.newValue
                            }
                        }
                    } else {
                        this.cellDecorator {
                            text = it
                        }
                    }
                }
            }

            selectionModel.selectedItemProperty().onChange {
                experimentColumn.cellFactory = ComboBoxTableCell.forTableColumn(
                    when (selectedItem?.objectModule) {
                        MODULE_1 -> {
                            when (selectedItem?.objectTransformer) {
                                TYPE_1_VOLTAGE.toString() -> {
                                    model.firstModuleTestsList200V
                                }
                                TYPE_2_VOLTAGE.toString() -> {
                                    model.firstModuleTestsList20kV
                                }
                                else -> null
                            }
                        }
                        MODULE_2 -> {
                            when (selectedItem?.objectTransformer) {
                                TYPE_2_VOLTAGE.toString() -> {
                                    model.secondModuleTestsList20kV
                                }
                                TYPE_3_VOLTAGE.toString() -> {
                                    model.secondModuleTestsList50kV
                                }
                                else -> null
                            }
                        }
                        MODULE_3 -> {
                            model.thirdModuleTestsList
                        }
                        else -> {
                            null
                        }
                    }
                )
            }
        }

        hbox(spacing = 32.0) {
            anchorpaneConstraints {
                leftAnchor = 16.0
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

            button("Создать объект испытания") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS).apply {
                    glyphSize = 18
                }
                action {
                    find<AddTestObjectView>().openModal(resizable = false)
                }
            }

            button("Удалить") {
                graphic = FontAwesomeIconView(FontAwesomeIcon.TRASH).apply {
                    glyphSize = 18
                }
                action {
                    confirm(
                        "Удаление ОИ ${objectsTable.selectedItem!!.objectName}",
                        "Вы действительно хотите удалить объект испытания?",
                        ButtonType("ДА"), ButtonType("НЕТ"),
                        owner = this@TestObjectView.currentWindow,
                        title = "Удаление ОИ ${objectsTable.selectedItem!!.objectName}"
                    ) {
                        with(objectsTable.selectedItem!!) {
                            transaction {
                                TestObjects.deleteWhere { TestObjects.id eq id }
                            }
                        }
                        objectsTable.items = model.testObjectsList
                    }
                }
            }.removeWhen(objectsTable.selectionModel.selectedItemProperty().isNull)
        }.addClass(Styles.regularLabels)
    }
}
