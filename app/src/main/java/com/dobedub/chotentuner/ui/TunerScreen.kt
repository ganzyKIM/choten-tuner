package com.dobedub.chotentuner.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.music.NoteReading
import com.dobedub.chotentuner.ui.theme.LocalRetro
import com.dobedub.chotentuner.ui.theme.PixelFont
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerScreen(
    reading: NoteReading?,
    micGranted: Boolean,
    onRequestMic: () -> Unit,
    a4: Int,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!micGranted) {
            Spacer(Modifier.height(26.dp))
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
            Spacer(Modifier.height(26.dp))
        } else {
            val inTune = reading != null && abs(reading.cents) <= 5.0

            Box(Modifier.height(20.dp), contentAlignment = Alignment.Center) {
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
                            fontSize = 64.sp,
                            color = noteColor,
                            bold = true,
                        )
                        Column {
                            PixelText(
                                if (reading.name.length > 1) "#" else " ",
                                fontSize = 25.sp,
                                color = noteColor,
                                bold = true,
                            )
                            PixelText("${reading.octave}", fontSize = 25.sp, color = palette.textMuted)
                        }
                    }
                } else {
                    PixelText("--", fontSize = 64.sp, color = palette.text.copy(alpha = 0.2f), bold = true)
                }
            }

            PixelText(
                reading?.let {
                    "${it.solfege} · 목표 ${String.format(Locale.US, "%.1f", it.targetFrequency)} Hz"
                } ?: "소리를 기다리는 중...",
                fontSize = 10.sp,
                color = palette.textMuted,
            )

            CentsGauge(cents = reading?.cents, inTune = inTune)

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
            PixelText("기준 A4 = $a4 Hz", fontSize = 9.sp, color = palette.textMuted)
        }
    }
}

/** Semicircular cents gauge: purple/pink/mint zones, pixel ticks, hard needle. */
@Composable
fun CentsGauge(cents: Double?, inTune: Boolean, modifier: Modifier = Modifier) {
    val palette = LocalRetro.current
    val needle by animateFloatAsState(
        targetValue = (cents ?: 0.0).toFloat().coerceIn(-50f, 50f),
        animationSpec = tween(90),
        label = "needle",
    )
    val active = cents != null
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = PixelFont, fontSize = 9.sp, color = palette.border)

    Canvas(modifier = modifier.fillMaxWidth().height(172.dp)) {
        val cx = size.width / 2f
        val cy = size.height - 14.dp.toPx()
        val r = minOf(size.width / 2f - 36.dp.toPx(), size.height - 52.dp.toPx())

        fun deg(c: Float) = (c / 50f) * 55f - 90f

        val stroke = Stroke(width = 12.dp.toPx())
        val arcTopLeft = Offset(cx - r, cy - r)
        val arcSize = Size(r * 2f, r * 2f)
        fun zone(from: Float, to: Float, color: Color) {
            drawArc(
                color = color,
                startAngle = deg(from),
                sweepAngle = deg(to) - deg(from),
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = stroke,
            )
        }
        zone(-50f, -20f, palette.purple)
        zone(-20f, -5f, palette.pink)
        zone(-5f, 5f, if (inTune) palette.mintDeep else palette.mint)
        zone(5f, 20f, palette.pink)
        zone(20f, 50f, palette.purple)

        for (c in -50..50 step 5) {
            val major = c % 25 == 0
            val a = Math.toRadians(deg(c.toFloat()).toDouble())
            val dirX = cos(a).toFloat()
            val dirY = sin(a).toFloat()
            val outer = r - 10.dp.toPx()
            val inner = outer - (if (major) 12.dp else 7.dp).toPx()
            drawLine(
                color = palette.border,
                start = Offset(cx + dirX * inner, cy + dirY * inner),
                end = Offset(cx + dirX * outer, cy + dirY * outer),
                strokeWidth = (if (major) 3.dp else 2.dp).toPx(),
            )
        }

        for ((c, label) in listOf(-50 to "-50", -25 to "-25", 0 to "0", 25 to "+25", 50 to "+50")) {
            val a = Math.toRadians(deg(c.toFloat()).toDouble())
            val lr = r + 16.dp.toPx()
            val x = cx + cos(a).toFloat() * lr
            val y = cy + sin(a).toFloat() * lr
            val measured = textMeasurer.measure(AnnotatedString(label), labelStyle)
            drawText(
                measured,
                topLeft = Offset(x - measured.size.width / 2f, y - measured.size.height / 2f),
            )
        }

        val na = Math.toRadians(deg(needle).toDouble())
        val needleColor = when {
            !active -> palette.border.copy(alpha = 0.2f)
            inTune -> palette.mintDeep
            else -> palette.border
        }
        val nLen = r - 26.dp.toPx()
        drawLine(
            color = needleColor,
            start = Offset(cx, cy),
            end = Offset(cx + cos(na).toFloat() * nLen, cy + sin(na).toFloat() * nLen),
            strokeWidth = 4.dp.toPx(),
        )
        val pivot = 12.dp.toPx()
        drawRect(
            color = palette.border,
            topLeft = Offset(cx - pivot / 2f, cy - pivot / 2f),
            size = Size(pivot, pivot),
        )
    }
}
