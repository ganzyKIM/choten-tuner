package com.dobedub.chotentuner.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class YinPitchDetectorTest {

    private val sampleRate = 44100
    private val bufferSize = 4096
    private val detector = YinPitchDetector(sampleRate, bufferSize)

    private fun sine(
        freq: Double,
        amp: Double = 0.6,
        n: Int = bufferSize,
        sr: Int = sampleRate,
    ): FloatArray = FloatArray(n) { i -> (amp * sin(2.0 * PI * freq * i / sr)).toFloat() }

    private fun assertDetects(expected: Double, buffer: FloatArray, toleranceHz: Double) {
        val got = detector.detect(buffer)
        assertTrue(
            "expected ~${expected}Hz, got $got",
            got > 0 && abs(got - expected) <= toleranceHz,
        )
    }

    @Test
    fun `detects concert A 440`() {
        assertDetects(440.0, sine(440.0), 1.0)
    }

    @Test
    fun `detects low E2 of a guitar`() {
        assertDetects(82.41, sine(82.41), 1.0)
    }

    @Test
    fun `detects high E6`() {
        assertDetects(1318.51, sine(1318.51), 7.0)
    }

    @Test
    fun `detects off-grid frequency via interpolation`() {
        // 445.3 Hz does not land on an integer lag; parabolic interpolation must kick in
        assertDetects(445.3, sine(445.3), 1.0)
    }

    @Test
    fun `detects fundamental when harmonics are strong`() {
        // A plucked-string-like spectrum on G3: fundamental plus 3 strong harmonics
        val f = 196.0
        val buf = FloatArray(bufferSize) { i ->
            val t = 2.0 * PI * i / sampleRate
            (0.5 * sin(f * t) + 0.35 * sin(2 * f * t) + 0.2 * sin(3 * f * t) + 0.1 * sin(4 * f * t))
                .toFloat()
        }
        assertDetects(196.0, buf, 1.0)
    }

    @Test
    fun `tolerates a DC offset`() {
        val buf = sine(330.0).also { for (i in it.indices) it[i] = it[i] + 0.1f }
        assertDetects(330.0, buf, 1.5)
    }

    @Test
    fun `returns -1 for silence`() {
        assertEquals(-1f, detector.detect(FloatArray(bufferSize)), 0f)
    }

    @Test
    fun `returns -1 for white noise`() {
        val rng = Random(42)
        val buf = FloatArray(bufferSize) { (rng.nextDouble(-0.3, 0.3)).toFloat() }
        assertEquals(-1f, detector.detect(buf), 0f)
    }
}
