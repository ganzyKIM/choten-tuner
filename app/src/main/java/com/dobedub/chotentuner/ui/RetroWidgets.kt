package com.dobedub.chotentuner.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.ui.theme.LocalRetro
import com.dobedub.chotentuner.ui.theme.PixelFont
import kotlin.math.ceil
import kotlinx.coroutines.delay

/** Pixel-font text; color defaults to the palette's text color. */
@Composable
fun PixelText(
    text: String,
    fontSize: TextUnit = 12.sp,
    color: Color = Color.Unspecified,
    bold: Boolean = false,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    val resolved = if (color == Color.Unspecified) LocalRetro.current.text else color
    Text(
        text = text,
        modifier = modifier,
        color = resolved,
        fontSize = fontSize,
        fontFamily = PixelFont,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = textAlign,
        lineHeight = lineHeight,
    )
}

/** The 초텐짱 checkerboard wallpaper. */
@Composable
fun Checkerboard(modifier: Modifier = Modifier) {
    val palette = LocalRetro.current
    Canvas(modifier) {
        drawRect(palette.bg)
        val cell = 14.dp.toPx()
        val cols = ceil(size.width / cell).toInt()
        val rows = ceil(size.height / cell).toInt()
        for (r in 0..rows) {
            for (c in 0..cols) {
                if ((r + c) % 2 == 0) {
                    drawRect(
                        color = palette.checker,
                        topLeft = Offset(c * cell, r * cell),
                        size = Size(cell, cell),
                    )
                }
            }
        }
    }
}

/** Hard offset shadow with no blur — the Win95 look. */
fun Modifier.hardShadow(offset: Dp, color: Color): Modifier = drawBehind {
    val o = offset.toPx()
    drawRect(color = color, topLeft = Offset(o, o), size = size)
}

@Composable
private fun TitleBarButton(label: String, onClick: (() -> Unit)? = null) {
    val palette = LocalRetro.current
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(palette.window)
            .border(2.dp, palette.border)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick,
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        PixelText(label, fontSize = 8.sp, bold = true)
    }
}

/** A Win95-style window: gradient title bar with ─ □ ✕, thick border, hard shadow. */
@Composable
fun RetroWindow(
    title: String,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val palette = LocalRetro.current
    Column(
        modifier = modifier
            .hardShadow(4.dp, palette.shadow)
            .background(palette.window)
            .border(3.dp, palette.border)
            .padding(3.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .background(Brush.horizontalGradient(listOf(palette.titleStart, palette.titleEnd)))
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PixelText(title, fontSize = 11.sp, bold = true)
            Spacer(Modifier.weight(1f))
            TitleBarButton("─")
            Spacer(Modifier.width(4.dp))
            TitleBarButton("□")
            Spacer(Modifier.width(4.dp))
            TitleBarButton("✕", onClick = onClose)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(palette.border)
        )
        content()
    }
}

/** Chunky button that physically presses down (translate + shadow collapse). */
@Composable
fun RetroButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    background: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalRetro.current
    val bg = if (background == Color.Unspecified) palette.window else background
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val shift = if (pressed && enabled) 2.dp else 0.dp
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else 0.45f)
            .offset(x = shift, y = shift)
            .then(if (pressed && enabled) Modifier else Modifier.hardShadow(2.dp, palette.shadow))
            .background(bg)
            .border(2.dp, palette.border)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Inset readout panel (for LCD-ish numbers and status lines). */
@Composable
fun RetroPanel(
    modifier: Modifier = Modifier,
    background: Color = Color.Unspecified,
    contentPadding: PaddingValues = PaddingValues(8.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    val palette = LocalRetro.current
    val bg = if (background == Color.Unspecified) palette.window else background
    Box(
        modifier = modifier
            .background(bg)
            .border(2.dp, palette.border)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** The mascot's dialogue window: typewriter reveal + blinking DOS cursor. */
@Composable
fun StatusLine(message: String, modifier: Modifier = Modifier) {
    val palette = LocalRetro.current
    var count by remember { mutableIntStateOf(0) }
    LaunchedEffect(message) {
        count = 0
        while (count < message.length) {
            delay(20)
            count++
        }
    }
    val blink = rememberInfiniteTransition(label = "cursor")
    val alpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse),
        label = "alpha",
    )
    RetroPanel(
        modifier = modifier.fillMaxWidth(),
        background = palette.panel,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
            PixelText(message.take(count), fontSize = 10.sp, lineHeight = 16.sp)
            PixelText("_", fontSize = 10.sp, bold = true, modifier = Modifier.alpha(alpha))
        }
    }
}
