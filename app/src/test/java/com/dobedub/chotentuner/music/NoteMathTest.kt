package com.dobedub.chotentuner.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NoteMathTest {

    @Test
    fun `midiToFrequency returns 440 for A4`() {
        assertEquals(440.0, NoteMath.midiToFrequency(69), 1e-6)
    }

    @Test
    fun `midiToFrequency returns middle C for midi 60`() {
        assertEquals(261.6256, NoteMath.midiToFrequency(60), 1e-3)
    }

    @Test
    fun `midiToFrequency follows custom A4 reference`() {
        assertEquals(442.0, NoteMath.midiToFrequency(69, 442.0), 1e-6)
        assertEquals(221.0, NoteMath.midiToFrequency(57, 442.0), 1e-6)
    }

    @Test
    fun `exact A440 analyzes as A4 with zero cents`() {
        val r = NoteMath.analyze(440.0)
        assertNotNull(r)
        r!!
        assertEquals(69, r.midi)
        assertEquals("A", r.name)
        assertEquals("라", r.solfege)
        assertEquals(4, r.octave)
        assertEquals(0.0, r.cents, 0.01)
        assertEquals(440.0, r.targetFrequency, 1e-6)
    }

    @Test
    fun `sharp frequency reports positive cents`() {
        // 1200 * log2(446/440) = 23.44 cents
        val r = NoteMath.analyze(446.0)!!
        assertEquals("A", r.name)
        assertEquals(4, r.octave)
        assertEquals(23.44, r.cents, 0.05)
    }

    @Test
    fun `flat frequency rounds to nearest lower note`() {
        // 254 Hz is 51.2 cents below C4, so nearest note is B3 (+48.9 cents)
        val r = NoteMath.analyze(254.0)!!
        assertEquals("B", r.name)
        assertEquals(3, r.octave)
        assertEquals(48.9, r.cents, 0.2)
    }

    @Test
    fun `custom A4 reference shifts the tuning target`() {
        // Playing exactly 440 while calibrated to A4=442 -> 7.85 cents flat
        val r = NoteMath.analyze(440.0, a4 = 442.0)!!
        assertEquals("A", r.name)
        assertEquals(4, r.octave)
        assertEquals(-7.85, r.cents, 0.05)
        assertEquals(442.0, r.targetFrequency, 1e-6)
    }

    @Test
    fun `octave boundary maps B3 below C4`() {
        val b3 = NoteMath.analyze(246.94)!!
        assertEquals("B", b3.name)
        assertEquals(3, b3.octave)
        val c4 = NoteMath.analyze(261.63)!!
        assertEquals("C", c4.name)
        assertEquals(4, c4.octave)
    }

    @Test
    fun `non-positive and out-of-range frequencies return null`() {
        assertNull(NoteMath.analyze(0.0))
        assertNull(NoteMath.analyze(-100.0))
        assertNull(NoteMath.analyze(10.0))
        assertNull(NoteMath.analyze(9000.0))
    }
}
