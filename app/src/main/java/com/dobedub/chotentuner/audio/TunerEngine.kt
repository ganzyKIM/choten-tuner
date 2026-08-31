package com.dobedub.chotentuner.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Microphone capture loop: reads hops of audio, keeps a sliding analysis
 * window, gates on RMS and reports YIN pitch estimates from a background
 * thread. Caller must hold RECORD_AUDIO permission before [start].
 */
class TunerEngine(
    private val context: Context,
    private val sampleRate: Int = 44100,
    private val windowSize: Int = 4096,
    private val hopSize: Int = 2048,
) {

    /**
     * RMS below this counts as silence. Driven by the 민감도 setting so a quiet
     * instrument in a loud room can be dialled in either direction.
     */
    @Volatile
    var silenceRms: Float = rmsGateFor(DEFAULT_SENSITIVITY)

    private var thread: Thread? = null
    @Volatile private var running = false

    val isRunning: Boolean get() = running

    /**
     * Preferred capture sources, most faithful first. The default MIC source runs
     * noise suppression and automatic gain control, which distort the waveform a
     * tuner has to measure; UNPROCESSED bypasses that where the device supports it.
     */
    private fun audioSources(): List<Int> {
        val unprocessedSupported = runCatching {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.getProperty(AudioManager.PROPERTY_SUPPORT_AUDIO_SOURCE_UNPROCESSED) == "true"
        }.getOrDefault(false)
        return buildList {
            if (unprocessedSupported) add(MediaRecorder.AudioSource.UNPROCESSED)
            add(MediaRecorder.AudioSource.VOICE_RECOGNITION)
            add(MediaRecorder.AudioSource.MIC)
        }
    }

    @SuppressLint("MissingPermission")
    private fun openRecord(): AudioRecord? {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
        )
        val bufBytes = maxOf(minBuf, windowSize * Float.SIZE_BYTES * 2)
        for (source in audioSources()) {
            val record = runCatching {
                AudioRecord(
                    source,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                    bufBytes,
                )
            }.getOrNull() ?: continue
            if (record.state == AudioRecord.STATE_INITIALIZED) return record
            record.release()
        }
        return null
    }

    /**
     * Starts capturing. [onResult] is called from the audio thread with the
     * detected frequency in Hz (-1f for silence / no clear pitch) and the
     * window's RMS level, which the settings screen shows as an input meter.
     */
    fun start(onResult: (frequencyHz: Float, rms: Float) -> Unit) {
        if (running) return
        running = true
        thread = Thread {
            val record = openRecord()
            if (record == null) {
                running = false
                return@Thread
            }

            val detector = YinPitchDetector(sampleRate, windowSize)
            val window = FloatArray(windowSize)
            val hop = FloatArray(hopSize)
            var filled = 0

            record.startRecording()
            try {
                while (running) {
                    var read = 0
                    var failed = false
                    while (read < hopSize && running) {
                        val n = record.read(hop, read, hopSize - read, AudioRecord.READ_BLOCKING)
                        if (n <= 0) {
                            failed = true
                            break
                        }
                        read += n
                    }
                    if (failed) {
                        Thread.sleep(30)
                        continue
                    }
                    if (read < hopSize) continue

                    System.arraycopy(window, hopSize, window, 0, windowSize - hopSize)
                    System.arraycopy(hop, 0, window, windowSize - hopSize, hopSize)
                    filled += hopSize
                    if (filled < windowSize) continue

                    var sum = 0f
                    for (s in window) sum += s * s
                    val rms = sqrt(sum / windowSize)
                    if (rms < silenceRms) {
                        onResult(-1f, rms)
                    } else {
                        onResult(detector.detect(window), rms)
                    }
                }
            } catch (_: InterruptedException) {
                // stop() interrupted a sleep; fall through to cleanup
            } finally {
                try {
                    record.stop()
                } catch (_: Exception) {
                }
                record.release()
            }
        }.apply {
            name = "TunerEngine"
            priority = Thread.MAX_PRIORITY
            start()
        }
    }

    fun stop() {
        running = false
        thread?.interrupt()
        thread?.join(500)
        thread = null
    }

    companion object {
        const val MIN_SENSITIVITY = 1
        const val MAX_SENSITIVITY = 10
        const val DEFAULT_SENSITIVITY = 6

        private const val QUIETEST_GATE = 0.001f  // ~-60 dBFS, hears almost anything
        private const val LOUDEST_GATE = 0.03f    // ~-30 dBFS, ignores room noise

        /** Maps a 민감도 level onto the RMS gate; higher level = hears quieter sounds. */
        fun rmsGateFor(level: Int): Float {
            val t = (level.coerceIn(MIN_SENSITIVITY, MAX_SENSITIVITY) - MIN_SENSITIVITY)
                .toFloat() / (MAX_SENSITIVITY - MIN_SENSITIVITY)
            return LOUDEST_GATE * (QUIETEST_GATE / LOUDEST_GATE).pow(t)
        }
    }
}
