package ru.avem.pult.communication.model

abstract class DeviceController : IDeviceController {
    override var isResponding = false
        set(value) {
            field = value
            if (!value) {
                println("$name is not responding!")
            }
        }
}
