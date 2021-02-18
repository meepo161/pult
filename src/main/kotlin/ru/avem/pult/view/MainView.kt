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
import ru.avem.pult.database.entities.TestObject
import ru.avem.pult.database.entities.User
import ru.avem.pult.utils.callKeyBoard
import ru.avem.pult.viewmodels.MainViewModel
import tornadofx.*
import tornadofx.controlsfx.confirmNotification
import tornadofx.controlsfx.warningNotification
import kotlin.system.exitProcess

class MainView : View("Лаборатория испытательная высоковольтная стационарная") {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
    }

    private val model: MainViewModel by inject()
    private val errorValidationCtx = ValidationContext()
    private val warningValidatorCtx = ValidationContext().apply {
        decorationProvider = {
            SimpleMessageDecorator(it.message, ValidationSeverity.Warning)
        }
    }

    private val testView: TestView by inject()


    override fun onDock() {
        errorValidationCtx.validate(false)
        warningValidatorCtx.validate(false)
        currentWindow?.setOnCloseRequest {
            if (model.isLampOn.value) {
                warningNotification(
                    "Внимание!",
                    "Выключите лампу перед выходом из программы!",
                    Pos.CENTER,
                    hideAfter = 10.seconds
                )
                it.consume()
            }
        }
    }

    override val root = borderpane {
        prefWidth = 1920.0
        prefHeight = 1080.0

        top {
            hbox {
                menubar {
                    hboxConstraints {
                        hGrow = Priority.ALWAYS
                    }
                    menu("База данных") {
                        graphic = OctIconView(OctIcon.DATABASE).apply {
                            glyphSize = 60.0
                        }
                        item("Объекты испытания") {
                            graphic = OctIconView(OctIcon.GEAR).apply {
                                glyphSize = 60.0
                            }

                            action {
                                replaceWith<TestObjectView>(
                                    centerOnScreen = true
                                )
                            }
                        }

                        separator()

                        item("Список протоколов") {
                            graphic = OctIconView(OctIcon.FILE_TEXT).apply {
                                glyphSize = 60.0
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
                                glyphSize = 60.0
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
                            glyphSize = 60.0
                        }

                        menu("Настройки приборов") {
                            graphic = OctIconView(OctIcon.ALERT).apply {
                                glyphSize = 60.0
                            }
                            item("Настройки АРН") {
                                graphic = OctIconView(OctIcon.VERSIONS).apply {
                                    glyphSize = 60.0
                                }
                                action {
                                    model.performActionByAdmin {
                                        replaceWith<LatrSettingsView>(
                                            centerOnScreen = true
                                        )
                                    }
                                }
                            }
                        }
                        item("Настройки коэффициентов") {
                            graphic = OctIconView(OctIcon.FILE_BINARY).apply {
                                glyphSize = 60.0
                            }
                            action {
                                model.performActionByAdmin {
                                    replaceWith<CoefficientsSettingsView>(
                                        centerOnScreen = true
                                    )
                                }
                            }
                        }
                    }
                    menu("Инструменты") {
                        graphic = OctIconView(OctIcon.TOOLS).apply {
                            glyphSize = 60.0
                        }
                        item("Состояние устройств") {
                            graphic = OctIconView(OctIcon.CIRCUIT_BOARD).apply {
                                glyphSize = 60.0
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
                                glyphSize = 60.0
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
                                glyphSize = 60.0
                            }

                            action {
                                confirmNotification(
                                    "О программе",
                                    """Высоковольтная испытательная установка
                                   |Версия ПО: 1.0.1 от 18.02.2021
                                   """.trimMargin(),
                                    Pos.CENTER, darkStyle = true
                                )
                            }
                        }
                    }
                    menu("Выход") {
                        graphic = OctIconView(OctIcon.LOG_OUT).apply {
                            glyphSize = 60.0
                        }

                        item("На экран аутентификации") {
                            graphic = OctIconView(OctIcon.LOG_OUT).apply {
                                glyphSize = 60.0
                            }
                            action {
                                replaceWith<AuthenticationView>(
                                    centerOnScreen = true
                                )
                            }
                        }
                        item("Из программы") {
                            graphic = OctIconView(OctIcon.X).apply {
                                glyphSize = 60.0
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
                togglebutton("ВКЛ ЛАМПУ") {
                    graphic = FontAwesomeIconView(FontAwesomeIcon.LIGHTBULB_ALT).apply {
                        glyphSize = 60
                    }
                    isSelected = false

                    action {
                        with(CommunicationModel.getDeviceById(CommunicationModel.DeviceID.DD1) as OwenPrController) {
                            if (isSelected) {
                                onLampPower()
                                this@MainView.model.isLampOn.value = true
                                text = "ВЫКЛ ЛАМПУ"
                            } else {
                                this@MainView.model.isLampOn.value = false
                                offLampPower()
                                text = "ВКЛ ЛАМПУ"
                            }
                        }
                    }
                }
            }.addClass(Styles.hard)
        }

        center {
            anchorpane {
                vbox(spacing = 64.0) {
                    anchorpaneConstraints {
                        leftAnchor = 0.0
                        rightAnchor = 0.0
                        topAnchor = 0.0
                        bottomAnchor = 0.0
                    }
                    alignment = Pos.CENTER

                    vbox(spacing = 32.0) {
                        alignment = Pos.CENTER

                        label("Заполните поля") {
                            style {
                                fontWeight = FontWeight.BOLD
                            }
                        }.addClass(Styles.hard)

                        hbox(spacing = 30.0) {
                            alignment = Pos.CENTER
                            label("Заводской номер") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.FILE_TEXT_ALT).apply {
                                    glyphSize = 60
                                }
                            }.addClass(Styles.hard)

                            textfield(model.factoryNumber) {
                                callKeyBoard()
                                prefWidth = 1200.0
                                warningValidatorCtx.addValidator(this) {
                                    if (it.isNullOrBlank()) error("Если не заполнено, то будет пустым полем в протоколе") else null
                                }
                            }.addClass(Styles.hard)
                        }

                        hbox(spacing = 62.0) {
                            alignment = Pos.CENTER
                            label("Вид испытания") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.AREA_CHART).apply {
                                    glyphSize = 60
                                }
                            }
                            combobox<String>(model.test) {
                                prefWidth = 1200.0
                                items = model.testList
                                errorValidationCtx.addValidator(
                                    this,
                                    property = selectionModel.selectedItemProperty()
                                ) {
                                    if (it == null) error("Обязательное поле") else null
                                }
                                model.test.onChange {
                                    model.testObject.value = null
                                }
                            }
                        }.addClass(Styles.hard)

                        hbox(spacing = 0.0) {
                            alignment = Pos.CENTER

                            label("Объект испытания") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.COGS).apply {
                                    glyphSize = 60
                                }
                            }.addClass(Styles.hard)
                            combobox<TestObject>(model.testObject) {
                                prefWidth = 1200.0
                                errorValidationCtx.addValidator(this, property = model.testObject) {
                                    if (it == null) error("Обязательное поле") else null
                                }

                                setOnShowing {
                                    items = model.testObjectsList.filter {
                                        it.objectTest == model.test.value && it.objectTest == model.test.value
                                    }.observable()
                                }
                            }.addClass(Styles.hard)
                        }.removeWhen(model.test.isEmpty).addClass(Styles.hard)

                        hbox(spacing = 130.0) {
                            alignment = Pos.CENTER

                            label("Контроллёр") {
                                graphic = FontAwesomeIconView(FontAwesomeIcon.USERS).apply {
                                    glyphSize = 60
                                }
                            }
                            combobox<User>(model.user) {
                                prefWidth = 1200.0
                                setOnShowing {
                                    items = model.usersList
                                }
                                warningValidatorCtx.addValidator(this, property = model.user) {
                                    if (it == null) error("Если не заполнено, то будет пустым полем в протоколе") else null
                                }
                            }
                        }.addClass(Styles.hard)

                        checkbox("Ручная регулировка напряжения") {
                        }.addClass(Styles.hard).bind(model.isManualVoltageRegulation)
                    }

                    button("Перейти к испытаниям") {
                        isDefaultButton = true
                        prefWidth = 1200.0
                        prefHeight = 160.0

                        graphic = OctIconView(OctIcon.ALERT).apply {
                            glyphSize = 60
                            fill = c("red")
                        }

                        action {
//                            if (model.test.value == TEST_2 && !model.isLampOn.value) {
//                                warningNotification(
//                                    "Внимание",
//                                    "Для проведения импульсных испытаний требуется включить питание лампы",
//                                    Pos.BOTTOM_CENTER
//                                )
//                                return@action
//                            }
//                            val causes = CommunicationModel.checkDevices()
//                            if (causes.isEmpty()) {
                            replaceWith<TestView>()
//                            } else {
//                                showDeviceErrorNotification(causes)
//                            } TODO
                        }
                        enableWhen(errorValidationCtx::valid)
                    }.addClass(Styles.superHard)
                }
            }
        }
    }

    private fun showDeviceErrorNotification(causes: List<CommunicationModel.DeviceID>) {
        if (causes.containsAll(
                listOf(
                    CommunicationModel.DeviceID.DD1,
                    CommunicationModel.DeviceID.PV21,
                    CommunicationModel.DeviceID.PA11,
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
            if (causes.contains(CommunicationModel.DeviceID.DD1) ||
                causes.contains(CommunicationModel.DeviceID.PV21) ||
                causes.contains(CommunicationModel.DeviceID.GV240) ||
                causes.contains(CommunicationModel.DeviceID.PA11)
            ) {
                showDeviceMessageNotification(causes)
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
