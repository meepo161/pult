package ru.avem.pult.viewmodels

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.transactions.transaction
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
        val TEST_1 = "Проверка качества изоляции повышенным напряжением промышленной частоты"
        val TEST_2 = "Проверка качества изоляции импульсным напряжением промышленной частоты"

        val TYPE_1_VOLTAGE = 6000
        val TYPE_2_VOLTAGE = 72000
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

    val testList = observableList(TEST_1, TEST_2)
    val transformatorsList = observableList(TYPE_1_VOLTAGE, TYPE_2_VOLTAGE)

    val authorizedUser = SimpleObjectProperty<User>()
    val factoryNumber = SimpleStringProperty()
    var test = SimpleStringProperty()
    val testObject = SimpleObjectProperty<TestObject>()
    val user = SimpleObjectProperty<User>()
    val isManualVoltageRegulation = SimpleBooleanProperty()

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

            else -> {
                return latrSettingsModel.fragments.getValue(LatrSettingsViewModel.MODULE_1_FRAGMENT).model
            }
        }
    }
}
