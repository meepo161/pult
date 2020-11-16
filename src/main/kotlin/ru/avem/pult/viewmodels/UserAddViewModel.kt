package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleStringProperty

class UserAddViewModel {
    val login = SimpleStringProperty()
    val password = SimpleStringProperty()
    val fullName = SimpleStringProperty()
}
