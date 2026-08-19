package com.dobedub.chotentuner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.music.Instrument
import com.dobedub.chotentuner.ui.theme.LocalRetro
import java.util.Locale

@Composable
fun SettingsOverlay(
    a4: Int,
    calibration: Float,
    instrument: Instrument,
    canCalibrateNow: Boolean,
    onA4: (Int) -> Unit,
    onCalibration: (Float) -> Unit,
    onCalibrateNow: () -> Unit,
    onInstrument: (Instrument) -> Unit,
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
