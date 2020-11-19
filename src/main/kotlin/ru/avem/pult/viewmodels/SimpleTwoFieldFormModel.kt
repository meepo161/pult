package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleStringProperty
import ru.avem.pult.xml.adapters.XmlSettingsAdapter
import tornadofx.ValidationContext
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlRootElement(name = "coefficients")
class SimpleTwoFieldFormModel {
    val validationCtx = ValidationContext()

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val firstValue = SimpleStringProperty("1")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val secondValue = SimpleStringProperty("1")
}