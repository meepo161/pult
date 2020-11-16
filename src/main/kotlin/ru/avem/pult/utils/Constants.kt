package ru.avem.pult.utils

import javafx.scene.paint.Color
import tornadofx.c

enum class ExperimentType(val type: String) {
    AC("Переменный ток") {
        override fun toString() = type
    },
    DC("Постоянный ток") {
        override fun toString() = type
    }
}

enum class LogTag(val c: Color) {
    MESSAGE(c("#6A8759")),
    ERROR(c("#FF6B68")),
    DEBUG(c("#6897BB"))
}

enum class TestStateColors(val c: Color) {
    WAIT(c("#ffaf69")),
    GO(c("#42f57e")),
    ERROR(c("#ff0000"))
}

enum class State(val c: Color) {
    OK(c("#00FF0093")),
    INTERMEDIATE(c("#FFFF0093")),
    BAD(c("#FF000093")),
}

const val KTR = 31

const val YES = "ДА"
const val NO = "НЕТ"

const val SHEET_PASSWORD = "444488888888"

const val KM2_STATE = 0b1
const val A1_PROTECTION_STATE = 0b10
const val A2_PROTECTION_STATE = 0b100
const val A3_PROTECTION_STATE = 0b1000
const val DOOR_STATE = 0b10000
