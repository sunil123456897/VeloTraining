package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural audio & tactile feedback engine for the spin-bike cockpit.
 * Uses AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK so background YouTube or music ducks briefly
 * during 3-2-1-START cues without ever pausing playback.
 */
class AudioHapticEngine(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    /**
     * Countdown 3 - 2 - 1 short crisp tone + light tactile pulse.
     */
    fun triggerCountdownBeep(secondsLeft: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) {
            vibrateOneShot(durationMs = 55L, amplitude = 160)
        }
        if (soundEnabled) {
            val freq = when (secondsLeft) {
                3 -> 784.0  // G5
                2 -> 880.0  // A5
                else -> 987.77 // B5
            }
            playDuckingTone(frequencyHz = freq, durationMs = 95, amplitude = 0.65f)
        }
    }

    /**
     * Phase transition ("СТАРТ" / new dial angle) double tone + strong distinct vibration pattern.
     */
    fun triggerPhaseTransition(isAttackPhase: Boolean, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (vibrationEnabled) {
            if (isAttackPhase) {
                vibrateWaveform(longArrayOf(0L, 110L, 65L, 160L), intArrayOf(0, 255, 0, 255))
            } else {
                vibrateWaveform(longArrayOf(0L, 130L, 80L, 90L), intArrayOf(0, 190, 0, 140))
            }
        }
        if (soundEnabled) {
            val freq = if (isAttackPhase) 1318.5 else 1046.5 // E6 for Attack, C6 for Recovery
            playDuckingTone(frequencyHz = freq, durationMs = 230, amplitude = 0.80f)
        }
    }

    /**
     * Subtle soft click for RPM cadence metronome (does not request audio ducking so music stays untouched).
     */
    fun triggerMetronomeTick(isRightFoot: Boolean) {
        engineScope.launch {
            runCatching {
                val freq = if (isRightFoot) 1100.0 else 920.0
                playRawPcmTone(frequencyHz = freq, durationMs = 18, amplitude = 0.18f)
            }
        }
    }

    private fun playDuckingTone(frequencyHz: Double, durationMs: Int, amplitude: Float) {
        engineScope.launch {
            var focusRequest: AudioFocusRequest? = null
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .setAudioAttributes(audioAttributes)
                        .setAcceptsDelayedFocusGain(false)
                        .setWillPauseWhenDucked(false)
                        .build()
                    audioManager?.requestAudioFocus(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    audioManager?.requestAudioFocus(
                        null,
                        AudioManager.STREAM_NOTIFICATION,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    )
                }

                playRawPcmTone(frequencyHz = frequencyHz, durationMs = durationMs, amplitude = amplitude)
                delay(durationMs.toLong() + 40L)
            } catch (_: Throwable) {
                // Ignore audio hardware failures in headless/emulator environments
            } finally {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                        audioManager?.abandonAudioFocusRequest(focusRequest)
                    } else {
                        @Suppress("DEPRECATION")
                        audioManager?.abandonAudioFocus(null)
                    }
                } catch (_: Throwable) {
                }
            }
        }
    }

    private fun playRawPcmTone(frequencyHz: Double, durationMs: Int, amplitude: Float) {
        val sampleRate = 24000
        val numSamples = ((durationMs / 1000.0) * sampleRate).toInt().coerceAtLeast(64)
        val pcmBuffer = ShortArray(numSamples)
        val fadeSamples = (numSamples * 0.15).toInt().coerceAtLeast(8)

        for (i in 0 until numSamples) {
            val env = when {
                i < fadeSamples -> i.toFloat() / fadeSamples
                i > numSamples - fadeSamples -> (numSamples - i).toFloat() / fadeSamples
                else -> 1f
            }
            val sample = sin(2.0 * PI * i * frequencyHz / sampleRate)
            pcmBuffer[i] = (sample * Short.MAX_VALUE * amplitude * env).toInt().toShort()
        }

        val byteCount = numSamples * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(audioAttributes)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(byteCount.coerceAtLeast(1024))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcmBuffer, 0, numSamples)
        track.play()
        Thread.sleep(durationMs.toLong() + 25L)
        track.stop()
        track.release()
    }

    private fun vibrateOneShot(durationMs: Long, amplitude: Int) {
        try {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(durationMs)
            }
        } catch (_: Throwable) {
        }
    }

    private fun vibrateWaveform(timings: LongArray, amplitudes: IntArray) {
        try {
            val vib = vibrator ?: return
            if (!vib.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(timings, -1)
            }
        } catch (_: Throwable) {
        }
    }
}
