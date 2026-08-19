package com.dobedub.chotentuner.music

/**
 * Tuning preset. The frequency window keeps the detector from locking onto
 * room noise or an octave error outside what the instrument can actually play,
 * and [strings] drives the reference hint under the meter.
 *
 * @param strings MIDI notes of the open strings / reference pitches
 */
enum class Instrument(
    val label: String,
    val minFreq: Double,
    val maxFreq: Double,
    val strings: List<Int>,
) {
    GENERAL("범용", NoteMath.MIN_FREQ, NoteMath.MAX_FREQ, emptyList()),

    /** E2 A2 D3 G3 B3 E4 */
    GUITAR("기타", 70.0, 1350.0, listOf(40, 45, 50, 55, 59, 64)),

    /** E1 A1 D2 G2 */
    BASS("베이스", 26.0, 420.0, listOf(28, 33, 38, 43)),

    /** G3 D4 A4 E5 */
    VIOLIN("바이올린", 170.0, 3200.0, listOf(55, 62, 69, 76)),

    /** 중현 G3, 유현 D4 — 전통 해금의 두 줄 */
    HAEGEUM("해금", 130.0, 1300.0, listOf(55, 62)),

    /** 本調子(혼초시) B2 F#3 B3 */
    SHAMISEN("샤미센", 90.0, 1100.0, listOf(47, 54, 59)),

    PIANO("피아노", 26.0, 4200.0, emptyList()),
    ;

    fun accepts(freqHz: Double) = freqHz in minFreq..maxFreq

    /** e.g. "E2 A2 D3 G3 B3 E4" */
    fun stringHint(): String = strings.joinToString(" ") { midi ->
        NoteMath.NOTE_NAMES[((midi % 12) + 12) % 12] + (midi / 12 - 1)
    }

    companion object {
        fun fromName(name: String?): Instrument =
            entries.firstOrNull { it.name == name } ?: GENERAL
    }
}
