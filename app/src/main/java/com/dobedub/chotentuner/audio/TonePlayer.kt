package com.dobedub.chotentuner.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/**
 * Streams a continuous, click-free sine tone. Frequency changes glide
 * smoothly (short portamento) and start/stop are faded to avoid pops.
 */
class TonePlayer(private val sampleRate: Int = 44100) {

    @Volatile private var targetFreq = 440.0
    @Volatile private var running = false
    private var thread: Thread? = null

    val isPlaying: Boolean get() = running

    fun setFrequency(freqHz: Double) {
        targetFreq = freqHz
    }

    fun start(freqHz: Double) {
        targetFreq = freqHz
        if (running) return
        running = true
        thread = Thread {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT,
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, 8192))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            track.play()

            val chunk = FloatArray(1024)
            var phase = 0.0
            var freq = targetFreq
            var amp = 0f
            val fullAmp = 0.4f

            try {
                // Keep writing until stopped AND the fade-out has finished.
                while (running || amp > 0.0005f) {
                    val ampTarget = if (running) fullAmp else 0f
                    for (i in chunk.indices) {
                        freq += (targetFreq - freq) * 0.0005 // ~45 ms glide
                        amp += (ampTarget - amp) * 0.0006f // ~40 ms fade
                        phase += 2.0 * PI * freq / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                        chunk[i] = (amp * sin(phase)).toFloat()
                    }
                    track.write(chunk, 0, chunk.size, AudioTrack.WRITE_BLOCKING)
                }
            } finally {
                try {
                    track.stop()
                } catch (_: Exception) {
                }
                track.release()
            }
        }.apply {
            name = "TonePlayer"
            start()
        }
    }

    fun stop() {
        running = false
        thread?.join(800)
        thread = null
    }
}
