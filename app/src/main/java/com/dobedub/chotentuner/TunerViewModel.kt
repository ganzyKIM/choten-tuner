package com.dobedub.chotentuner

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dobedub.chotentuner.audio.TonePlayer
import com.dobedub.chotentuner.audio.TunerEngine
import com.dobedub.chotentuner.music.NoteMath
import com.dobedub.chotentuner.music.NoteReading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppMode { TUNER, TONE }

/** Lowest / highest note offered by the tone generator (C2..B6). */
private const val TONE_MIN_MIDI = 36
private const val TONE_MAX_MIDI = 95

class TunerViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("choten_tuner", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(AppMode.TUNER)
    val mode: StateFlow<AppMode> = _mode.asStateFlow()

    private val _a4 = MutableStateFlow(prefs.getInt("a4", 440))
    val a4: StateFlow<Int> = _a4.asStateFlow()

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

    /** One-shot mascot line (poke / transformation); overrides the state line briefly. */
    private val _transientLine = MutableStateFlow<String?>(null)
    val transientLine: StateFlow<String?> = _transientLine.asStateFlow()
    private var blurtJob: Job? = null

    private val engine = TunerEngine()
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
        if (freq > 0) {
            val median = synchronized(recent) {
                recent.addLast(freq.toDouble())
                if (recent.size > 3) recent.removeFirst()
                recent.sorted()[recent.size / 2]
            }
            val r = NoteMath.analyze(median, _a4.value.toDouble())
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

    /** 변신! Toggles between 초텐짱 (light) and 아메 (dark). */
    fun toggleDark() {
        val next = !_dark.value
        _dark.value = next
        prefs.edit().putBoolean("dark", next).apply()
        blurt(Dialogue.transformLines(next).random())
    }

    fun pokeCharacter() = blurt(Dialogue.pokeLines(_dark.value).random())

    private fun blurt(line: String) {
        _transientLine.value = line
        blurtJob?.cancel()
        blurtJob = viewModelScope.launch {
            delay(4000)
            _transientLine.value = null
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
