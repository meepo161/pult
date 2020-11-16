package ru.avem.pult.entities

import javafx.beans.property.DoubleProperty
import javafx.beans.property.IntegerProperty
import javafx.beans.property.StringProperty

data class TableValues(
    var connection: StringProperty,
    var specifiedVoltage: StringProperty,
    var ktr: StringProperty,
    var measuredVoltage: StringProperty,
    var specifiedAmperage: StringProperty,
    var measuredAmperage: StringProperty,
    var testTime: StringProperty,
    var result: StringProperty
)
