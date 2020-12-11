package ru.avem.pult.communication.model.devices.owen.pr

import ru.avem.pult.communication.model.DeviceRegister
import ru.avem.pult.communication.model.IDeviceModel

class OwenPrModel : IDeviceModel {
    companion object {
        const val DI_01_16_TRIG = "DI_01_16_TRIG"
        const val DI_01_16_TRIG_INV = "DI_01_16_TRIG_INV"
        const val DI_01_16_RAW = "DI_01_16_RAW"
        const val DI_01_16_RST = "DI_01_16_RST"
        const val DO_01_16 = "DO_01_16"
        const val DO_01_16_ERROR_S1_MASK_1 = "DO_01_16_ERROR_S1_MASK_1"
        const val DO_01_16_ERROR_S1_MASK_0 = "DO_01_16_ERROR_S1_MASK_0"
        const val DO_01_16_ERROR_S2_MASK_1 = "DO_01_16_ERROR_S2_MASK_1"
        const val DO_01_16_ERROR_S2_MASK_0 = "DO_01_16_ERROR_S2_MASK_0"
        const val DO_01_16_ERROR_S3_MASK_1 = "DO_01_16_ERROR_S3_MASK_1"
        const val DO_01_16_ERROR_S3_MASK_0 = "DO_01_16_ERROR_S3_MASK_0"
        const val DO_01_16_ERROR_S4_MASK_1 = "DO_01_16_ERROR_S4_MASK_1"
        const val DO_01_16_ERROR_S4_MASK_0 = "DO_01_16_ERROR_S4_MASK_0"
        const val DI_17_32_TRIG = "DI_17_32_TRIG"
        const val DI_17_32_TRIG_INV = "DI_17_32_TRIG_INV"
        const val DI_17_32_RAW = "DI_17_32_RAW"
        const val DI_17_32_RST = "DI_17_32_RST"
        const val DI_01_16_ERROR_MASK_1 = "DI_01_16_ERROR_S1_MASK_1"
        const val DI_01_16_ERROR_MASK_0 = "DI_01_16_ERROR_S1_MASK_0"
        const val DI_17_32_ERROR_S1_MASK_1 = "DI_17_32_ERROR_S1_MASK_1"
        const val DI_17_32_ERROR_S1_MASK_0 = "DI_17_32_ERROR_S1_MASK_0"
        const val DO_ERROR_S1_TIME = "DO_ERROR_S1_TIME"
        const val DO_ERROR_S2_TIME = "DO_ERROR_S2_TIME"
        const val DO_ERROR_S3_TIME = "DO_ERROR_S3_TIME"
        const val DO_ERROR_S4_TIME = "DO_ERROR_S4_TIME"
        const val AI_01_F = "AI_01_F"
        const val AI_02_F = "AI_02_F"
        const val AI_03_F = "AI_03_F"
        const val AI_04_F = "AI_04_F"
        const val WD_TIMEOUT = "WD_TIMEOUT"
        const val CMD = "CMD"
        const val STATE = "STATE"
    }

    override val registers: Map<String, DeviceRegister> = mapOf(
        DI_01_16_RAW to DeviceRegister(516, DeviceRegister.RegisterValueType.SHORT),
        DI_01_16_RST to DeviceRegister(517, DeviceRegister.RegisterValueType.SHORT),
        DI_01_16_TRIG to DeviceRegister(518, DeviceRegister.RegisterValueType.SHORT),
        DI_01_16_TRIG_INV to DeviceRegister(519, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_TRIG to DeviceRegister(522, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_TRIG_INV to DeviceRegister(523, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_RAW to DeviceRegister(520, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_RST to DeviceRegister(521, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16 to DeviceRegister(512, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S1_MASK_1 to DeviceRegister(553, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S1_MASK_0 to DeviceRegister(554, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S2_MASK_1 to DeviceRegister(558, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S2_MASK_0 to DeviceRegister(559, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S3_MASK_1 to DeviceRegister(563, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S3_MASK_0 to DeviceRegister(564, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S4_MASK_1 to DeviceRegister(568, DeviceRegister.RegisterValueType.SHORT),
        DO_01_16_ERROR_S4_MASK_0 to DeviceRegister(569, DeviceRegister.RegisterValueType.SHORT),
        DI_01_16_ERROR_MASK_1 to DeviceRegister(547, DeviceRegister.RegisterValueType.SHORT),
        DI_01_16_ERROR_MASK_0 to DeviceRegister(548, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_ERROR_S1_MASK_1 to DeviceRegister(549, DeviceRegister.RegisterValueType.SHORT),
        DI_17_32_ERROR_S1_MASK_0 to DeviceRegister(550, DeviceRegister.RegisterValueType.SHORT),
        DO_ERROR_S1_TIME to DeviceRegister(557, DeviceRegister.RegisterValueType.SHORT),
        DO_ERROR_S2_TIME to DeviceRegister(562, DeviceRegister.RegisterValueType.SHORT),
        DO_ERROR_S3_TIME to DeviceRegister(567, DeviceRegister.RegisterValueType.SHORT),
        DO_ERROR_S4_TIME to DeviceRegister(572, DeviceRegister.RegisterValueType.SHORT),
        AI_01_F to DeviceRegister(531, DeviceRegister.RegisterValueType.FLOAT),
        AI_02_F to DeviceRegister(533, DeviceRegister.RegisterValueType.FLOAT),
        AI_03_F to DeviceRegister(535, DeviceRegister.RegisterValueType.FLOAT),
        AI_04_F to DeviceRegister(537, DeviceRegister.RegisterValueType.FLOAT),
        WD_TIMEOUT to DeviceRegister(573, DeviceRegister.RegisterValueType.SHORT),
        CMD to DeviceRegister(574, DeviceRegister.RegisterValueType.SHORT),
        STATE to DeviceRegister(575, DeviceRegister.RegisterValueType.SHORT)
    )

    override fun getRegisterById(idRegister: String) =
        registers[idRegister] ?: error("Такого регистра нет в описанной карте $idRegister")
}