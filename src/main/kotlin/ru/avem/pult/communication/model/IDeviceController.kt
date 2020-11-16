package ru.avem.pult.communication.model

import mu.KotlinLogging
import ru.avem.pult.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.pult.communication.utils.TransportException

interface IDeviceController {
    val name: String

    val protocolAdapter: ModbusRTUAdapter

    val id: Byte

    var isResponding: Boolean

    var requestTotalCount: Int
    var requestSuccessCount: Int

    fun readRegister(register: DeviceRegister)
    fun <T : Number> writeRegister(register: DeviceRegister, value: T)
    fun readAllRegisters()
    fun writeRegisters(register: DeviceRegister, values: List<Short>)
    val pollingRegisters: MutableList<DeviceRegister>
    val writingRegisters: MutableList<Pair<DeviceRegister, Number>>
    val pollingMutex: Any
    val writingMutex: Any

    fun IDeviceController.transactionWithAttempts(block: () -> Unit) {
        var attempt = 0
        val connection = protocolAdapter.connection

        while (attempt++ < connection.attemptCount) {
            requestTotalCount++

            try {
                block()
                requestSuccessCount++
                break
            } catch (e: TransportException) {
                val message =
                    "repeat $attempt/${connection.attemptCount} attempts with common success rate = ${(requestSuccessCount) * 100 / requestTotalCount}%"
                KotlinLogging.logger(name).info(message)

                if (attempt == connection.attemptCount) {
                    throw TransportException(message)
                }
            }
        }
    }

    fun getRegisterById(idRegister: String): DeviceRegister

    fun addPollingRegister(register: DeviceRegister) {
        synchronized(pollingMutex) {
            pollingRegisters.add(register)
        }
    }

    fun addWritingRegister(writingPair: Pair<DeviceRegister, Number>) {
        synchronized(writingMutex) {
            writingRegisters.add(writingPair)
        }
    }

    fun removePollingRegister(register: DeviceRegister) {
        synchronized(pollingMutex) {
            pollingRegisters.remove(register)
        }
    }

    fun removeAllPollingRegisters() {
        synchronized(pollingMutex) {
            pollingRegisters.forEach(DeviceRegister::deleteObservers)
            pollingRegisters.clear()
        }
    }

    fun removeAllWritingRegisters() {
        synchronized(writingMutex) {
            writingRegisters.forEach {
                it.first.deleteObservers()
            }
            writingRegisters.clear()
        }
    }

    fun readPollingRegisters() {
        synchronized(pollingMutex) {
            pollingRegisters.forEach {
                readRegister(it)
            }
        }
    }

    fun writeWritingRegisters() {
        synchronized(writingMutex) {
            writingRegisters.forEach {
                writeRegister(it.first, it.second)
            }
        }
    }

    fun checkResponsibility()
}
