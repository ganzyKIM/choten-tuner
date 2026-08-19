package com.dobedub.chotentuner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dobedub.chotentuner.ui.CharacterZone
import com.dobedub.chotentuner.ui.Checkerboard
import com.dobedub.chotentuner.ui.PixelText
import com.dobedub.chotentuner.ui.RetroButton
import com.dobedub.chotentuner.ui.RetroWindow
import com.dobedub.chotentuner.ui.SettingsOverlay
import com.dobedub.chotentuner.ui.StatusLine
import com.dobedub.chotentuner.ui.ToneScreen
import com.dobedub.chotentuner.ui.TunerScreen
import com.dobedub.chotentuner.ui.theme.AmeDark
import com.dobedub.chotentuner.ui.theme.ChotenLight
import com.dobedub.chotentuner.ui.theme.LocalRetro
import kotlinx.coroutines.delay

/**
 * Shared height of the 튜너 / 소리내기 panels — sized between what each needs
 * so switching tabs never moves the layout below.
 */
private val MODE_CONTENT_HEIGHT = 352.dp

class MainActivity : ComponentActivity() {

    private val vm: TunerViewModel by viewModels()

    private val micGranted = mutableStateOf(false)

    private val micPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            micGranted.value = granted
            if (granted) vm.startTuner()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            val dark by vm.dark.collectAsState()
            CompositionLocalProvider(LocalRetro provides if (dark) AmeDark else ChotenLight) {
                SystemBarsSync(dark)
                ChotenTunerApp(
                    vm = vm,
                    micGranted = micGranted.value,
                    onRequestMic = { micPermission.launch(Manifest.permission.RECORD_AUDIO) },
                    onExit = { finish() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        micGranted.value = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (micGranted.value) vm.startTuner()
    }

    override fun onPause() {
        super.onPause()
        vm.stopAll()
    }
}

@Composable
private fun SystemBarsSync(dark: Boolean) {
    val view = LocalView.current
    val palette = LocalRetro.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor = (if (dark) palette.bg else palette.pink).toArgb()
        window.navigationBarColor = palette.shadow.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
    }
}

@Composable
fun ChotenTunerApp(
    vm: TunerViewModel,
    micGranted: Boolean,
    onRequestMic: () -> Unit,
    onExit: () -> Unit,
) {
    val palette = LocalRetro.current
    val mode by vm.mode.collectAsState()
    val reading by vm.reading.collectAsState()
    val a4 by vm.a4.collectAsState()
    val dark by vm.dark.collectAsState()
    val toneMidi by vm.toneMidi.collectAsState()
    val tonePlaying by vm.tonePlaying.collectAsState()
    val settingsOpen by vm.settingsOpen.collectAsState()
    val transientLine by vm.transientLine.collectAsState()
    val transientSprite by vm.transientSprite.collectAsState()

    val r = reading
    val bucket = Dialogue.bucketFor(micGranted, mode, r, tonePlaying, toneMidi)

    // The gauge reacts instantly, but the mascot waits for the situation to settle
    // so she doesn't flicker between poses while a note drifts across a threshold.
    var settledBucket by remember { mutableStateOf(bucket) }
    LaunchedEffect(bucket) {
        delay(400)
        settledBucket = bucket
    }

    val baseLine = remember(settledBucket, dark) { Dialogue.linesFor(settledBucket, dark).random() }
    val line = transientLine ?: baseLine
    val sprite = transientSprite ?: Dialogue.spriteFor(settledBucket)

    Box(Modifier.fillMaxSize()) {
        Checkerboard(Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RetroWindow(
                title = "CHOTEN TUNER v1.0",
                onClose = onExit,
                modifier = Modifier.fillMaxWidth().animateContentSize(),
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TabButton(
                            text = "♪ 튜너",
                            selected = mode == AppMode.TUNER,
                            modifier = Modifier.weight(1f),
                        ) {
                            vm.setMode(AppMode.TUNER)
                            if (micGranted) vm.startTuner()
                        }
                        TabButton(
                            text = "♫ 소리내기",
                            selected = mode == AppMode.TONE,
                            modifier = Modifier.weight(1f),
                        ) {
                            vm.setMode(AppMode.TONE)
                        }
                        RetroButton(
                            onClick = { vm.toggleDark() },
                            background = palette.purple,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(52.dp).height(40.dp),
                        ) {
                            PixelText("변신", fontSize = 10.sp, bold = true)
                        }
                        RetroButton(
                            onClick = { vm.openSettings(true) },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(40.dp).height(40.dp),
                        ) {
                            PixelText("⚙", fontSize = 14.sp)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Both modes occupy the same height so the window never resizes
                    // when you switch tabs — the mascot below keeps her size.
                    val modeArea = Modifier.fillMaxWidth().height(MODE_CONTENT_HEIGHT)
                    when (mode) {
                        AppMode.TUNER -> TunerScreen(
                            reading = r,
                            micGranted = micGranted,
                            onRequestMic = onRequestMic,
                            a4 = a4,
                            modifier = modeArea,
                        )
                        AppMode.TONE -> ToneScreen(
                            toneMidi = toneMidi,
                            playing = tonePlaying,
                            a4 = a4,
                            onSelect = vm::selectTone,
                            onToggle = vm::toggleTone,
                            onOctave = vm::shiftOctave,
                            modifier = modeArea,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    StatusLine(line)
                }
            }

            Spacer(Modifier.height(16.dp))

            CharacterZone(
                dark = dark,
                sprite = sprite,
                onPoke = vm::pokeCharacter,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }

        if (settingsOpen) {
            SettingsOverlay(
                a4 = a4,
                onA4 = vm::setA4,
                onClose = { vm.openSettings(false) },
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val palette = LocalRetro.current
    if (selected) {
        // Pressed-in look: shifted, no shadow, pink
        Box(
            modifier = modifier
                .height(40.dp)
                .offset(x = 2.dp, y = 2.dp)
                .background(palette.pink)
                .border(2.dp, palette.border)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            PixelText(text, fontSize = 11.sp, bold = true)
        }
    } else {
        RetroButton(
            onClick = onClick,
            contentPadding = PaddingValues(0.dp),
            modifier = modifier.height(40.dp),
        ) {
            PixelText(text, fontSize = 11.sp)
        }
    }
}
