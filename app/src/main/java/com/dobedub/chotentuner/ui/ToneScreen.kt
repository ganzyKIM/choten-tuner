package com.dobedub.chotentuner.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.music.NoteMath
import com.dobedub.chotentuner.ui.theme.LocalRetro
import java.util.Locale

@Composable
fun ToneScreen(
    toneMidi: Int,
    playing: Boolean,
    a4: Int,
    onSelect: (Int) -> Unit,
    onToggle: () -> Unit,
    onOctave: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    val idx = ((toneMidi % 12) + 12) % 12
    val octave = toneMidi / 12 - 1
    val freq = NoteMath.midiToFrequency(toneMidi, a4.toDouble())

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RetroPanel(Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.Bottom) {
                    PixelText(
                        NoteMath.NOTE_NAMES[idx],
                        fontSize = 34.sp,
                        bold = true,
                        color = if (playing) palette.pinkDeep else palette.text,
                    )
                    Spacer(Modifier.width(4.dp))
                    PixelText("$octave", fontSize = 18.sp, color = palette.textMuted)
                }
                PixelText(
                    "${NoteMath.SOLFEGE[idx]} · ${String.format(Locale.US, "%.2f", freq)} Hz",
                    fontSize = 11.sp,
                    color = palette.textMuted,
                )
                Spacer(Modifier.height(4.dp))
                if (playing) {
                    val blink = rememberInfiniteTransition(label = "notes")
                    val alpha by blink.animateFloat(
                        initialValue = 1f,
                        targetValue = 0.25f,
                        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "alpha",
                    )
                    PixelText(
                        "♪ ♫ 재생중~ ♫ ♪",
                        fontSize = 10.sp,
                        color = palette.pinkDeep,
                        bold = true,
                        modifier = Modifier.alpha(alpha),
                    )
                } else {
                    PixelText("멈춰있어! 음을 눌러봐~", fontSize = 10.sp, color = palette.textMuted)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RetroButton(
                onClick = { onOctave(-1) },
                enabled = toneMidi - 12 >= 36,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
            ) {
                PixelText("◀", fontSize = 13.sp, bold = true)
            }
            RetroPanel(Modifier.width(130.dp), background = palette.panel) {
                PixelText("옥타브 $octave", fontSize = 12.sp, bold = true)
            }
            RetroButton(
                onClick = { onOctave(1) },
                enabled = toneMidi + 12 <= 95,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
            ) {
                PixelText("▶", fontSize = 13.sp, bold = true)
            }
        }

        Spacer(Modifier.height(12.dp))

        val baseMidi = (toneMidi / 12) * 12
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (0 until 12).chunked(4).forEach { rowNotes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowNotes.forEach { i ->
                        val isBlack = NoteMath.NOTE_NAMES[i].length > 1
                        val selected = i == idx
                        val bg = when {
                            selected -> palette.pink
                            isBlack -> palette.keyDark
                            else -> palette.window
                        }
                        val fg = when {
                            selected -> palette.text
                            isBlack -> palette.keyDarkText
                            else -> palette.text
                        }
                        RetroButton(
                            onClick = { onSelect(baseMidi + i) },
                            background = bg,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PixelText(
                                    NoteMath.NOTE_NAMES[i],
                                    fontSize = 13.sp,
                                    bold = true,
                                    color = fg,
                                )
                                PixelText(
                                    NoteMath.SOLFEGE[i],
                                    fontSize = 8.sp,
                                    color = if (selected) palette.text else palette.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        RetroButton(
            onClick = onToggle,
            background = if (playing) palette.pinkDeep else palette.mint,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            PixelText(
                if (playing) "■ 그만!" else "▶ 소리내기!",
                fontSize = 15.sp,
                bold = true,
            )
        }
    }
}
