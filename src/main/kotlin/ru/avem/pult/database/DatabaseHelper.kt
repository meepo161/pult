package ru.avem.pult.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.TestObjects.objectName
import ru.avem.pult.database.entities.Users.login
import ru.avem.pult.viewmodels.MainViewModel.Companion.MODULE_2
import ru.avem.pult.viewmodels.MainViewModel.Companion.TEST_4
import ru.avem.pult.database.entities.*
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
        if (User.all().count() < 2) {
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
        }

        if (TestObject.find {
                objectName eq "Перчатки 6кВ"
            }.empty()) {
            TestObject.new {
                objectName = "Перчатки 6кВ"
                objectVoltage = "6000"
                objectAmperage = "6.0"
                objectTime = "60"
                objectModule = MODULE_2
                objectTransformer = "20000"
                objectTest = TEST_4
            }
        }

        if (TestObject.find {
                objectName eq "Перчатки 9кВ"
            }.empty()) {
            TestObject.new {
                objectName = "Перчатки 9кВ"
                objectVoltage = "9000"
                objectAmperage = "9.0"
                objectTime = "60"
                objectModule = MODULE_2
                objectTransformer = "20000"
                objectTest = TEST_4
            }
        }

        if (TestObject.find {
                objectName eq "Боты 15кВ"
            }.empty()) {
            TestObject.new {
                objectName = "Боты 15кВ"
                objectVoltage = "15000"
                objectAmperage = "7.0"
                objectTime = "60"
                objectModule = MODULE_2
                objectTransformer = "20000"
                objectTest = TEST_4
            }
        }

        if (TestObject.find {
                objectName eq "Боты 20кВ"
            }.empty()) {
            TestObject.new {
                objectName = "Боты 20кВ"
                objectVoltage = "20000"
                objectAmperage = "10.0"
                objectTime = "60"
                objectModule = MODULE_2
                objectTransformer = "20000"
                objectTest = TEST_4
            }
        }

        if (TestObject.find {
                objectName eq "Галоши"
            }.empty()) {
            TestObject.new {
                objectName = "Галоши"
                objectVoltage = "3500"
                objectAmperage = "2.0"
                objectTime = "60"
                objectModule = MODULE_2
                objectTransformer = "20000"
                objectTest = TEST_4
            }
        }
    }
}
