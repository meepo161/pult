package ru.avem.pult.entities

import javafx.beans.property.SimpleBooleanProperty

data class ConnectionPoint(
    val property: SimpleBooleanProperty = SimpleBooleanProperty(),
    var isNeedToUpdate: Boolean = false
)