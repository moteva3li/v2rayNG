package com.v2ray.ang.v2hub.utility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class VpnTimer{
    private var timerJob: Job? = null
    private var elapsedTimeSeconds: Long = 0

    data class TimeFormat(
        val hours: String,
        val minutes: String,
        val seconds: String
    ) {
        override fun toString(): String = "$hours:$minutes:$seconds"
    }

    fun startTimer(onTick: (TimeFormat) -> Unit) {
        if (timerJob != null) return // Prevent multiple timers

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            val startTime = System.currentTimeMillis() - (elapsedTimeSeconds * 1000)

            while (isActive) {
                delay(1000) // Update every second
                elapsedTimeSeconds = (System.currentTimeMillis() - startTime) / 1000

                withContext(Dispatchers.Main) {
                    onTick(formatTime(elapsedTimeSeconds))
                }
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        elapsedTimeSeconds = 0
    }

    private fun formatTime(totalSeconds: Long): TimeFormat {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60

        return TimeFormat(
            hours = String.format("%02d", hours),
            minutes = String.format("%02d", minutes),
            seconds = String.format("%02d", seconds)
        )
    }

    // Get current time without starting the timer
    fun getCurrentTime(): TimeFormat = formatTime(elapsedTimeSeconds)
}