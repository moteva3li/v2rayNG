package com.v2ray.ang.v2hub.utility

import android.net.TrafficStats
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round

class NetworkSpeedMonitor {
    private var lastTxBytes: Long = 0
    private var lastRxBytes: Long = 0
    private var lastUpdateTime: Long = 0
    private var monitoringJob: Job? = null

    data class NetworkSpeed(
        val downloadSpeed: Double, // in Mbps
        val uploadSpeed: Double    // in Mbps
    ) {
        // Format speeds to 2 decimal places
        override fun toString(): String {
            return "Download: ${formatSpeed(downloadSpeed)} Mbps, Upload: ${formatSpeed(uploadSpeed)} Mbps"
        }
    }

    fun startMonitoring(
        updateInterval: Long = 1000L,
        onSpeedUpdate: (NetworkSpeed) -> Unit
    ) {
        monitoringJob = CoroutineScope(Dispatchers.IO).launch {
            lastTxBytes = TrafficStats.getTotalTxBytes()
            lastRxBytes = TrafficStats.getTotalRxBytes()
            lastUpdateTime = System.currentTimeMillis()

            while (isActive) {
                delay(updateInterval)
                val currentTime = System.currentTimeMillis()
                val timeDiff = currentTime - lastUpdateTime

                val currentTxBytes = TrafficStats.getTotalTxBytes()
                val currentRxBytes = TrafficStats.getTotalRxBytes()

                val txBytesDiff = currentTxBytes - lastTxBytes
                val rxBytesDiff = currentRxBytes - lastRxBytes

                val uploadSpeed = calculateSpeedMbps(txBytesDiff, timeDiff)
                val downloadSpeed = calculateSpeedMbps(rxBytesDiff, timeDiff)

                lastTxBytes = currentTxBytes
                lastRxBytes = currentRxBytes
                lastUpdateTime = currentTime

                withContext(Dispatchers.Main) {
                    onSpeedUpdate(NetworkSpeed(downloadSpeed, uploadSpeed))
                }
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun calculateSpeedMbps(bytesDiff: Long, timeDiffMs: Long): Double {
        val bitsPerSec = (bytesDiff * 8.0) / (timeDiffMs / 1000.0)
        val mbps = bitsPerSec / 1_000_000.0
        return formatSpeedValue(mbps)
    }

    private fun formatSpeedValue(speed: Double): Double {
        return (round(speed * 100) / 100)
    }

    companion object {
        fun formatSpeed(speed: Double): String {
            return when {
                speed < 0.01 -> "0.00"
                speed > 999.99 -> "999.99+"
                else -> String.format("%.2f", speed)
            }
        }
    }
}