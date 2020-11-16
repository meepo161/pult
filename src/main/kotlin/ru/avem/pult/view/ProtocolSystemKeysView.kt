package ru.avem.pult.view

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.event.EventType
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.stage.WindowEvent
import tornadofx.*

class ProtocolSystemKeysView : View("Системные ключи") {
    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            replaceWith<ProtocolSettingsView>(
                centerOnScreen = true
            )
            it.consume()
        }
    }

    override val root = vbox(spacing = 16.0) {
        paddingAll = 16
        button("Назад") {
            graphic = FontAwesomeIconView(FontAwesomeIcon.ARROW_LEFT).apply {
                glyphSize = 18
            }
            action {
                currentWindow?.onCloseRequest?.handle(WindowEvent(currentWindow, EventType.ROOT))
            }
        }
        tableview<SystemProtocolField> {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
            items = observableList(
                SystemProtocolField(
                    "#PROTOCOL_NUMBER#",
                    "Порядковый номер протокола",
                    "Число: 1, 2, 3 .. n"
                ),
                SystemProtocolField(
                    "#OBJECT#",
                    "Наименование объекта испытания",
                    "Перчатки диэлектрические, Указатель"
                ),
                SystemProtocolField(
                    "#SERIAL_NUMBER#",
                    "Заводской номер изделия",
                    "ТБ303707909"
                ),
                SystemProtocolField(
                    "#U_SET_1#",
                    "Заданное напряжение объекта испытания, либо точки 1, если испытание проводится на модуле 2",
                    "6000"
                ),
                SystemProtocolField(
                    "#U_OBJ_1#",
                    "Измеренное напряжение объекта испытания, либо точки 1, если испытание проводится на модуле 2",
                    "5932"
                ),
                SystemProtocolField(
                    "#I_SET_1#",
                    "Заданный ток утечки объекта испытания, либо точки 1, если испытание проводится на модуле 2",
                    "6"
                ),
                SystemProtocolField(
                    "#I_OBJ_1#",
                    "Измеренный ток утечки объекта испытания, либо точки 1, если испытание проводится на модуле 2",
                    "3.03"
                ),
                SystemProtocolField(
                    "#TIME_1#",
                    "Время выдержки объекта испытания, либо точки 1, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#RESULT_1#",
                    "Результат испытания, либо точки 1, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#U_SET_2#",
                    "Заданное напряжение объекта испытания, либо точки 2, если испытание проводится на модуле 2",
                    "6000"
                ),
                SystemProtocolField(
                    "#U_OBJ_2#",
                    "Измеренное напряжение объекта испытания, либо точки 2, если испытание проводится на модуле 2",
                    "5932"
                ),
                SystemProtocolField(
                    "#I_SET_2#",
                    "Заданный ток утечки объекта испытания, либо точки 2, если испытание проводится на модуле 2",
                    "6"
                ),
                SystemProtocolField(
                    "#I_OBJ_2#",
                    "Измеренный ток утечки объекта испытания, либо точки 2, если испытание проводится на модуле 2",
                    "3.03"
                ),
                SystemProtocolField(
                    "#TIME_2#",
                    "Время выдержки объекта испытания, либо точки 2, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#RESULT_2#",
                    "Результат испытания, либо точки 2, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#U_SET_3#",
                    "Заданное напряжение объекта испытания, либо точки 3, если испытание проводится на модуле 2",
                    "6000"
                ),
                SystemProtocolField(
                    "#U_OBJ_3#",
                    "Измеренное напряжение объекта испытания, либо точки 3, если испытание проводится на модуле 2",
                    "5932"
                ),
                SystemProtocolField(
                    "#I_SET_3#",
                    "Заданный ток утечки объекта испытания, либо точки 3, если испытание проводится на модуле 2",
                    "6"
                ),
                SystemProtocolField(
                    "#I_OBJ_3#",
                    "Измеренный ток утечки объекта испытания, либо точки 3, если испытание проводится на модуле 2",
                    "3.03"
                ),
                SystemProtocolField(
                    "#TIME_3#",
                    "Время выдержки объекта испытания, либо точки 3, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#RESULT_3#",
                    "Результат испытания, либо точки 3, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#U_SET_4#",
                    "Заданное напряжение объекта испытания, либо точки 4, если испытание проводится на модуле 2",
                    "6000"
                ),
                SystemProtocolField(
                    "#U_OBJ_4#",
                    "Измеренное напряжение объекта испытания, либо точки 4, если испытание проводится на модуле 2",
                    "5932"
                ),
                SystemProtocolField(
                    "#I_SET_4#",
                    "Заданный ток утечки объекта испытания, либо точки 4, если испытание проводится на модуле 2",
                    "6"
                ),
                SystemProtocolField(
                    "#I_OBJ_4#",
                    "Измеренный ток утечки объекта испытания, либо точки 4, если испытание проводится на модуле 2",
                    "3.03"
                ),
                SystemProtocolField(
                    "#TIME_4#",
                    "Время выдержки объекта испытания, либо точки 4, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#RESULT_4#",
                    "Результат испытания, либо точки 4, если испытание проводится на модуле 2",
                    "60"
                ),
                SystemProtocolField(
                    "#POS_1_NAME#",
                    "ФИО испытателя",
                    "Иванов Иван Иванович"
                ),
                SystemProtocolField(
                    "#DATE#",
                    "Сегодняшняя дата и время в формате- чч:мм ДД.ММ.ГГ",
                    "19:09 - 01.01.2020"
                )
            )
            column("Ключ", SystemProtocolField::key).fixedWidth(250)
            column("Описание", SystemProtocolField::desc)
            column("Пример значения", SystemProtocolField::sample).fixedWidth(400)
        }
    }.addClass(Styles.regularLabels)
}

data class SystemProtocolField(var key: String, var desc: String, var sample: String)