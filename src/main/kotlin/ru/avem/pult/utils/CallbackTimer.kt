package ru.avem.pult.utils

import javafx.util.Duration
import tornadofx.*
import java.util.*

class CallbackTimer(
    delay: Duration = 0.seconds,
    tickPeriod: Duration,
    tickTimes: Int = 1,
    onStartJob: (tcb: ICallbackTimer) -> Unit = {},
    tickJob: (tcb: ICallbackTimer) -> Unit = {},
    onFinishJob: (tcb: ICallbackTimer) -> Unit = {},
    private var timerName: String = "default_timer"
) : ICallbackTimer {

    val timer = Timer(timerName)
    var currentTick = -1
    var isRunning = true

    private val timerTask = object : TimerTask() {
        override fun run() {
            currentTick++
            tickJob(this@CallbackTimer)

            if (currentTick == tickTimes) {
                isRunning = false
                timer.cancel()
                onFinishJob(this@CallbackTimer)
            }
        }
    }

    init {
        Thread {
            onStartJob(this)
            timer.schedule(timerTask, delay.toMillis().toLong(), tickPeriod.toMillis().toLong())
        }.start()
    }

    override fun getCurrentTicks() = currentTick

    override fun getName() = timerName

    override fun stop() {
        timer.cancel()
        isRunning = false
    }
}

interface ICallbackTimer {
    fun getCurrentTicks(): Int

    fun getName(): String
    fun stop()
}
