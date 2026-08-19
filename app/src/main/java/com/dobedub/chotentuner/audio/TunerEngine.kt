package com.dobedub.chotentuner.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlin.math.sqrt

/**
 * Microphone capture loop: reads hops of audio, keeps a sliding analysis
 * window, gates on RMS and reports YIN pitch estimates from a background
 * thread. Caller must hold RECORD_AUDIO permission before [start].
 */
class TunerEngine(
    private val sampleRate: Int = 44100,
    private val windowSize: Int = 4096,
    private val hopSize: Int = 2048,
) {

    /** RMS below this is treated as silence (~-46 dBFS). */
    private val silenceRms = 0.005f

    private var thread: Thread? = null
    @Volatile private var running = false

    val isRunning: Boolean get() = running

    /**
     * Starts capturing. [onResult] is called from the audio thread with the
     * detected frequency in Hz, or -1f for silence / no clear pitch.
     */
    @SuppressLint("MissingPermission")
    fun start(onResult: (frequencyHz: Float) -> Unit) {
        if (running) return
        running = true
        thread = Thread {
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT,
            )
            val record = try {
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_FLOAT,
                    maxOf(minBuf, windowSize * Float.SIZE_BYTES * 2),
                )
            } catch (e: Exception) {
                running = false
                return@Thread
            }
            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
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
                        onResult(-1f)
                    } else {
                        onResult(detector.detect(window))
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
}
