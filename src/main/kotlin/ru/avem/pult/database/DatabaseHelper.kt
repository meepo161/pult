package ru.avem.pult.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.*
import ru.avem.pult.database.entities.Users.login
import java.sql.Connection

fun connectToDB() {
    Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        SchemaUtils.create(Users, Protocols, TestObjects, ProtocolFields)
    }
}

fun repairDB() {
    transaction {
        if (User.all().count() < 3) {
            val admin = User.find {
                login eq "admin"
            }

            if (admin.empty()) {
                User.new {
                    login = "admin"
                    password = "avem"
                    fullName = "admin"
                }
            }

            User.new {
                login = "1"
                password = "1"
                fullName = "1"
            }

            Protocol.new {
                date = "10.03.2020"
                time = "11:30:00"
                factoryNumber = "factoryNumber"
                objectName = "objectName"
                specifiedU = "specifiedU"
                specifiedI = "specifiedI"
                objectU0 = "objectU0"
                objectI0 = "objectI0"
                objectU1 = "objectU1"
                objectI1 = "objectI1"
                objectU2 = "objectU2"
                objectI2 = "objectI2"
                objectU3 = "objectU3"
                objectI3 = "objectI3"
                experimentTime0 = "experimentTime0"
                experimentTime1 = "experimentTime1"
                experimentTime2 = "experimentTime2"
                experimentTime3 = "experimentTime3"
                tester = "tester"
                result = "result"
                result0 = "result0"
                result1 = "result1"
                result2 = "result2"
                result3 = "result3"
                graphU = "[1,0, 2,0, 3,0, 4,0, 5,0, 6,0, 7,0, 8,0, 9,0, 10,0, 11,0, 12,0]"
                graphI = "[0,1, 0,2, 0,4, 0,8, 1,6, 3,2, 6,4, 12,8, 25,6, 51,2, 102,4, 204,8]"
            }

        }
    }
}
