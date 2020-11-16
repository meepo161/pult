package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleStringProperty
import ru.avem.pult.xml.adapters.XmlSettingsAdapter
import tornadofx.ValidationContext
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

@XmlRootElement(name = "LatrSettings")
class LatrSettingsFragmentModel {
    val validationCtx = ValidationContext()

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val minDutty = SimpleStringProperty("50")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val maxDutty = SimpleStringProperty("50")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val corridor = SimpleStringProperty("0.2")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val delta = SimpleStringProperty("0.03")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val timeMinPulse = SimpleStringProperty("100")

    @XmlElement
    @XmlJavaTypeAdapter(XmlSettingsAdapter::class)
    val timeMaxPulse = SimpleStringProperty("100")
}