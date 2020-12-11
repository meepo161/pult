package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty

class TestObjectAddViewModel {
    var name = SimpleStringProperty()
    var voltage = SimpleIntegerProperty()
    var amperage = SimpleDoubleProperty()
    var time = SimpleIntegerProperty()
    var transformer = SimpleStringProperty()
    var test = SimpleStringProperty()
}
