package com.dobedub.chotentuner.ui

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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.R
import com.dobedub.chotentuner.SparkleBurst
import com.dobedub.chotentuner.Sprite
import com.dobedub.chotentuner.ui.theme.LocalRetro
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Bust artwork is cropped to 400x350. */
private const val BUST_ASPECT = 400f / 350f

/** How much bigger than a plain fit-to-width the bust is drawn. */
private const val BUST_ZOOM = 1.15f

private val SPARKLE_SYMBOLS = arrayOf("☆", "★", "✧", "♪", "・")

/** One star in a burst, living out its own little arc. */
private data class Sparkle(
    val xFrac: Float,
    val yFrac: Float,
    val sizeSp: Float,
    val symbol: String,
    val colorIndex: Int,
    val bornAt: Long,
    val life: Long,
    val riseDp: Float,
    val driftDp: Float,
)

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

/** Scatters a burst in a ring around the character's upper body. */
private fun newSparkle(seed: Int, bornAt: Long, index: Int, total: Int): Sparkle {
    val rng = Random(seed * 1000 + index)
    // Spread evenly around the ring, then jitter so it never looks like a dial.
    val angle = (2.0 * PI * index / total) + rng.nextDouble(-0.35, 0.35)
    val radius = rng.nextDouble(0.19, 0.38)
    return Sparkle(
        xFrac = (0.5 + cos(angle) * radius).toFloat(),
        yFrac = (0.44 + sin(angle) * radius * 0.78).toFloat(),
        sizeSp = rng.nextDouble(11.0, 22.0).toFloat(),
        symbol = SPARKLE_SYMBOLS[rng.nextInt(SPARKLE_SYMBOLS.size)],
        colorIndex = rng.nextInt(3),
        bornAt = bornAt + rng.nextLong(0, 420),
        life = rng.nextLong(1500, 2600),
        riseDp = rng.nextDouble(14.0, 40.0).toFloat(),
        driftDp = rng.nextDouble(-10.0, 10.0).toFloat(),
    )
}

/**
 * The mascot area under the main window: her bust sits still at the bottom of
 * the screen, posed to match what the app is doing, and stars burst around her
 * when she is poked or transforms.
 */
@Composable
fun CharacterZone(
    dark: Boolean,
    sprite: Sprite,
    burst: SparkleBurst,
    onPoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalRetro.current
    val sparkles = remember { mutableStateListOf<Sparkle>() }
    var now by remember { mutableLongStateOf(0L) }

    LaunchedEffect(burst.id) {
        if (burst.count > 0) {
            val born = System.currentTimeMillis()
            repeat(burst.count) { sparkles.add(newSparkle(burst.id, born, it, burst.count)) }
        }
        while (sparkles.isNotEmpty()) {
            withFrameMillis { }
            val t = System.currentTimeMillis()
            now = t
            sparkles.removeAll { t - it.bornAt > it.life }
        }
    }

    BoxWithConstraints(modifier.clipToBounds()) {
        val fitted = min(maxHeight.value, (maxWidth / BUST_ASPECT).value)
        val bustHeight = (fitted * BUST_ZOOM).dp
        val topPad = (maxHeight - bustHeight).coerceAtLeast(0.dp)

        val interaction = remember { MutableInteractionSource() }
        Image(
            painter = painterResource(drawableFor(sprite, dark)),
            contentDescription = if (dark) "아메" else "초텐짱",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPad)
                .height(bustHeight)
                .aspectRatio(BUST_ASPECT)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onPoke,
                ),
        )

        val sparkleColors = listOf(palette.pinkDeep, palette.purple, palette.blue)
        for (s in sparkles) {
            val age = now - s.bornAt
            if (age < 0) continue
            val p = (age.toFloat() / s.life).coerceIn(0f, 1f)
            // Pop in fast, hold, then drift away.
            val alpha = when {
                p < 0.12f -> p / 0.12f
                p > 0.55f -> ((1f - p) / 0.45f)
                else -> 1f
            }.coerceIn(0f, 1f)
            val scale = 0.55f + 0.45f * (p / 0.22f).coerceAtMost(1f)
            PixelText(
                s.symbol,
                fontSize = s.sizeSp.sp,
                color = sparkleColors[s.colorIndex],
                bold = true,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = maxWidth * s.xFrac + (s.driftDp * p).dp,
                        y = maxHeight * s.yFrac - (s.riseDp * p).dp,
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .alpha(alpha),
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
