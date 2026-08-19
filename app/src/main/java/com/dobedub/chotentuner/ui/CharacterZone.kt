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
import com.dobedub.chotentuner.CharacterMood
import com.dobedub.chotentuner.R
import com.dobedub.chotentuner.ui.theme.LocalRetro
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

private const val BUST_ASPECT = 500f / 510f

/**
 * The mascot area under the main window: the character bust peeks from the
 * bottom edge of the screen, gently animated according to the app state.
 * Tapping her makes her talk (via the dialogue window).
 */
@Composable
fun CharacterZone(
    dark: Boolean,
    mood: CharacterMood,
    onPoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    BoxWithConstraints(modifier.clipToBounds()) {
        val bustHeight = min(maxHeight.value, (maxWidth * 0.92f / BUST_ASPECT).value).dp
        val topPad = (maxHeight - bustHeight).let { if (it > 0.dp) it else 0.dp }

        val transition = rememberInfiniteTransition(label = "chara")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2.0 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                tween(
                    durationMillis = when (mood) {
                        CharacterMood.SING -> 900
                        CharacterMood.PERFECT -> 1100
                        else -> 3000
                    },
                    easing = LinearEasing,
                ),
            ),
            label = "phase",
        )
        val bobAmp = when (mood) {
            CharacterMood.IDLE -> 4f
            CharacterMood.SING -> 7f
            CharacterMood.PERFECT -> 6f
            CharacterMood.OFF -> 2f
            CharacterMood.SHOCK -> 1f
        }
        val wobble = if (mood == CharacterMood.OFF) sin(phase * 2) * 2.2f else 0f

        val interaction = remember { MutableInteractionSource() }
        Image(
            painter = painterResource(if (dark) R.drawable.ame_bust else R.drawable.choten_bust),
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

        if (mood == CharacterMood.PERFECT || mood == CharacterMood.SING) {
            val symbol = if (mood == CharacterMood.PERFECT) "☆" else "♪"
            val pulse = (sin(phase) + 1f) / 2f
            PixelText(
                symbol,
                fontSize = 18.sp,
                color = palette.pinkDeep,
                bold = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = (-86).dp, y = topPad + 26.dp)
                    .alpha(pulse),
            )
            PixelText(
                symbol,
                fontSize = 14.sp,
                color = palette.purple,
                bold = true,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = 90.dp, y = topPad + 54.dp)
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
