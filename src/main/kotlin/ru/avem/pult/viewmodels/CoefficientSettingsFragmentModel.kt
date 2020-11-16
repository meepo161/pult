package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleStringProperty
import ru.avem.pult.xml.adapters.XmlSettingsAdapter
import tornadofx.ValidationContext
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlRootElement(name = "coefficients")
class CoefficientSettingsFragmentModel {
    val validationCtx = ValidationContext()

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val latr = SimpleStringProperty("200")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val tap = SimpleStringProperty("100")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val obj = SimpleStringProperty("20000")
}