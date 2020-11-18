package ru.avem.pult.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.database.entities.TestObjects.objectName
import ru.avem.pult.database.entities.Users.login
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
    }
}
