package com.dobedub.chotentuner

import com.dobedub.chotentuner.music.NoteMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DialogueTest {

    private fun tunerBucket(cents: Double): String {
        // Pick a frequency that lands `cents` away from A4.
        val freq = 440.0 * Math.pow(2.0, cents / 1200.0)
        val reading = NoteMath.analyze(freq)!!
        return Dialogue.bucketFor(
            micGranted = true,
            mode = AppMode.TUNER,
            reading = reading,
            tonePlaying = false,
            toneMidi = 69,
        )
    }

    @Test
    fun `tuner holds one bucket however far off the note is`() {
        val buckets = listOf(-49.0, -30.0, -12.0, -3.0, 0.0, 3.0, 12.0, 30.0, 49.0)
            .map { tunerBucket(it) }
            .toSet()
        assertEquals(setOf("tuner_on"), buckets)
    }

    @Test
    fun `tuner falls back to idle only when nothing is heard`() {
        val idle = Dialogue.bucketFor(true, AppMode.TUNER, null, false, 69)
        assertEquals("tuner_idle", idle)
    }

    @Test
    fun `missing permission wins over everything`() {
        val bucket = Dialogue.bucketFor(false, AppMode.TUNER, NoteMath.analyze(440.0), false, 69)
        assertEquals("no_perm", bucket)
    }

    @Test
    fun `tone mode splits by the note's colour`() {
        // C4, F4, G4 are perfect intervals above C; D4/E4/A4/B4 major; the rest minor.
        assertEquals("tone_plain", Dialogue.bucketFor(true, AppMode.TONE, null, true, 60))
        assertEquals("tone_bright", Dialogue.bucketFor(true, AppMode.TONE, null, true, 64))
        assertEquals("tone_dark", Dialogue.bucketFor(true, AppMode.TONE, null, true, 61))
        assertEquals("tone_idle", Dialogue.bucketFor(true, AppMode.TONE, null, false, 60))
    }

    @Test
    fun `every bucket has lines for both personas`() {
        val buckets = listOf(
            "no_perm", "tuner_idle", "tuner_on",
            "tone_idle", "tone_bright", "tone_dark", "tone_plain",
        )
        for (b in buckets) {
            for (dark in listOf(false, true)) {
                val lines = Dialogue.linesFor(b, dark)
                assertTrue("$b (dark=$dark) has no lines", lines.isNotEmpty())
                assertTrue("$b (dark=$dark) fell back to the placeholder", lines != listOf("..."))
            }
        }
    }

    @Test
    fun `poke reactions cover several distinct poses for both personas`() {
        for (dark in listOf(false, true)) {
            val poses = Dialogue.pokeReactions(dark).map { it.sprite }.toSet()
            assertTrue(
                "dark=$dark only offers $poses when poked",
                poses.size >= 4,
            )
        }
    }
}
