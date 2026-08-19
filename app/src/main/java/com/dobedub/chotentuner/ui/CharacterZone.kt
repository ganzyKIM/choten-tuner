package com.dobedub.chotentuner.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.R
import com.dobedub.chotentuner.Sprite
import com.dobedub.chotentuner.ui.theme.LocalRetro
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/** Bust artwork is cropped to 400x350. */
private const val BUST_ASPECT = 400f / 350f

/** How much bigger than a plain fit-to-width the bust is drawn. */
private const val BUST_ZOOM = 1.15f

private fun drawableFor(sprite: Sprite, dark: Boolean): Int = if (dark) {
    when (sprite) {
        Sprite.NEUTRAL -> R.drawable.ame_default
        Sprite.JOY -> R.drawable.ame_dere
        Sprite.SHARP -> R.drawable.ame_yandere
        Sprite.FLAT -> R.drawable.ame_smoking
        Sprite.SHY -> R.drawable.ame_dere
        Sprite.DARK -> R.drawable.ame_drug
    }
} else {
    when (sprite) {
        Sprite.NEUTRAL -> R.drawable.choten_default
        Sprite.JOY -> R.drawable.choten_peace
        Sprite.SHARP -> R.drawable.choten_angry
        Sprite.FLAT -> R.drawable.choten_vape
        Sprite.SHY -> R.drawable.choten_dere
        Sprite.DARK -> R.drawable.choten_vape
    }
}

/**
 * The mascot area under the main window: her bust peeks up from the bottom of
 * the screen, posed and animated to match what the app is doing.
 * Tapping her makes her talk (through the shared dialogue window).
 */
@Composable
fun CharacterZone(
    dark: Boolean,
    sprite: Sprite,
    onPoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    BoxWithConstraints(modifier.clipToBounds()) {
        val fitted = min(maxHeight.value, (maxWidth / BUST_ASPECT).value)
        val bustHeight = (fitted * BUST_ZOOM).dp
        val topPad = (maxHeight - bustHeight).coerceAtLeast(0.dp)

        val transition = rememberInfiniteTransition(label = "chara")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                tween(
                    durationMillis = when (sprite) {
                        Sprite.JOY -> 1000
                        Sprite.SHY -> 1400
                        Sprite.SHARP -> 340
                        Sprite.FLAT -> 4200
                        Sprite.DARK -> 3600
                        Sprite.NEUTRAL -> 3000
                    },
                    easing = LinearEasing,
                ),
            ),
            label = "phase",
        )
        val bobAmp = when (sprite) {
            Sprite.JOY -> 6f
            Sprite.SHY -> 4f
            Sprite.SHARP -> 1.5f
            Sprite.FLAT -> 3f
            Sprite.DARK -> 3f
            Sprite.NEUTRAL -> 4f
        }
        val wobble = if (sprite == Sprite.SHARP) sin(phase) * 1.6f else 0f

        val interaction = remember { MutableInteractionSource() }
        Image(
            painter = painterResource(drawableFor(sprite, dark)),
            contentDescription = if (dark) "아메" else "초텐짱",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPad)
                .height(bustHeight)
                .aspectRatio(BUST_ASPECT)
                .graphicsLayer {
                    translationY = sin(phase) * bobAmp * density
                    rotationZ = wobble
                }
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onPoke,
                ),
        )

        if (sprite == Sprite.JOY || sprite == Sprite.DARK) {
            val symbol = if (sprite == Sprite.JOY) "☆" else "♪"
            val pulse = (sin(phase) + 1f) / 2f
            PixelText(
                symbol,
                fontSize = 18.sp,
                color = palette.pinkDeep,
                bold = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-96).dp, y = topPad + 22.dp)
                    .alpha(pulse),
            )
            PixelText(
                symbol,
                fontSize = 14.sp,
                color = palette.purple,
                bold = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 100.dp, y = topPad + 52.dp)
                    .alpha(1f - pulse),
            )
        }

        PixelText(
            "made by 간지김",
            fontSize = 9.sp,
            color = palette.textMuted,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 4.dp),
        )
    }
}
