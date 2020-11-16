package ru.avem.pult.communication.adapters.modbusrtu.requests

import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_CRC
import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_DEVICE_ID
import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_FUNCTION
import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER
import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.BYTE_SIZE_OF_REGISTER_ID
import ru.avem.pult.communication.adapters.modbusrtu.requests.ModbusRtuRequest.Companion.REGISTER_ID_POSITION
import ru.avem.pult.communication.adapters.modbusrtu.utils.CRC
import ru.avem.pult.communication.adapters.utils.ModbusRegister
import ru.avem.pult.communication.utils.LogicException
import ru.avem.pult.communication.utils.toShort
import java.nio.ByteBuffer

class PresetSingleRegister(override val deviceId: Byte, override val registerId: Short, val registerData: ModbusRegister) :
    ModbusRtuRequest {
    companion object {
        const val FUNCTION_CODE: Byte = 0x06
    }

    override val function: Byte = FUNCTION_CODE
    private val registerDataPosition = REGISTER_ID_POSITION + BYTE_SIZE_OF_REGISTER_ID

    override fun getRequestBytes(): ByteArray = ByteBuffer.allocate(getRequestSize()).apply {
        put(deviceId)
        put(function)
        putShort(registerId)
        putShort(registerData.toShort())
    }.also {
        CRC.sign(it)
    }.array()

    override fun getRequestSize() =
        BYTE_SIZE_OF_DEVICE_ID +
                BYTE_SIZE_OF_FUNCTION +
                BYTE_SIZE_OF_REGISTER_ID +
                BYTE_SIZE_OF_REGISTER +
                BYTE_SIZE_OF_CRC

    override fun getResponseSize() = getRequestSize()

    fun parseResponse(response: ByteArray) {
        checkResponse(response)
        checkRegisterId((response[REGISTER_ID_POSITION + 0] to response[REGISTER_ID_POSITION + 1]).toShort())
        checkRegisterData(ModbusRegister(response[registerDataPosition + 0], response[registerDataPosition + 1]))
    }

    private fun checkRegisterData(registerDataFromResponse: ModbusRegister) {
        if (registerData != registerDataFromResponse) {
            throw LogicException("Ошибка ответа: неправильный registerData[$registerDataFromResponse] вместо [$registerData]")
        }
    }
}
