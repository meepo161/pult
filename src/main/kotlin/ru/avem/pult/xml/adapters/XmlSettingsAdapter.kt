package ru.avem.pult.xml.adapters

import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javax.xml.bind.annotation.adapters.XmlAdapter

class XmlSettingsAdapter : XmlAdapter<String, StringProperty>() {
    override fun unmarshal(v: String?): StringProperty {
        return SimpleStringProperty(v)
    }

    override fun marshal(v: StringProperty?): String {
        return v?.value ?: "null"
    }
}