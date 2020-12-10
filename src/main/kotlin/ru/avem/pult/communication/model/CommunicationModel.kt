package ru.avem.pult.communication.model

import ru.avem.pult.app.MainApp.Companion.isAppRunning
import ru.avem.pult.communication.Connection
import ru.avem.pult.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.pult.communication.model.devices.avem.avem4.Avem4Controller
import ru.avem.pult.communication.model.devices.avem.avem7.Avem7Controller
import ru.avem.pult.communication.model.devices.avem.latr.AvemLatrController
import ru.avem.pult.communication.model.devices.owen.pr.OwenPrController
import ru.avem.pult.communication.utils.SerialParameters
import java.lang.Thread.sleep
import kotlin.concurrent.thread

object CommunicationModel {
    @Suppress("UNUSED_PARAMETER")
    enum class DeviceID(description: String) {
        PV21("Напряжение на ОИ"),
        PA11("Ток утечки"),
        DD1("ПР200"),
        GV240("АРН")
    }

    private var isConnected = false

    private val connection = Connection(
        adapterName = "CP2103 USB to RS-485",
        serialParameters = SerialParameters(8, 0, 1, 38400),
        timeoutRead = 200,
        timeoutWrite = 200,
        attemptCount = 10
    ).apply {
        connect()
        isConnected = true
    }

    private val adapter = ModbusRTUAdapter(connection)

    private val deviceControllers: Map<DeviceID, IDeviceController> = mapOf(
        DeviceID.DD1 to OwenPrController(DeviceID.DD1.toString(), adapter, 1),
        DeviceID.PV21 to Avem4Controller(DeviceID.PV21.toString(), adapter, 21),
        DeviceID.PA11 to Avem7Controller(DeviceID.PA11.toString(), adapter, 11),
        DeviceID.GV240 to AvemLatrController(DeviceID.GV240.toString(), adapter, 240.toByte())
    )

    init {
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.readPollingRegisters()
                    }
                }
                sleep(1)
            }
        }
        thread(isDaemon = true) {
            while (isAppRunning) {
                if (isConnected) {
                    deviceControllers.values.forEach {
                        it.writeWritingRegisters()
                    }
                }
                sleep(1)
            }
        }
    }

    fun getDeviceById(deviceID: DeviceID) = deviceControllers[deviceID] ?: error("Не определено $deviceID")

    fun startPoll(deviceID: DeviceID, registerID: String, block: (Number) -> Unit) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.addObserver { _, arg ->
            block(arg as Number)
        }
        device.addPollingRegister(register)
    }

    fun clearPollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
    }

    fun clearWritingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllWritingRegisters)
    }

    fun removePollingRegister(deviceID: DeviceID, registerID: String) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        register.deleteObservers()
        device.removePollingRegister(register)
        register.value = -1
    }

    fun removePollingRegisters() {
        deviceControllers.values.forEach(IDeviceController::removeAllPollingRegisters)
    }

    fun checkDevices(): List<DeviceID> {
        deviceControllers.values.forEach(IDeviceController::checkResponsibility)
        return deviceControllers.filter { !it.value.isResponding }.keys.toList()
    }

    fun addWritingRegister(deviceID: DeviceID, registerID: String, value: Number) {
        val device = getDeviceById(deviceID)
        val register = device.getRegisterById(registerID)
        device.addWritingRegister(register to value)
    }
}
