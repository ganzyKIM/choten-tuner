package com.dobedub.chotentuner.music

import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Result of mapping a detected frequency onto the equal-tempered scale.
 *
 * @param midi MIDI note number of the nearest note (A4 = 69)
 * @param name note letter with accidental, e.g. "A", "C#"
 * @param solfege Korean solfege name, e.g. "라", "도#"
 * @param octave scientific pitch octave (A4 -> 4)
 * @param cents signed deviation from the nearest note in cents (-50..+50)
 * @param frequency the input frequency in Hz
 * @param targetFrequency exact frequency of the nearest note at the given A4 reference
 */
data class NoteReading(
    val midi: Int,
    val name: String,
    val solfege: String,
    val octave: Int,
    val cents: Double,
    val frequency: Double,
    val targetFrequency: Double,
)

object NoteMath {

    val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val SOLFEGE = arrayOf("도", "도#", "레", "레#", "미", "파", "파#", "솔", "솔#", "라", "라#", "시")

    /** Lowest / highest frequency the tuner cares about (below B0 / above piano top C8). */
    const val MIN_FREQ = 25.0
    const val MAX_FREQ = 4500.0

    /** Frequency in Hz of a MIDI note under the given A4 reference. */
    fun midiToFrequency(midi: Int, a4: Double = 440.0): Double {
        return a4 * 2.0.pow((midi - 69) / 12.0)
    }

    /** Map a raw frequency to the nearest note, or null when out of the supported range. */
    fun analyze(frequency: Double, a4: Double = 440.0): NoteReading? {
        if (frequency.isNaN() || frequency < MIN_FREQ || frequency > MAX_FREQ) return null
        val midi = (69 + 12 * log2(frequency / a4)).roundToInt()
        val target = midiToFrequency(midi, a4)
        val cents = 1200.0 * log2(frequency / target)
        val idx = ((midi % 12) + 12) % 12
        return NoteReading(
            midi = midi,
            name = NOTE_NAMES[idx],
            solfege = SOLFEGE[idx],
            octave = midi / 12 - 1,
            cents = cents,
            frequency = frequency,
            targetFrequency = target,
        )
    }
}
