package com.dobedub.chotentuner.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.dobedub.chotentuner.audio.TunerEngine
import com.dobedub.chotentuner.music.Instrument
import com.dobedub.chotentuner.ui.theme.LocalRetro
import java.util.Locale
import kotlin.math.log10
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingsOverlay(
    a4: Int,
    calibration: Float,
    instrument: Instrument,
    sensitivity: Int,
    inputLevel: StateFlow<Float>,
    canCalibrateNow: Boolean,
    onA4: (Int) -> Unit,
    onCalibration: (Float) -> Unit,
    onCalibrateNow: () -> Unit,
    onInstrument: (Instrument) -> Unit,
    onSensitivity: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val palette = LocalRetro.current
    BackHandler(onBack = onClose)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x9916121F))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClose,
            ),
        contentAlignment = Alignment.Center,
    ) {
        RetroWindow(
            title = "설정.EXE",
            onClose = onClose,
            modifier = Modifier
                .width(320.dp)
                .padding(vertical = 40.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PixelText("악기 프리셋", fontSize = 13.sp, bold = true)
                Spacer(Modifier.height(4.dp))
                PixelText(
                    "악기 음역 밖의 잡음을 걸러줘!",
                    fontSize = 9.sp,
                    color = palette.textMuted,
                )
                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Instrument.entries.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            pair.forEach { inst ->
                                val selected = inst == instrument
                                RetroButton(
                                    onClick = { onInstrument(inst) },
                                    background = if (selected) palette.pink else palette.window,
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                ) {
                                    PixelText(inst.label, fontSize = 11.sp, bold = selected)
                                }
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                if (instrument.strings.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    PixelText(
                        instrument.stringHint(),
                        fontSize = 10.sp,
                        color = palette.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }

                Divider()

                PixelText("마이크 민감도", fontSize = 13.sp, bold = true)
                Spacer(Modifier.height(4.dp))
                PixelText(
                    "숫자가 클수록 작은 소리까지 잡아!\n주변이 시끄러우면 낮춰줘~",
                    fontSize = 9.sp,
                    color = palette.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(8.dp))
                Stepper(
                    value = "$sensitivity / ${TunerEngine.MAX_SENSITIVITY}",
                    canDecrease = sensitivity > TunerEngine.MIN_SENSITIVITY,
                    canIncrease = sensitivity < TunerEngine.MAX_SENSITIVITY,
                    onDecrease = { onSensitivity(sensitivity - 1) },
                    onIncrease = { onSensitivity(sensitivity + 1) },
                )
                Spacer(Modifier.height(10.dp))
                InputLevelBar(inputLevel, TunerEngine.rmsGateFor(sensitivity))

                Divider()

                PixelText("기준음 A4 보정", fontSize = 13.sp, bold = true)
                Spacer(Modifier.height(8.dp))
                Stepper(
                    value = "$a4 Hz",
                    canDecrease = a4 > 435,
                    canIncrease = a4 < 445,
                    onDecrease = { onA4(a4 - 1) },
                    onIncrease = { onA4(a4 + 1) },
                )
                Spacer(Modifier.height(4.dp))
                PixelText("범위 435~445 Hz", fontSize = 9.sp, color = palette.textMuted)

                Divider()

                PixelText("마이크 보정", fontSize = 13.sp, bold = true)
                Spacer(Modifier.height(4.dp))
                PixelText(
                    "측정이 계속 높거나 낮게 나올 때\n그 차이만큼 빼줄게!",
                    fontSize = 9.sp,
                    color = palette.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                )
                Spacer(Modifier.height(8.dp))
                Stepper(
                    value = String.format(Locale.US, "%+.1f ct", calibration),
                    canDecrease = calibration > -30f,
                    canIncrease = calibration < 30f,
                    onDecrease = { onCalibration(calibration - 0.5f) },
                    onIncrease = { onCalibration(calibration + 0.5f) },
                )
                Spacer(Modifier.height(8.dp))
                RetroButton(
                    onClick = onCalibrateNow,
                    enabled = canCalibrateNow,
                    background = palette.mint,
                ) {
                    PixelText("지금 이 음을 기준으로!", fontSize = 11.sp, bold = true)
                }
                Spacer(Modifier.height(4.dp))
                PixelText(
                    if (canCalibrateNow) "정확한 음을 들려주면서 눌러줘~"
                    else "소리가 들리는 중에만 쓸 수 있어!",
                    fontSize = 9.sp,
                    color = palette.textMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                RetroButton(
                    onClick = {
                        onA4(440)
                        onCalibration(0f)
                        onSensitivity(TunerEngine.DEFAULT_SENSITIVITY)
                    },
                    background = palette.blue,
                ) {
                    PixelText("전부 기본값으로!", fontSize = 11.sp, bold = true)
                }

                Spacer(Modifier.height(12.dp))
                PixelText("초텐짱의 비밀 설정창~☆", fontSize = 9.sp, color = palette.textMuted)
            }
        }
    }
}

/** dBFS position of an RMS value on a -60..0 dB scale, as a 0..1 fraction. */
private fun levelFraction(rms: Float): Float {
    val db = 20f * log10(rms.coerceAtLeast(1e-6f))
    return ((db + 60f) / 60f).coerceIn(0f, 1f)
}

/**
 * Live mic meter with the silence gate marked, so the 민감도 number can be
 * chosen against what the microphone is actually picking up.
 */
@Composable
private fun InputLevelBar(levelFlow: StateFlow<Float>, gate: Float) {
    val palette = LocalRetro.current
    val rms by levelFlow.collectAsState()
    val level = levelFraction(rms)
    val gateAt = levelFraction(gate)
    val passing = rms >= gate

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .background(palette.panel)
                .border(2.dp, palette.border)
        ) {
            drawRect(
                color = if (passing) palette.mintDeep else palette.textMuted,
                size = Size(size.width * level, size.height),
            )
            // The gate: everything left of this line is treated as silence.
            drawRect(
                color = palette.pinkDeep,
                topLeft = Offset(size.width * gateAt - 1.5.dp.toPx(), 0f),
                size = Size(3.dp.toPx(), size.height),
            )
        }
        Spacer(Modifier.height(4.dp))
        PixelText(
            if (passing) "입력 감지중! ♪" else "감지 기준(분홍선)을 넘겨야 반응해~",
            fontSize = 9.sp,
            color = if (passing) palette.mintDeep else palette.textMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun Divider() {
    val palette = LocalRetro.current
    Spacer(Modifier.height(14.dp))
    Box(
        Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(palette.border.copy(alpha = 0.35f))
    )
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun Stepper(
    value: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        RetroButton(
            onClick = onDecrease,
            enabled = canDecrease,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(42.dp),
        ) {
            PixelText("-", fontSize = 18.sp, bold = true)
        }
        RetroPanel(Modifier.width(130.dp)) {
            PixelText(value, fontSize = 15.sp, bold = true)
        }
        RetroButton(
            onClick = onIncrease,
            enabled = canIncrease,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(42.dp),
        ) {
            PixelText("+", fontSize = 18.sp, bold = true)
        }
    }
}
