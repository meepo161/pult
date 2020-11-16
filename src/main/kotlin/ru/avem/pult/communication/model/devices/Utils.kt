package ru.avem.pult.communication.model.devices

open class ControllerException : Exception()

class LatrStuckException : ControllerException()