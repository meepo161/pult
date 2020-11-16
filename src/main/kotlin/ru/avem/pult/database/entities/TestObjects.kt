package ru.avem.pult.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object TestObjects : IntIdTable() {
    val objectName = varchar("objectName", 256)
    val objectVoltage = varchar("voltage", 32)
    val objectAmperage = varchar("amperage", 32)
    val objectTime = varchar("time", 32)
    val objectModule = varchar("module", 512)
    val objectTransformer = varchar("transformer", 64)
    val objectTest = varchar("test", 2048)
}

class TestObject(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TestObject>(TestObjects)

    var objectName by TestObjects.objectName
    var objectVoltage by TestObjects.objectVoltage
    var objectAmperage by TestObjects.objectAmperage
    var objectTime by TestObjects.objectTime
    var objectModule by TestObjects.objectModule
    var objectTransformer by TestObjects.objectTransformer
    var objectTest by TestObjects.objectTest

    override fun toString(): String {
        return objectName
    }
}
