package com.dobedub.chotentuner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dobedub.chotentuner.audio.TonePlayer
import com.dobedub.chotentuner.audio.TunerEngine
import com.dobedub.chotentuner.music.Instrument
import com.dobedub.chotentuner.music.NoteMath
import com.dobedub.chotentuner.music.NoteReading
import kotlin.math.pow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppMode { TUNER, TONE }

/** A one-shot shower of stars around the mascot. */
data class SparkleBurst(val id: Int, val count: Int)

/** Lowest / highest note offered by the tone generator (C2..B6). */
private const val TONE_MIN_MIDI = 36
private const val TONE_MAX_MIDI = 95

class TunerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("choten_tuner", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(AppMode.TUNER)
    val mode: StateFlow<AppMode> = _mode.asStateFlow()

    private val _a4 = MutableStateFlow(prefs.getInt("a4", 440))
    val a4: StateFlow<Int> = _a4.asStateFlow()

    /** Microphone calibration in cents; subtracted from every measurement. */
    private val _calibration = MutableStateFlow(prefs.getFloat("calibration", 0f))
    val calibration: StateFlow<Float> = _calibration.asStateFlow()

    private val _instrument = MutableStateFlow(Instrument.fromName(prefs.getString("instrument", null)))
    val instrument: StateFlow<Instrument> = _instrument.asStateFlow()

    private val _dark = MutableStateFlow(prefs.getBoolean("dark", false))
    val dark: StateFlow<Boolean> = _dark.asStateFlow()

    private val _reading = MutableStateFlow<NoteReading?>(null)
    val reading: StateFlow<NoteReading?> = _reading.asStateFlow()

    private val _toneMidi = MutableStateFlow(69)
    val toneMidi: StateFlow<Int> = _toneMidi.asStateFlow()

    private val _tonePlaying = MutableStateFlow(false)
    val tonePlaying: StateFlow<Boolean> = _tonePlaying.asStateFlow()

    private val _settingsOpen = MutableStateFlow(false)
    val settingsOpen: StateFlow<Boolean> = _settingsOpen.asStateFlow()

    /** One-shot mascot reaction (poke / transformation); overrides the state pose+line briefly. */
    private val _transientLine = MutableStateFlow<String?>(null)
    val transientLine: StateFlow<String?> = _transientLine.asStateFlow()

    private val _transientSprite = MutableStateFlow<Sprite?>(null)
    val transientSprite: StateFlow<Sprite?> = _transientSprite.asStateFlow()

    private val _sparkle = MutableStateFlow(SparkleBurst(0, 0))
    val sparkle: StateFlow<SparkleBurst> = _sparkle.asStateFlow()
    private var sparkleId = 0

    private var blurtJob: Job? = null

    private val engine = TunerEngine(app)
    private val player = TonePlayer()

    // Median-of-3 smoothing plus a short hold so the display doesn't flicker.
    private val recent = ArrayDeque<Double>()
    private var lastGoodAt = 0L

    fun setMode(m: AppMode) {
        if (_mode.value == m) return
        _mode.value = m
        when (m) {
            AppMode.TUNER -> stopTone()
            AppMode.TONE -> stopTuner()
        }
    }

    fun startTuner() {
        if (_mode.value != AppMode.TUNER) return
        engine.start(::onPitch)
    }

    fun stopTuner() {
        engine.stop()
        _reading.value = null
        synchronized(recent) { recent.clear() }
    }

    private fun onPitch(freq: Float) {
        val now = System.currentTimeMillis()
        if (freq > 0 && _instrument.value.accepts(freq.toDouble())) {
            val median = synchronized(recent) {
                recent.addLast(freq.toDouble())
                if (recent.size > 3) recent.removeFirst()
                recent.sorted()[recent.size / 2]
            }
            // Undo whatever constant offset the mic path adds before naming the note.
            val corrected = median * 2.0.pow(-_calibration.value / 1200.0)
            val r = NoteMath.analyze(corrected, _a4.value.toDouble())
            if (r != null) {
                lastGoodAt = now
                _reading.value = r
                return
            }
        }
        if (now - lastGoodAt > 700) {
            _reading.value = null
            synchronized(recent) { recent.clear() }
        }
    }

    /** Select an absolute MIDI note; starts (or retargets) the tone. */
    fun selectTone(midi: Int) {
        val clamped = midi.coerceIn(TONE_MIN_MIDI, TONE_MAX_MIDI)
        _toneMidi.value = clamped
        val f = NoteMath.midiToFrequency(clamped, _a4.value.toDouble())
        if (_tonePlaying.value) {
            player.setFrequency(f)
        } else {
            player.start(f)
            _tonePlaying.value = true
        }
    }

    fun shiftOctave(delta: Int) {
        val next = _toneMidi.value + delta * 12
        if (next in TONE_MIN_MIDI..TONE_MAX_MIDI) {
            _toneMidi.value = next
            if (_tonePlaying.value) {
                player.setFrequency(NoteMath.midiToFrequency(next, _a4.value.toDouble()))
            }
        }
    }

    fun toggleTone() {
        if (_tonePlaying.value) {
            stopTone()
        } else {
            player.start(NoteMath.midiToFrequency(_toneMidi.value, _a4.value.toDouble()))
            _tonePlaying.value = true
        }
    }

    fun stopTone() {
        player.stop()
        _tonePlaying.value = false
    }

    fun setA4(value: Int) {
        val clamped = value.coerceIn(435, 445)
        _a4.value = clamped
        prefs.edit().putInt("a4", clamped).apply()
        if (_tonePlaying.value) {
            player.setFrequency(NoteMath.midiToFrequency(_toneMidi.value, clamped.toDouble()))
        }
    }

    fun setCalibration(cents: Float) {
        val clamped = cents.coerceIn(-30f, 30f)
        _calibration.value = clamped
        prefs.edit().putFloat("calibration", clamped).apply()
    }

    /** Nulls out the current error: whatever is being played becomes dead-on. */
    fun calibrateFromCurrentReading() {
        val r = _reading.value ?: return
        setCalibration(_calibration.value + r.cents.toFloat())
    }

    fun setInstrument(value: Instrument) {
        _instrument.value = value
        prefs.edit().putString("instrument", value.name).apply()
        synchronized(recent) { recent.clear() }
    }

    /** 변신! Toggles between 초텐짱 (light) and 아메 (dark). */
    fun toggleDark() {
        val next = !_dark.value
        _dark.value = next
        prefs.edit().putBoolean("dark", next).apply()
        blurt(Dialogue.transformLines(next).random(), Sprite.JOY)
        burstSparkles(20)
    }

    fun pokeCharacter() {
        blurt(Dialogue.pokeLines(_dark.value).random(), Sprite.SHY)
        burstSparkles(11)
    }

    /** Called when the tuner settles on a perfect reading. */
    fun celebrate() = burstSparkles(13)

    private fun burstSparkles(count: Int) {
        sparkleId++
        _sparkle.value = SparkleBurst(sparkleId, count)
    }

    private fun blurt(line: String, sprite: Sprite) {
        _transientLine.value = line
        _transientSprite.value = sprite
        blurtJob?.cancel()
        blurtJob = viewModelScope.launch {
            delay(4000)
            _transientLine.value = null
            _transientSprite.value = null
        }
    }

    fun openSettings(open: Boolean) {
        _settingsOpen.value = open
    }

    fun stopAll() {
        stopTuner()
        stopTone()
    }

    override fun onCleared() = stopAll()
}
