package ru.avem.pult.database.entities

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Protocols : IntIdTable() {
    val date = varchar("date", 128)
    val time = varchar("time", 128)
    val factoryNumber = varchar("factoryNumber", 64)
    val objectName = varchar("objectName", 256)
    val specifiedU = varchar("specifiedU", 64)
    val specifiedI = varchar("specifiedI", 64)
    val objectU0 = varchar("objectU0", 64).nullable()
    val objectI0 = varchar("objectI0", 64).nullable()
    val objectU1 = varchar("objectU1", 64).nullable()
    val objectI1 = varchar("objectI1", 64).nullable()
    val objectU2 = varchar("objectU2", 64).nullable()
    val objectI2 = varchar("objectI2", 64).nullable()
    val objectU3 = varchar("objectU3", 64).nullable()
    val objectI3 = varchar("objectI3", 64).nullable()
    val experimentTime0 = varchar("experimentTime0", 64).nullable()
    val experimentTime1 = varchar("experimentTime1", 64).nullable()
    val experimentTime2 = varchar("experimentTime2", 64).nullable()
    val experimentTime3 = varchar("experimentTime3", 64).nullable()
    val tester = varchar("tester", 256)
    val result = varchar("result", 64)
    val result0 = varchar("result0", 64).nullable()
    val result1 = varchar("result1", 64).nullable()
    val result2 = varchar("result2", 64).nullable()
    val result3 = varchar("result3", 64).nullable()
}

class Protocol(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Protocol>(Protocols)

    var date by Protocols.date
    var time by Protocols.time
    var factoryNumber by Protocols.factoryNumber
    var objectName by Protocols.objectName
    var specifiedU by Protocols.specifiedU
    var specifiedI by Protocols.specifiedI
    var objectU0 by Protocols.objectU0
    var objectI0 by Protocols.objectI0
    var objectU1 by Protocols.objectU1
    var objectI1 by Protocols.objectI1
    var objectU2 by Protocols.objectU2
    var objectI2 by Protocols.objectI2
    var objectU3 by Protocols.objectU3
    var objectI3 by Protocols.objectI3
    var experimentTime0 by Protocols.experimentTime0
    var experimentTime1 by Protocols.experimentTime1
    var experimentTime2 by Protocols.experimentTime2
    var experimentTime3 by Protocols.experimentTime3
    var tester by Protocols.tester
    var result by Protocols.result
    var result0 by Protocols.result0
    var result1 by Protocols.result1
    var result2 by Protocols.result2
    var result3 by Protocols.result3

    override fun toString(): String {
        return "$id. $factoryNumber:$objectName - $date Результат: $result"
    }
}
