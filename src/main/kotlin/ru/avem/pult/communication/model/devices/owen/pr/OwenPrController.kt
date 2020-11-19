package ru.avem.pult.communication.model.devices.owen.pr

import ru.avem.pult.communication.adapters.modbusrtu.ModbusRTUAdapter
import ru.avem.pult.communication.adapters.utils.ModbusRegister
import ru.avem.pult.communication.model.DeviceController
import ru.avem.pult.communication.model.DeviceRegister
import ru.avem.pult.communication.utils.TransportException
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

    companion object {
        const val TRIG_RESETER: Short = 0xFFFF.toShort()
        const val WD_RESETER: Short = 2
    }

    override fun readRegister(register: DeviceRegister) {
        isResponding = try {
            transactionWithAttempts {
                val modbusRegister =
                    protocolAdapter.readHoldingRegisters(id, register.address, 1).map(ModbusRegister::toShort)
                register.value = modbusRegister.first()
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

    fun onBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask or 2.0.pow(nor).toInt().toShort()
        writeRegister(register, outMask)
    }

    fun offBitInRegister(register: DeviceRegister, bitPosition: Short) {
        val nor = bitPosition - 1
        outMask = outMask and 2.0.pow(nor).toInt().inv().toShort()
        writeRegister(register, outMask)
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
        with(getRegisterById(OwenPrModel.DI_01_16_ERROR_MASK_0)) {
            writeRegister(this, 112.toShort())
        }
        with(getRegisterById(OwenPrModel.DO_01_16_ERROR_S1_MASK_0)) {
            writeRegister(this, 30.toShort())
        }
    }

    fun turnOnLampLess1000() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 8)
        }
    }

    fun turnOffLampLess1000() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 8)
        }
    }

    fun turnOnLampMore1000() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 9)
        }
    }

    fun turnOffLampMore1000() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 9)
        }
    }

    fun onShortlocker20kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 10)
        }
    }

    fun offShortlocker20kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 10)
        }
    }

    fun onSoundAlarm() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 12)
        }
    }

    fun offSoundAlarm() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 12)
        }
    }

    fun onTransformer200V() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 1)
        }
    }

    fun offTransformer200V() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 1)
        }
    }

    fun onTransformer20kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 2)
        }
    }

    fun offTransformer20kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 2)
        }
    }

    fun onTransformer50kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 3)
        }
    }

    fun offTransformer50kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 3)
        }
    }

    fun offStepDownTransformer() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 4)
        }
    }

    fun onStepDownTransformer() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 4)
        }
    }

    fun onButtonPostPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 6)
        }
    }

    fun offButtonPostPower() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 6)
        }
    }

    fun onBathChannel1() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 4)
        }
    }

    fun offBathChannel1() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 4)
        }
    }

    fun onBathChannel2() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 5)
        }
    }

    fun offBathChannel2() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 5)
        }
    }

    fun onBathChannel3() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 6)
        }
    }

    fun offBathChannel3() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 6)
        }
    }

    fun onBathChannel4() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 7)
        }
    }

    fun offBathChannel4() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 7)
        }
    }

    fun togglePowerSupplyMode() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 3)
            offBitInRegister(this, 3)
        }
    }

    fun onShortlocker50kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 5)
        }
    }

    fun offShortlocker50kV() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 5)
        }
    }

    fun unlockBathLock() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 1)
            offBitInRegister(this, 1)
        }
    }

    fun onLightSign() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 13)
        }
    }

    fun offLightSign() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 13)
        }
    }

    fun onTimer() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            onBitInRegister(this, 14)
            onBitInRegister(this, 7)
        }
    }

    fun offTimer() {
        with(getRegisterById(OwenPrModel.DO_01_16)) {
            offBitInRegister(this, 7)
            offBitInRegister(this, 14)
        }
    }

    fun isTimerStartPressed(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(12).toBoolean()
        }
    }

    fun isTimerStopPressed(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(13).toBoolean()
        }
    }

    fun is20kVshortlockerSwitched(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(5).toBoolean()
        }
    }

    fun is200VcontactorSwitched(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(9).toBoolean()
        }
    }

    fun isDoorOpened(): Boolean {
        return !getRegisterById(OwenPrModel.DI_01_16_RAW).value.toInt().getRange(7).toBoolean()
    }

    fun isTotalAmperageProtectionTriggered(): Boolean {
        return getRegisterById(OwenPrModel.DI_01_16_TRIG).value.toInt().getRange(3).toBoolean()
    }

    fun isHiSwitchTurned(): Boolean {
        return getRegisterById(OwenPrModel.DI_01_16_RAW).value.toInt().getRange(8).toBoolean()
    }

    fun isStopPressed(): Boolean {
        return getRegisterById(OwenPrModel.DI_01_16_TRIG).value.toInt().getRange(1).toBoolean()
    }

    fun isLatrContactorSwitched(): Boolean {
        return getRegisterById(OwenPrModel.DI_01_16_RAW).value.toInt().getRange(2).toBoolean()
    }

    fun isGeneralAmmeterRelayTriggered(): Boolean {
        return getRegisterById(OwenPrModel.DI_01_16_TRIG).value.toInt().getRange(0).toBoolean()
    }

    fun is50kVcontactorSwitched(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(11).toBoolean()
        }
    }

    fun is20kVcontactorSwitched(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(10).toBoolean()
        }
    }

    fun isBathDoorClosed(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_RAW)) {
            return value.toInt().getRange(4).toBoolean() && !value.toInt().getRange(5).toBoolean()
        }
    }

    fun isBathChannelTriggered1(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(0).toBoolean()
        }
    }

    fun isBathChannelTriggered2(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(1).toBoolean()
        }
    }

    fun isBathChannelTriggered3(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(2).toBoolean()
        }
    }

    fun isBathChannelTriggered4(): Boolean {
        with(getRegisterById(OwenPrModel.DI_01_16_TRIG)) {
            return value.toInt().getRange(3).toBoolean()
        }
    }

}
