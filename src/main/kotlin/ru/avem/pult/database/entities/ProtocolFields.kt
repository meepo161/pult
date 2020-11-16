package ru.avem.pult.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object ProtocolFields : IntIdTable() {
    val fieldKey = varchar("fieldKey", 256)
    val fieldValue = varchar("fieldValue", 512)
}

class ProtocolField(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ProtocolField>(ProtocolFields)

    var fieldKey by ProtocolFields.fieldKey
    var fieldValue by ProtocolFields.fieldValue
}
