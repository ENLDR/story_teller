package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.sin

class SpookyAudioEngine {
    private var bgmTrack: AudioTrack? = null
    private var sfxTrack: AudioTrack? = null
    private var bgmJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var isBgmActive = false
    private var bgmType: String? = null

    fun startBgm(type: String) {
        stopBgm()
        isBgmActive = true
        bgmType = type
        bgmJob = scope.launch {
            val sampleRate = 22050
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            bgmTrack = track
            try {
                track.play()
            } catch (e: Exception) {
                return@launch
            }

            val buffer = ShortArray(bufferSize)
            var time = 0.0

            while (isActive && isBgmActive) {
                for (i in buffer.indices) {
                    time += 1.0 / sampleRate
                    // Low multi-tone spooky drone: combined low frequency sine waves
                    val freq1 = 55.0
                    val freq2 = 57.0
                    val modulation = 0.55 + 0.45 * sin(2.0 * Math.PI * 0.15 * time) // Swell effect
                    
                    val value = (sin(2.0 * Math.PI * freq1 * time) + sin(2.0 * Math.PI * freq2 * time)) / 2.0
                    val floatSample = value * 0.25 * modulation
                    
                    // Add subtle crackle/hiss for realistic atmosphere
                    val noise = (Math.random() * 2.0 - 1.0) * 0.012
                    
                    val finalSample = (floatSample + noise).coerceIn(-1.0, 1.0)
                    buffer[i] = (finalSample * Short.MAX_VALUE).toInt().toShort()
                }
                try {
                    track.write(buffer, 0, buffer.size)
                } catch (e: Exception) {
                    break
                }
            }
            try {
                track.stop()
                track.release()
            } catch (e: Exception) {}
        }
    }

    fun stopBgm() {
        isBgmActive = false
        bgmJob?.cancel()
        bgmJob = null
        bgmTrack?.let {
            try {
                it.stop()
                it.release()
            } catch (e: Exception) {}
        }
        bgmTrack = null
        bgmType = null
    }

    fun playSfx(type: String) {
        scope.launch {
            val sampleRate = 22050
            val durationSeconds = when {
                type.contains("Scream", ignoreCase = true) -> 1.5
                type.contains("Creak", ignoreCase = true) -> 1.3
                type.contains("Wind", ignoreCase = true) || type.contains("Rain", ignoreCase = true) -> 2.0
                type.contains("Heartbeat", ignoreCase = true) -> 1.6
                type.contains("Whisper", ignoreCase = true) -> 1.8
                type.contains("Shatter", ignoreCase = true) -> 0.8
                else -> 1.0
            }
            val numSamples = (sampleRate * durationSeconds).toInt()
            val buffer = ShortArray(numSamples)

            when {
                type.contains("Scream", ignoreCase = true) -> {
                    // Eerie rising and collapsing vibration scream
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val freq = 700.0 + 250.0 * sin(2.0 * Math.PI * 30.0 * t) - 300.0 * t
                        val fadeOut = (1.0 - t / durationSeconds).coerceIn(0.0, 1.0)
                        val noise = (Math.random() * 2.0 - 1.0) * 0.12
                        val sweep = sin(2.0 * Math.PI * freq * t) * 0.20
                        val sample = (sweep + noise) * fadeOut
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                type.contains("Creak", ignoreCase = true) -> {
                    // Unstable, sliding, friction frequency
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val baseFreq = 160.0
                        val jitter = sin(2.0 * Math.PI * 55.0 * t) * 25.0
                        val freq = baseFreq + jitter + (t * 40.0)
                        val amplitude = (t * (durationSeconds - t) * 1.5).coerceIn(0.0, 1.0)
                        val sample = sin(2.0 * Math.PI * freq * t) * 0.14 * amplitude
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                type.contains("Heartbeat", ignoreCase = true) -> {
                    // Double heavy pulse of low frequency
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val pulse1 = sin(2.0 * Math.PI * 50.0 * t) * Math.exp(-16.0 * t)
                        val t2 = t - 0.45
                        val pulse2 = if (t2 > 0) sin(2.0 * Math.PI * 50.0 * t2) * Math.exp(-16.0 * t2) else 0.0
                        val sample = (pulse1 + pulse2 * 0.85) * 0.55
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                type.contains("Whisper", ignoreCase = true) -> {
                    // Distorted whispering wind
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val noise = (Math.random() * 2.0 - 1.0) * 0.08
                        val hiss = sin(2.0 * Math.PI * (1200.0 + 300.0 * sin(2.0 * Math.PI * 4.0 * t)) * t) * 0.015
                        val fadeOut = (1.0 - t / durationSeconds).coerceIn(0.0, 1.0)
                        val sample = (noise + hiss) * fadeOut
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                type.contains("Shatter", ignoreCase = true) -> {
                    // Rapid decay metallic noise
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val noise = (Math.random() * 2.0 - 1.0) * 0.15
                        val ring = sin(2.0 * Math.PI * 1800.0 * t) * 0.05
                        val sample = (noise + ring) * Math.exp(-22.0 * t)
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
                else -> { // Default howling wind / rain rumble
                    for (i in 0 until numSamples) {
                        val t = i.toDouble() / sampleRate
                        val noise = (Math.random() * 2.0 - 1.0) * 0.05
                        val rum = sin(2.0 * Math.PI * 70.0 * t) * 0.03
                        val sample = (noise + rum) * (1.0 - t / durationSeconds)
                        buffer[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                }
            }

            withContext(Dispatchers.IO) {
                try {
                    val track = AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        numSamples * 2,
                        AudioTrack.MODE_STATIC
                    )
                    track.write(buffer, 0, buffer.size)
                    track.play()
                    delay((durationSeconds * 1000).toLong())
                    track.stop()
                    track.release()
                } catch (e: Exception) {}
            }
        }
    }

    fun shutdown() {
        stopBgm()
        scope.cancel()
    }
}
