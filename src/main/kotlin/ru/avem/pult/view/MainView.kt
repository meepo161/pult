package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import de.jensd.fx.glyphs.octicons.OctIcon
import de.jensd.fx.glyphs.octicons.OctIconView
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import org.slf4j.LoggerFactory
import ru.avem.pult.communication.model.CommunicationModel
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrController
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrModel
import ru.avem.pult.database.entities.TestObject
import ru.avem.pult.database.entities.User
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_3
import ru.avem.pult.viewmodels.MainViewModel.Companion.CONNECTION_4
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_1
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_3
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.warningNotification
import java.lang.Thread.sleep
import kotlin.system.exitProcess

class MainView : View("Лаборатория испытательная высоковольтная стационарная") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val model: MainViewModel by inject()
    private val validationCtx = ValidationContext()

    override fun onDock() {
        validationCtx.validate(false)
        currentWindow?.setOnCloseRequest {}
    }

    override val root = borderpane {
        prefWidth = 1366.0
        prefHeight = 768.0

        top {
            hbox {
                menubar {
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                    menu("База данных") {
                        graphic = OctIconView(OctIcon.DATABASE).apply {
                            glyphSize = 30.0
                        }
                        item("Объекты испытания") {
                            graphic = OctIconView(OctIcon.GEAR).apply {
                                glyphSize = 30.0
                            }

                            action {
                                model.performActionByAdmin {
                                    replaceWith<TestObjectView>(
                                        centerOnScreen = true
                                    )
                                }
                            }
                        }

//                        item("Испытания") {
//                            graphic = FontAwesomeIconView(FontAwesomeIcon.AREA_CHART).apply {
//                                glyphSize = 30.0
//                            }
//                        }

                        separator()

                        item("Список протоколов") {
                            graphic = OctIconView(OctIcon.FILE_TEXT).apply {
                                glyphSize = 30.0
                            }

                            action {
                                replaceWith<ProtocolsView>(
                                    centerOnScreen = true
                                )
                            }
                        }

                        separator()

                        item("Профили") {
                            graphic = OctIconView(OctIcon.ORGANIZATION).apply {
                                glyphSize = 30.0
                            }

                            action {
                                model.performActionByAdmin {
                                    replaceWith<UserEditorView>(
                                        centerOnScreen = true
                                    )
                                }
                            }
                        }
                    }
                    menu("Настройки") {
                        graphic = OctIconView(OctIcon.SETTINGS).apply {
                            glyphSize = 30.0
                        }

                        menu("Настройки приборов") {
                            graphic = OctIconView(OctIcon.ALERT).apply {
                                glyphSize = 30.0
                            }
                            item("Настройки АРН") {
                                graphic = OctIconView(OctIcon.VERSIONS).apply {
                                    glyphSize = 30.0
                                }
                                action {
                                    model.performActionByAdmin {
                                        replaceWith<LatrSettingsView>(
                                            centerOnScreen = true
                                        )
                                    }
                                }
                            }
//                            item("Настройки АВЭМ-4") {
//                                graphic = OctIconView(OctIcon.VERSIONS).apply {
//                                    glyphSize = 30.0
//                                }
//                            }
//                            item("Настройки АВЭМ-7") {
//                                graphic = OctIconView(OctIcon.VERSIONS).apply {
//                                    glyphSize = 30.0
//                                }
//                            }
                        }
//                        item("Настройки протокола") {
//                            graphic = OctIconView(OctIcon.FILE_TEXT).apply {
//                                glyphSize = 30.0
//                            }
//                            action {
//                                replaceWith<ProtocolSettingsView>(
//                                    centerOnScreen = true
//                                )
//                            }
//                        }
                        item("Настройки коэффициентов") {
                            graphic = OctIconView(OctIcon.FILE_BINARY).apply {
                                glyphSize = 30.0
                            }
                            action {
                                replaceWith<CoefficientsSettingsView>(
                                    centerOnScreen = true
                                )
                            }
                        }
                    }
                    menu("Инструменты") {
                        graphic = OctIconView(OctIcon.TOOLS).apply {
                            glyphSize = 30.0
                        }
                        item("Состояние устройств") {
                            graphic = OctIconView(OctIcon.CIRCUIT_BOARD).apply {
                                glyphSize = 30.0
                            }

                            action {
                                find<DeviceStatesView>().openModal(
                                    modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                    owner = this@MainView.currentWindow, resizable = false
                                )
                            }
                        }

                        item("Состояние входов БСУ") {
                            graphic = OctIconView(OctIcon.ISSUE_REOPENED).apply {
                                glyphSize = 30.0
                            }

                            action {
                                find<InputsStatesView>().openModal(
                                    modality = Modality.WINDOW_MODAL, escapeClosesWindow = true,
                                    owner = this@MainView.currentWindow, resizable = false
                                )
                            }
                        }

                        separator()

                        item("О программе") {
                            graphic = OctIconView(OctIcon.INFO).apply {
                                glyphSize = 30.0
                            }

                            action {
                                confirmNotification(
                                    "О программе",
                                    """Высоковольтная испытательная установка
                                   |Версия ПО: 1.0.0 от 22.10.2020
                                   """.trimMargin(),
                                    Pos.CENTER, darkStyle = true
                                )
                            }
                        }
                    }
                    menu("Выход") {
                        graphic = OctIconView(OctIcon.LOG_OUT).apply {
                            glyphSize = 30.0
                        }

                        item("На экран аутентификации") {
                            graphic = OctIconView(OctIcon.LOG_OUT).apply {
                                glyphSize = 30.0
                            }
                            action {
                                replaceWith<AuthenticationView>(
                                    centerOnScreen = true
                                )
                            }
                        }
                        item("Из программы") {
                            graphic = OctIconView(OctIcon.X).apply {
                                glyphSize = 30.0
                            }
                            action {
                                confirmation(
                                    "Выход",
                                    "Вы действительно хотите выйти?",
                                    ButtonType("ДА"), ButtonType("НЕТ"),
                                    owner = currentWindow,
                                    title = "Завершение работы"
                                ) { buttonType ->
                                    if (buttonType.text == "ДА") {
                                        exitProcess(0)
                                    }
                                }
                            }
                        }
                    }
                }

                button("Разблокировать люк ванны") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.UNLOCK).apply {
                        glyphSize = 29
                    }

                    action {
                        with(CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD2) as OwenPrController) {
                            CommunicationModel.startPoll(CommunicationModel.DeviceID.DD2, OwenPrModel.DI_01_16_RAW) {}
                            sleep(100)
                            if (!isHiSwitchTurned()) {
                                with(CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD3) as OwenPrController) {
                                    unlockBathLock()
                                }
                            } else {
                                warningNotification(
                                    title = "Внимание",
                                    text = "Невозможно открыть люк. Рубильник <Видимый разрыв> разомкнут",
                                    position = Pos.CENTER,
                                    hideAfter = 3.seconds
                                )
                            }
                            CommunicationModel.removePollingRegister(
                                CommunicationModel.DeviceID.DD2,
                                OwenPrModel.DI_01_16_RAW
                            )
                        }
                    }
                }
            }
        }

        center {
            anchorpane {
                vbox(spacing = 20.0) {
                    anchorpaneConstraints {
                        leftAnchor = 0.0
                        rightAnchor = 0.0
                        topAnchor = 0.0
                        bottomAnchor = 0.0
                    }
                    alignment = Pos.CENTER

                    vbox(spacing = 16.0) {
                        alignment = Pos.CENTER

                        label("Заполните поля") {
                            style {
                                fontWeight = FontWeight.BOLD
                            }
                        }.addClass(Styles.headerLabels)

                        hbox(spacing = 23.0) {
                            alignment = Pos.CENTER
                            label("Заводской номер") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT_ALT).apply {
                                    glyphSize = 18
                                }
                            }.addClass(Styles.regularLabels)

                            textfield(model.factoryNumber) {
                                callKeyBoard()
                                prefWidth = 550.0
                                validationCtx.addValidator(this) {
                                    if (it.isNullOrBlank()) error("Обязательное поле") else null
                                }
                            }.addClass(Styles.regularLabels)
                        }

                        hbox(spacing = 90.0) {
                            alignment = Pos.CENTER
                            label("Модуль") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.CONNECTDEVELOP).apply {
                                    glyphSize = 18.0
                                }
                            }

                            combobox<String>(model.module) {
                                prefWidth = 550.0
                                items = model.modulesList
                                validationCtx.addValidator(this, property = selectionModel.selectedItemProperty()) {
                                    if (it != null) null else error("Обязательное поле")
                                }

                                model.module.onChange {
                                    model.test.value = null
                                    model.connectionPoint1.property.value = false
                                    model.connectionPoint2.property.value = false
                                    model.connectionPoint3.property.value = false
                                    model.connectionPoint4.property.value = false
                                }
                            }
                        }.addClass(Styles.regularLabels)

                        hbox(spacing = 64.0) {
                            alignment = Pos.CENTER
                            label("Испытание") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.AREA_CHART).apply {
                                    glyphSize = 18.0
                                }
                            }
                            combobox<String>(model.test) {
                                prefWidth = 550.0

                                validationCtx.addValidator(this, property = selectionModel.selectedItemProperty()) {
                                    if (it == null) error("Обязательное поле") else null
                                }
                                model.test.onChange {
                                    model.testObject.value = null
                                }
                                setOnShowing {
                                    when (model.module.value) {
                                        MODULE_1 -> {
                                            items = model.firstModuleTestsList
                                        }
                                        MODULE_2 -> {
                                            items = model.secondModuleTestsList
                                        }
                                        MODULE_3 -> {
                                            items = model.thirdModuleTestsList
                                        }
                                    }
                                }
                            }
                        }.removeWhen(model.module.isEmpty).addClass(Styles.regularLabels)

                        hbox(spacing = 12.0) {
                            alignment = Pos.CENTER

                            label("Объект испытания") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.COGS).apply {
                                    glyphSize = 18.0
                                }
                            }.addClass(Styles.regularLabels)
                            combobox<TestObject>(model.testObject) {
                                prefWidth = 550.0
                                validationCtx.addValidator(this, property = model.testObject) {
                                    if (it == null) error("Обязательное поле") else null
                                }

                                setOnShowing {
                                    items = model.testObjectsList.filter {
                                        it.objectTest == model.test.value && it.objectModule == model.module.value
                                    }.observable()
                                }
                            }.addClass(Styles.regularLabels)
                        }.removeWhen(model.test.isEmpty).addClass(Styles.regularLabels)

                        vbox(spacing = 6) {
                            alignment = Pos.CENTER
                            paddingLeft = -310
                            checkbox("Испытательный канал 1", model.connectionPoint1.property)
                            checkbox("Испытательный канал 2", model.connectionPoint2.property)
                            checkbox("Испытательный канал 3", model.connectionPoint3.property)
                            checkbox("Испытательный канал 4", model.connectionPoint4.property)
                        }.removeWhen(model.module.isNotEqualTo(MODULE_2).or(model.testObject.isNull))
                            .addClass(Styles.regularLabels)

                        hbox(spacing = 57.0) {
                            alignment = Pos.CENTER

                            label("Контроллёр:") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.USERS).apply {
                                    glyphSize = 18.0
                                }
                            }
                            combobox<User>(model.user) {
                                prefWidth = 550.0
                                setOnShowing {
                                    items = model.usersList
                                }
                                validationCtx.addValidator(this, property = model.user) {
                                    if (it == null) error("Обязательное поле") else null
                                }
                            }
                        }.addClass(Styles.regularLabels)

                        checkbox("Ручная регулировка напряжения").addClass(Styles.regularLabels)
                            .bind(model.manualVoltageRegulation)
                    }

                    button("Перейти к испытаниям") {
                        isDefaultButton = true
                        prefWidth = 300.0

                        graphic = OctIconView(OctIcon.ALERT).apply {
                            glyphSize = 18.0
                            fill = c("red")
                        }

                        action {
                            model.selectedConnectionPoints.clear()
                            if (model.module.value == MODULE_2) {
                                if (model.connectionPoint1.property.value) {
                                    model.selectedConnectionPoints[CONNECTION_1] = model.connectionPoint1
                                }
                                if (model.connectionPoint2.property.value) {
                                    model.selectedConnectionPoints[CONNECTION_2] = model.connectionPoint2
                                }
                                if (model.connectionPoint3.property.value) {
                                    model.selectedConnectionPoints[CONNECTION_3] = model.connectionPoint3
                                }
                                if (model.connectionPoint4.property.value) {
                                    model.selectedConnectionPoints[CONNECTION_4] = model.connectionPoint4
                                }

                                if (model.selectedConnectionPoints.isEmpty()) {
                                    showConnectionsPointNotSelectedErrorNotification()
                                    return@action
                                }

                                if (model.testObject.value.objectVoltage.toInt() >= 10000) {
                                    if (model.selectedConnectionPoints.size > 2) {
                                        showConnectionsPointOvervoltageErrorNotification()
                                        return@action
                                    }
                                    if ((model.connectionPoint1.property.value && model.connectionPoint2.property.value)
                                        || (model.connectionPoint2.property.value && model.connectionPoint3.property.value)
                                        || (model.connectionPoint3.property.value && model.connectionPoint4.property.value)
                                    ) {
                                        showConnectionsPointTooCloseErrorNotification()
                                        return@action
                                    }
                                }
                            }

                            val causes = CommunicationModel.checkDevices()
                            if (causes.isEmpty()) {
                                replaceWith<TestView>()
                            } else {
                                showDeviceErrorNotification(causes)
                            }
                        }
                        enableWhen(validationCtx::valid)
                    }.addClass(Styles.regularLabels)
                }
            }
        }
    }

    private fun showConnectionsPointTooCloseErrorNotification() {
        error(
            header = "Ошибка",
            content = """
                При испытательных напряжениях > 10000 В можно выбрать испытательные каналы только через 1
                """.trimIndent(),
            title = "Выбраны 2 испытательных канала ближе, чем через 1",
            owner = currentWindow
        )
    }

    private fun showConnectionsPointOvervoltageErrorNotification() {
        error(
            header = "Ошибка",
            content = """
                При испытательных напряжениях > 10000 В можно выбрать только два испытательных канала
                """.trimIndent(),
            title = "Выбрано больше чем 2 испытательных канала",
            owner = currentWindow
        )
    }

    private fun showConnectionsPointNotSelectedErrorNotification() {
        error(
            header = "Ошибка",
            content = """
                Выберите, как минимум, один испытательный канал
                """.trimIndent(),
            title = "Не выбраны испытательные каналы",
            owner = currentWindow
        )
    }

    private fun showDeviceErrorNotification(causes: List<CommunicationModel.DeviceID>) {
        if (causes.containsAll(
                listOf(
                    CommunicationModel.DeviceID.DD2,
                    CommunicationModel.DeviceID.DD3,
                    CommunicationModel.DeviceID.PV21,
                    CommunicationModel.DeviceID.P11,
                    CommunicationModel.DeviceID.P12,
                    CommunicationModel.DeviceID.P13,
                    CommunicationModel.DeviceID.P14,
                    CommunicationModel.DeviceID.P15,
                    CommunicationModel.DeviceID.GV240
                )
            )
        ) {
            error(
                header = "Ошибка",
                content = """
                Нет связи со всеми устройствами. 
                Продолжить испытание невозможно.
                
                Возможные решения:
                1. Проверьте подключение преобразователя USB -> RS-485.
                2. Проверьте питание стенда.
                3. Проверьте соединение провода RS-485 на проблемных устройствах.
                4. Проверьте соединение провода на преобразователе USB -> RS-485
                
                Список устройств с которыми нет связи:
                ${causes.joinToString()}
                """.trimIndent(),
                title = "Ошибка связи с устройствами",
                owner = currentWindow
            )
        } else {
            when (model.module.value) {
                MODULE_1 -> {
                    if (causes.contains(CommunicationModel.DeviceID.DD2) ||
                        causes.contains(CommunicationModel.DeviceID.DD3) ||
                        causes.contains(CommunicationModel.DeviceID.PV21) ||
                        causes.contains(CommunicationModel.DeviceID.P11)
                    ) {
                        showDeviceMessageNotification(causes)
                    }
                }
                MODULE_2 -> {
                    if (causes.contains(CommunicationModel.DeviceID.DD2) ||
                        causes.contains(CommunicationModel.DeviceID.DD3) ||
                        causes.contains(CommunicationModel.DeviceID.PV21) ||
                        causes.contains(CommunicationModel.DeviceID.P12) ||
                        causes.contains(CommunicationModel.DeviceID.P13) ||
                        causes.contains(CommunicationModel.DeviceID.P14) ||
                        causes.contains(CommunicationModel.DeviceID.P15)
                    ) {
                        showDeviceMessageNotification(causes)
                    }
                }
                MODULE_3 -> {
                    if (causes.contains(CommunicationModel.DeviceID.DD2) ||
                        causes.contains(CommunicationModel.DeviceID.DD3) ||
                        causes.contains(CommunicationModel.DeviceID.PV21) ||
                        causes.contains(CommunicationModel.DeviceID.P11)
                    ) {
                        showDeviceMessageNotification(causes)
                    }
                }
            }
        }
    }

    private fun showDeviceMessageNotification(causes: List<CommunicationModel.DeviceID>) {
        error(
            header = "Ошибка",
            content = """
            Одно или несколько критически важных устройств не прошли проверку связи. 
            Продолжить испытание невозможно.
                                                        
            Возможные решения:
            1. Проверьте подключение преобразователя USB -> RS-485.
            2. Проверьте соединение провода RS-485 на проблемных устройствах.
                                                        
            Список устройств с которыми нет связи:
            ${causes.joinToString()}
            """.trimIndent(),
            title = "Ошибка связи с устройствами",
            owner = currentWindow
        )
    }
}