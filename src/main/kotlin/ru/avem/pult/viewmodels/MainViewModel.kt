package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
import ru.avem.pult.entities.ConnectionPoint
import ru.avem.pult.database.entities.*
import tornadofx.ViewModel
import tornadofx.controlsfx.warningNotification
import tornadofx.observable
import tornadofx.observableList
import java.nio.file.Path
import java.nio.file.Paths

class MainViewModel : ViewModel() {
    override val configPath: Path = Paths.get("conf/.properties")

    companion object {
        val TEST_1 = "Испытание повышенным напряжением рабочей части указателя напряжения до 3000 В"
        val TEST_2 = "Испытание (проверка) напряжения зажигания до 200 В"
        val TEST_3 = "Испытание изоляции соединительных проводов"
        val TEST_4 = "Испытание защитных средств из диэлектрической резины"
        val TEST_5 = "Испытание слесарно-монтажного инструмента с изолирующими рукоятками"
        val TEST_6 = "Испытание повышенным напряжением изолирующей части указателя >1000 В"
        val TEST_7 = "Испытание (проверка) напряжения зажигания указателей >1000 В"
        val TEST_8 = "Испытание изолирующих штанг"
        val TEST_9 = "Испытание ОПН и разрядников"
        val TEST_10 = "Испытание повышенным напряжением рабочей части указателя напряжения до 200 В"

        val MODULE_1 = "Модуль 1 (Ввод питания и испытания указателей напряжения)"
        val MODULE_2 = "Модуль 2 (Большая ванна)"
        val MODULE_3 = "Модуль 3 (Испытания штанг и маленькая ванна)"

        val TYPE_1_VOLTAGE = 200
        val TYPE_2_VOLTAGE = 20000
        val TYPE_3_VOLTAGE = 100000

        val CONNECTION_1 = "CONNECTION_1"
        val CONNECTION_2 = "CONNECTION_2"
        val CONNECTION_3 = "CONNECTION_3"
        val CONNECTION_4 = "CONNECTION_4"
    }

    val coefficientsSettingsModel: CoefficientsSettingsViewModel by inject()
    private val latrSettingsModel: LatrSettingsViewModel by inject()

    val usersList: ObservableList<User>
        get() {
            return transaction {
                User.find {
                    Users.login notLike "admin"
                }.toMutableList().observable()
            }
        }
    val testObjectsList: ObservableList<TestObject>
        get() {
            return transaction {
                TestObject.all().toMutableList().observable()
            }
        }
    val protocols: ObservableList<Protocol>
        get() {
            return transaction {
                Protocol.all().toMutableList().reversed().observable()
            }
        }
    val protocolFields: ObservableList<ProtocolField>
        get() {
            return transaction {
                ProtocolField.all().toMutableList().observable()
            }
        }

    var protocolTemplatePath = SimpleStringProperty(config.getProperty("template_path", ""))

    val modulesList = observableList(MODULE_1, MODULE_2, MODULE_3)
    val transformatorsList = observableList(TYPE_1_VOLTAGE, TYPE_2_VOLTAGE, TYPE_3_VOLTAGE)
    val firstModuleTransformators = observableList(TYPE_1_VOLTAGE.toString(), TYPE_2_VOLTAGE.toString())
    val secondModuleTransformators = observableList(TYPE_2_VOLTAGE.toString())
    val thirdModuleTransformators = observableList(TYPE_3_VOLTAGE.toString())
    val firstModuleTestsList = observableList(TEST_1, TEST_2, TEST_10)
    val secondModuleTestsList = observableList(TEST_3, TEST_4, TEST_5)
    val thirdModuleTestsList = observableList(TEST_1, TEST_3, TEST_6, TEST_7, TEST_8, TEST_9)
    val firstModuleTestsList200V = observableList(TEST_2, TEST_10)
    val firstModuleTestsList20kV = observableList(TEST_1)
    val secondModuleTestsList20kV = observableList(TEST_3, TEST_4, TEST_5)
    val secondModuleTestsList50kV = observableList(TEST_3)
    val selectedConnectionPoints = mutableMapOf<String, ConnectionPoint>()

    val authorizedUser = SimpleObjectProperty<User>()
    val factoryNumber = SimpleStringProperty()
    val module = SimpleStringProperty()
    var test = SimpleStringProperty()
    val testObject = SimpleObjectProperty<TestObject>()
    val user = SimpleObjectProperty<User>()
    val connectionPoint1 = ConnectionPoint()
    val connectionPoint2 = ConnectionPoint()
    val connectionPoint3 = ConnectionPoint()
    val connectionPoint4 = ConnectionPoint()
    val manualVoltageRegulation = SimpleBooleanProperty()

    fun performActionByAdmin(performAction: () -> Unit) {
        if (authorizedUser.value.login == "admin") {
            performAction()
        } else {
            warningNotification(
                "Внимание!",
                "Эта операция разрешена только администратору.",
                Pos.BOTTOM_CENTER
            )
        }
    }

    fun getLatrParameters(): LatrSettingsFragmentModel {
        when {
            testObject.value.objectTransformer == TYPE_1_VOLTAGE.toString() -> {
                return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE_1_FRAGMENT).model
            }

            testObject.value.objectTransformer == TYPE_2_VOLTAGE.toString() -> {
                when {
                    testObject.value.objectVoltage.toInt() <= 1000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U1_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 3000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U3_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 5000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U5_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 8000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U8_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 11000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U11_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 13000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U13_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 15000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U15_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 17000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_U17_FRAGMENT).model
                    }
                    else -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE2_LAST_FRAGMENT).model
                    }
                }
            }

            testObject.value.objectTransformer == TYPE_3_VOLTAGE.toString() -> {
                when {
                    testObject.value.objectVoltage.toInt() <= 1000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U1_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 3000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U3_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 5000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U5_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 8000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U8_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 11000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U11_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 13000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U13_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 15000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U15_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 17000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U17_FRAGMENT).model
                    }
                    testObject.value.objectVoltage.toInt() <= 20000 -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_U20_FRAGMENT).model
                    }
                    else -> {
                        return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE3_LAST_FRAGMENT).model
                    }
                }
            }

            else -> {
                return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE_1_FRAGMENT).model
            }
        }
    }
}
