package ru.avem.pult.communication.model.devices.avem.latr

import ru.avem.pult.communication.model.DeviceRegister
import ru.avem.pult.communication.model.IDeviceModel

class AvemLatrModel : IDeviceModel {
    override val registers: Map<String, DeviceRegister> = mapOf(
        IR_LIMIT_SWITCH to DeviceRegister(0x1119, DeviceRegister.RegisterValueType.SHORT),
        IR_SET_VALUE to DeviceRegister(0x111A, DeviceRegister.RegisterValueType.FLOAT, "В"),
        IR_DUTY_MAX_PERCENT to DeviceRegister(0x115A, DeviceRegister.RegisterValueType.FLOAT, "%"),
        IR_DUTY_MIN_PERCENT to DeviceRegister(0x115C, DeviceRegister.RegisterValueType.FLOAT, "%"),
        IR_TIME_REGULATION to DeviceRegister(0x1120, DeviceRegister.RegisterValueType.INT32, "мс"),
        IR_CORRIDOR to DeviceRegister(0x1122, DeviceRegister.RegisterValueType.FLOAT),
        IR_DELTA to DeviceRegister(0x1124, DeviceRegister.RegisterValueType.FLOAT, "%"),
        IR_TIME_PULSE_MIN to DeviceRegister(0x1128, DeviceRegister.RegisterValueType.INT32, "мс"),
        IR_TIME_PULSE_MAX to DeviceRegister(0x112A, DeviceRegister.RegisterValueType.INT32, "мс"),

        IR_TIME_PERIOD_MAX to DeviceRegister(0x115E, DeviceRegister.RegisterValueType.FLOAT),
        IR_TIME_PERIOD_MIN to DeviceRegister(0x1160, DeviceRegister.RegisterValueType.FLOAT),

        IR_VOLTAGE_LIMIT_MIN to DeviceRegister(0x112C, DeviceRegister.RegisterValueType.FLOAT, "В"),
        IR_START_STOP to DeviceRegister(0x112E, DeviceRegister.RegisterValueType.INT32),
        IR_START to DeviceRegister(0x112E, DeviceRegister.RegisterValueType.SHORT),
        IR_RESET to DeviceRegister(0x112F, DeviceRegister.RegisterValueType.SHORT),
        DEVICE_STATUS to DeviceRegister(0x1024, DeviceRegister.RegisterValueType.INT32),
        RMS_VOLTAGE to DeviceRegister(0x1004, DeviceRegister.RegisterValueType.FLOAT, "В"),
        SOFTWARE_DATE to DeviceRegister(0x1022, DeviceRegister.RegisterValueType.INT32),
        IR_MAX_VOLTAGE_KEYS to DeviceRegister(0x1170, DeviceRegister.RegisterValueType.FLOAT, "В"),
        IR_MODE to DeviceRegister(0x116A, DeviceRegister.RegisterValueType.INT32)
    )

    companion object {
        const val IR_LIMIT_SWITCH = "IR_LIMIT_SWITCH"
        const val IR_SET_VALUE = "IR_SET_VALUE"
        const val IR_DUTY_MAX_PERCENT = "IR_DUTY_MAX_PERCENT"
        const val IR_DUTY_MIN_PERCENT = "IR_DUTY_MIN_PERCENT"
        const val IR_TIME_REGULATION = "IR_TIME_REGULATION"
        const val IR_CORRIDOR = "IR_CORRIDOR"
        const val IR_DELTA = "IR_DELTA"
        const val IR_TIME_PULSE_MIN = "IR_TIME_PULSE_MIN"
        const val IR_TIME_PULSE_MAX = "IR_TIME_PULSE_MAX"
        const val IR_TIME_PERIOD_MAX = "IR_TIME_PERIOD_MAX"
        const val IR_TIME_PERIOD_MIN = "IR_TIME_PERIOD_MIN"
        const val IR_VOLTAGE_LIMIT_MIN = "IR_VOLTAGE_LIMIT_MIN"
        const val IR_START_STOP = "IR_START_STOP"
        const val IR_START = "IR_START"
        const val IR_RESET = "IR_RESET"
        const val DEVICE_STATUS = "DEVICE_STATUS"
        const val RMS_VOLTAGE = "RMS_VOLTAGE"
        const val SOFTWARE_DATE = "SOFTWARE_DATE"
        const val IR_MAX_VOLTAGE_KEYS = "IR_MAX_VOLTAGE_KEYS"
        const val IR_MODE = "IR_MODE"
    }

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}
