package com.dobedub.chotentuner.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.music.Instrument
import com.dobedub.chotentuner.music.NoteReading
import com.dobedub.chotentuner.ui.theme.LocalRetro
import com.dobedub.chotentuner.ui.theme.PixelFont
import java.util.Locale
import kotlin.math.abs

@Composable
fun TunerScreen(
    reading: NoteReading?,
    micGranted: Boolean,
    onRequestMic: () -> Unit,
    a4: Int,
    instrument: Instrument,
    calibration: Float,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!micGranted) {
            Spacer(Modifier.weight(1f))
            PixelText(
                "마이크 권한이 없으면\n초텐짱이 못 들어~ ㅠㅠ",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(16.dp))
            RetroButton(onClick = onRequestMic, background = palette.pink) {
                PixelText("마이크 권한 주기!", fontSize = 13.sp, bold = true)
            }
            Spacer(Modifier.height(10.dp))
            PixelText("안 뜨면 설정 앱 > 권한에서 켜줘!", fontSize = 9.sp, color = palette.textMuted)
            Spacer(Modifier.weight(1f))
        } else {
            val inTune = reading != null && abs(reading.cents) <= 5.0

            Box(Modifier.height(22.dp), contentAlignment = Alignment.Center) {
                if (inTune) {
                    PixelText("☆ PERFECT! ☆", fontSize = 13.sp, color = palette.mintDeep, bold = true)
                }
            }

            Box(Modifier.height(96.dp), contentAlignment = Alignment.Center) {
                if (reading != null) {
                    val noteColor = if (inTune) palette.mintDeep else palette.text
                    Row(verticalAlignment = Alignment.Bottom) {
                        PixelText(
                            reading.name.first().toString(),
                            fontSize = 66.sp,
                            color = noteColor,
                            bold = true,
                        )
                        Column {
                            PixelText(
                                if (reading.name.length > 1) "#" else " ",
                                fontSize = 26.sp,
                                color = noteColor,
                                bold = true,
                            )
                            PixelText("${reading.octave}", fontSize = 26.sp, color = palette.textMuted)
                        }
                    }
                } else {
                    PixelText("--", fontSize = 66.sp, color = palette.text.copy(alpha = 0.2f), bold = true)
                }
            }

            PixelText(
                reading?.let {
                    "${it.solfege} · 목표 ${String.format(Locale.US, "%.1f", it.targetFrequency)} Hz"
                } ?: "소리를 기다리는 중...",
                fontSize = 10.sp,
                color = palette.textMuted,
            )

            Spacer(Modifier.weight(1f))

            CentsMeter(cents = reading?.cents, inTune = inTune)

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                RetroPanel(Modifier.weight(1f)) {
                    PixelText(
                        reading?.let { String.format(Locale.US, "%+.1f ct", it.cents) } ?: "--.- ct",
                        fontSize = 14.sp,
                        bold = true,
                    )
                }
                RetroPanel(Modifier.weight(1f)) {
                    PixelText(
                        reading?.let { String.format(Locale.US, "%.1f Hz", it.frequency) } ?: "---.- Hz",
                        fontSize = 14.sp,
                        bold = true,
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            val hint = buildString {
                append(instrument.label)
                append(" · A4 ")
                append(a4)
                append("Hz")
                if (calibration != 0f) append(String.format(Locale.US, " · 보정 %+.1fct", calibration))
                if (instrument.strings.isNotEmpty()) {
                    append("\n")
                    append(instrument.stringHint())
                }
            }
            PixelText(
                hint,
                fontSize = 9.sp,
                color = palette.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
            )
        }
    }
}

/**
 * Horizontal cents meter: a boxed scale running flat ♭ on the left through
 * dead-on in the middle to sharp ♯ on the right, with a pixel needle.
 */
@Composable
fun CentsMeter(cents: Double?, inTune: Boolean, modifier: Modifier = Modifier) {
    val palette = LocalRetro.current
    val needle by animateFloatAsState(
        targetValue = (cents ?: 0.0).toFloat().coerceIn(-50f, 50f),
        animationSpec = tween(110),
        label = "needle",
    )
    val active = cents != null
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = PixelFont, fontSize = 9.sp, color = palette.textMuted)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.panel)
            .border(2.dp, palette.border)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            PixelText("♭ 낮음", fontSize = 10.sp, bold = true, color = palette.textMuted)
            Spacer(Modifier.weight(1f))
            PixelText("높음 ♯", fontSize = 10.sp, bold = true, color = palette.textMuted)
        }

        Spacer(Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
        ) {
            val w = size.width
            val trackTop = 22.dp.toPx()
            val trackH = 20.dp.toPx()
            val tickTop = trackTop + trackH + 3.dp.toPx()

            /** cents -> x position across the full width */
            fun x(c: Float) = (c + 50f) / 100f * w

            // Coloured zones, widest at the extremes and mint in the middle.
            fun zone(from: Float, to: Float, color: Color) {
                drawRect(
                    color = color,
                    topLeft = Offset(x(from), trackTop),
                    size = Size(x(to) - x(from), trackH),
                )
            }
            zone(-50f, -20f, palette.purple)
            zone(-20f, -5f, palette.pink)
            zone(-5f, 5f, if (inTune) palette.mintDeep else palette.mint)
            zone(5f, 20f, palette.pink)
            zone(20f, 50f, palette.purple)
            drawRect(
                color = palette.border,
                topLeft = Offset(0f, trackTop),
                size = Size(w, trackH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
            )

            // Counter ticks under the track.
            for (c in -50..50 step 5) {
                val major = c % 25 == 0
                val h = (if (major) 13.dp else 7.dp).toPx()
                val tw = (if (major) 3.dp else 2.dp).toPx()
                drawRect(
                    color = palette.border,
                    topLeft = Offset(x(c.toFloat()) - tw / 2f, tickTop),
                    size = Size(tw, h),
                )
            }

            for ((c, label) in listOf(-50 to "-50", -25 to "-25", 0 to "0", 25 to "+25", 50 to "+50")) {
                val measured = textMeasurer.measure(AnnotatedString(label), labelStyle)
                val cx = (x(c.toFloat()) - measured.size.width / 2f)
                    .coerceIn(0f, w - measured.size.width)
                drawText(measured, topLeft = Offset(cx, tickTop + 15.dp.toPx()))
            }

            // Needle: a pixel arrow sitting on the track, dark until it locks in.
            val nx = x(needle)
            val needleColor = when {
                !active -> palette.border.copy(alpha = 0.25f)
                inTune -> palette.mintDeep
                else -> palette.border
            }
            val halfW = 7.dp.toPx()
            val headH = 12.dp.toPx()
            val head = Path().apply {
                moveTo(nx, trackTop + headH)
                lineTo(nx - halfW, trackTop - 4.dp.toPx())
                lineTo(nx + halfW, trackTop - 4.dp.toPx())
                close()
            }
            drawPath(head, needleColor)
            drawRect(
                color = needleColor,
                topLeft = Offset(nx - 2.dp.toPx(), trackTop),
                size = Size(4.dp.toPx(), trackH + 3.dp.toPx()),
            )
        }
    }
}
