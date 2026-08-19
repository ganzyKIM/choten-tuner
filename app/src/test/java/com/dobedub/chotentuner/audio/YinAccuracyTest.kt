package com.dobedub.chotentuner.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Measures how far the detector's estimate sits from the true pitch, in cents.
 * A tuner that reads a few cents off is useless, so the bias is asserted here
 * rather than left to the loose Hz tolerances in [YinPitchDetectorTest].
 */
class YinAccuracyTest {

    private val sampleRate = 44100
    private val bufferSize = 4096
    private val detector = YinPitchDetector(sampleRate, bufferSize)

    private fun sine(freq: Double, amp: Double = 0.6, phase: Double = 0.0): FloatArray =
        FloatArray(bufferSize) { i ->
            (amp * sin(2.0 * PI * freq * i / sampleRate + phase)).toFloat()
        }

    /** Signed cents from [expected] to [detected]; positive means detected is sharp. */
    private fun cents(detected: Double, expected: Double) =
        1200.0 * ln(detected / expected) / ln(2.0)

    private fun midiToHz(midi: Int) = 440.0 * 2.0.pow((midi - 69) / 12.0)

    @Test
    fun `pure sines are detected within one cent across the tuner's range`() {
        val names = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        var worst = 0.0
        var worstAt = ""
        println("midi  note   expected      detected      cents")
        for (midi in 40..88) {
            val expected = midiToHz(midi)
            val detected = detector.detect(sine(expected)).toDouble()
            val err = cents(detected, expected)
            if (abs(err) > abs(worst)) {
                worst = err
                worstAt = "${names[midi % 12]}${midi / 12 - 1}"
            }
            println(
                "%4d  %-5s %10.3f Hz %10.3f Hz %+8.2f".format(
                    midi, names[midi % 12] + (midi / 12 - 1), expected, detected, err,
                )
            )
        }
        println("worst bias: %+.2f cents at %s".format(worst, worstAt))
        assertTrue("worst bias %+.2f cents at %s".format(worst, worstAt), abs(worst) < 1.0)
    }

    @Test
    fun `phase offset does not shift the estimate`() {
        val f = 261.6256 // C4
        var worst = 0.0
        for (step in 0 until 8) {
            val phase = 2.0 * PI * step / 8.0
            val err = cents(detector.detect(sine(f, phase = phase)).toDouble(), f)
            if (abs(err) > abs(worst)) worst = err
        }
        println("worst phase-induced bias: %+.2f cents".format(worst))
        assertTrue("phase bias %+.2f cents".format(worst), abs(worst) < 1.0)
    }
}
