package ru.avem.pult.entities

import javafx.beans.property.StringProperty

data class ImpulseTableValues(
    var connection: StringProperty,
    var specifiedVoltage: StringProperty,
    var ktr: StringProperty,
    var measuredVoltage: StringProperty,
    var specifiedAmperage: StringProperty,
    var measuredAmperage: StringProperty,
    var dat1: StringProperty,
    var dat2: StringProperty,
    var testTime: StringProperty,
    var result: StringProperty
)