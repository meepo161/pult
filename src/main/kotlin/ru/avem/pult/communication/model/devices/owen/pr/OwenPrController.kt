package ru.avem.pult.communication.model.devices.owen.pr

import ru.avem.pult.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.pult.communication.adapters.utils.ModbusRegister
import ru.avem.pult.communication.model.DeviceController
import ru.avem.pult.communication.model.DeviceRegister
import ru.avem.pult.communication.utils.TransportException
import ru.avem.pult.communication.utils.TypeByteOrder
import ru.avem.pult.communication.utils.allocateOrderedByteBuffer
import ru.avem.pult.communication.utils.toBoolean
import ru.avem.pult.utils.getRange
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.pow

class OwenPrController(
    override val name: String,
    override val protocolAdapter: ModbusRTUAdapter,
    override val id: Byte
) : DeviceController() {
    val model = OwenPrModel()
    override var isResponding = false
    override var requestTotalCount = 0
    override var requestSuccessCount = 0
    override val pollingRegisters = mutableListOf<DeviceRegister>()
    override val writingMutex = Any()
    override val writingRegisters = mutableListOf<Pair<DeviceRegister, Number>>()
    override val pollingMutex = Any()

    var outMask: Short = 0
    var outMaskPRM: Short = 0

    companion object {
        const val TRIG_RESETER: Short = 0xFFFF.toShort()
        const val WD_RESETER: Short = 2
    }

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                when (register.valueType) {
                    DeviceRegister.RegisterValueType.SHORT -> {
                        val modbusRegister =
                            protocolAdapter.readHoldingRegisters(id, register.address, 1).map(ModbusRegister::toShort)
                        register.value = modbusRegister.first()
                    }
                    DeviceRegister.RegisterValueType.FLOAT -> {
                        val modbusRegister =
                            protocolAdapter.readHoldingRegisters(id, register.address, 2).map(ModbusRegister::toShort)
                        register.value =
                            allocateOrderedByteBuffer(modbusRegister, TypeByteOrder.MID_LITTLE_ENDIAN, 4).float
                    }
                    DeviceRegister.RegisterValueType.INT32 -> TODO()
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun readAllRegisters() {
        model.registers.values.forEach {
            readRegister(it)
        }
    }

    override fun <T : Number> writeRegister(register: DeviceRegister, value: T) {
        isResponding = try {
            when (value) {
                is Float -> {
                    val bb = ByteBuffer.allocate(4).putFloat(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Int -> {
                    val bb = ByteBuffer.allocate(4).putInt(value).order(ByteOrder.LITTLE_ENDIAN)
                    val registers = listOf(ModbusRegister(bb.getShort(2)), ModbusRegister(bb.getShort(0)))
                    transactionWithAttempts {
                        protocolAdapter.presetMultipleRegisters(id, register.address, registers)
                    }
                }
                is Short -> {
                    transactionWithAttempts {
                        protocolAdapter.presetSingleRegister(id, register.address, ModbusRegister(value))
                    }
                }
                else -> {
                    throw UnsupportedOperationException("Method can handle only with Float, Int and Short")
                }
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun writeRegisters(register: DeviceRegister, values: List<Short>) {
        val registers = values.map { ModbusRegister(it) }
        isResponding = try {
            transactionWithAttempts {
                protocolAdapter.presetMultipleRegisters(id, register.address, registers)
            }
            true
        } catch (e: TransportException) {
            false
        }
    }

    override fun checkResponsibility() {
        model.registers.values.firstOrNull()?.let {
            readRegister(it)
        }
    }

    override fun getRegisterById(idRegister: String) = model.getRegisterById(idRegister)

    private fun onBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask)
    }

    private fun offBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask)
    }

    private fun onBitInRegisterPRM(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMaskPRM = outMaskPRM or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMaskPRM)
    }

    private fun offBitInRegisterPRM(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMaskPRM = outMaskPRM and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMaskPRM)
    }

    fun resetTriggers() {
        with(getRegisterById(OwenPrModel.DI_01_16_RST)) {
            writeRegister(this, TRIG_RESETER)
        }
        with(getRegisterById(OwenPrModel.DI_17_32_RST)) {
            writeRegister(this, TRIG_RESETER)
        }
        with(getRegisterById(OwenPrModel.WD_TIMEOUT)) {
            writeRegister(this, 5000.toShort())
        }
        with(getRegisterById(OwenPrModel.CMD)) {
            writeRegister(this, WD_RESETER)
        }
    }

    fun presetGeneralProtectionsMasks() {
        with(getRegisterById(OwenPrModel.DI_01_16_ERROR_MASK_1)) {
            writeRegister(this, 112.toShort())
        }
        with(getRegisterById(OwenPrModel.DI_17_32_ERROR_S1_MASK_1)) {
            writeRegister(this, 1.toShort())
        }
        with(getRegisterById(OwenPrModel.DO_01_16_ERROR_S1_MASK_0)) {
            writeRegister(this, 158.toShort())
        }
        with(getRegisterById(OwenPrModel.DO_17_32_ERROR_S1_MASK_0)) {
            writeRegister(this, 3.toShort())
        }
    }

    fun onLampPower() {
        with(getRegisterById(OwenPrModel.LAMP_CONTROL)) {
            onBitInRegister(this, 1)
        }
    }

    fun offLampPower() {
        with(getRegisterById(OwenPrModel.LAMP_CONTROL)) {
            offBitInRegister(this, 1)
        }
    }

    fun onArnPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 3)
        }
    }

    fun offArnPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 3)
        }
    }

    fun onImpulsePower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 5)
        }
    }

    fun offImpulsePower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 5)
        }
    }

    fun onViuPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 4)
        }
    }

    fun offViuPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 4)
        }
    }

    fun onSoundAlarm() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 6)
        }
    }

    fun offSoundAlarm() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 6)
        }
    }

    fun onProtectionsLamp() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 7)
        }
    }

    fun offProtectionsLamp() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 7)
        }
    }

    fun onImpulseShortlocker() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 8)
        }
    }

    fun offImpulseShortlocker() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 8)
        }
    }

    fun onViuShortlocker() {
        with(getRegisterById(OwenPrModel.DO_17_32)) {
            onBitInRegister(this, 1)
        }
    }

    fun offViuShortlocker() {
        with(getRegisterById(OwenPrModel.DO_17_32)) {
            offBitInRegister(this, 1)
        }
    }

    fun onImpulsePlate() {
        with(getRegisterById(OwenPrModel.DO_17_32)) {
            onBitInRegisterPRM(this, 2)
        }
    }

    fun offImpulsePlate() {
        with(getRegisterById(OwenPrModel.DO_17_32)) {
            offBitInRegisterPRM(this, 2)
        }
    }

    fun isSectionDoorOpened(): Boolean {
        with(getRegisterById(OwenPrModel.DI_17_32_RAW)) {
            return value.toInt().getRange(0).toBoolean()
        }
    }

    fun isImpulsePowerOn(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(2).toBoolean()
        }
    }

    fun isViuPowerOn(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(3).toBoolean()
        }
    }

    fun isDoorOpened(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(4).toBoolean()
        }
    }

    fun isKa1Triggered(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(5).toBoolean()
        }
    }

    fun isKa2Triggered(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(6).toBoolean()
        }
    }

    fun isImpulsePcbPowerOn(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(7).toBoolean()
        }
    }

    fun getAmperageSensor1Value(): Double {
        with(getRegisterById(OwenPrModel.AI_01_F)) {
            return value.toDouble()
        }
    }

    fun getAmperageSensor2Value(): Double {
        with(getRegisterById(OwenPrModel.AI_03_F)) {
            return value.toDouble()
        }
    }
}
