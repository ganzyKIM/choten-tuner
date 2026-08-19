package com.dobedub.chotentuner.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.dobedub.chotentuner.R

/**
 * 초텐짱 design tokens. Two palettes: the light angel form (초텐짱)
 * and the dark form (아메) toggled by the 변신 button.
 */
@Immutable
data class RetroPalette(
    val bg: Color,
    val checker: Color,
    val window: Color,
    val panel: Color,
    val border: Color,
    val shadow: Color,
    val text: Color,
    val textMuted: Color,
    val pink: Color,
    val pinkDeep: Color,
    val purple: Color,
    val blue: Color,
    val mint: Color,
    val mintDeep: Color,
    val keyDark: Color,
    val keyDarkText: Color,
    val titleStart: Color,
    val titleEnd: Color,
)

val ChotenLight = RetroPalette(
    bg = Color(0xFFF8F0FF),
    checker = Color(0xFFFFFFFF),
    window = Color(0xFFFFFFFF),
    panel = Color(0xFFF8F0FF),
    border = Color(0xFF4A4A4A),
    shadow = Color(0xFF4A4A4A),
    text = Color(0xFF4A4A4A),
    textMuted = Color(0xFF9A8FA8),
    pink = Color(0xFFFFB7E5),
    pinkDeep = Color(0xFFFF8ED4),
    purple = Color(0xFFD1B3FF),
    blue = Color(0xFFB3E5FF),
    mint = Color(0xFFA9F0CB),
    mintDeep = Color(0xFF2FBF77),
    keyDark = Color(0xFF4A4A4A),
    keyDarkText = Color(0xFFFFFFFF),
    titleStart = Color(0xFFFFB7E5),
    titleEnd = Color(0xFFD1B3FF),
)

val AmeDark = RetroPalette(
    bg = Color(0xFF221C2B),
    checker = Color(0xFF2A2336),
    window = Color(0xFF322B40),
    panel = Color(0xFF262031),
    border = Color(0xFFEFE6FF),
    shadow = Color(0xFF120E1A),
    text = Color(0xFFF2EAFF),
    textMuted = Color(0xFFA798C2),
    pink = Color(0xFFD16B8A),
    pinkDeep = Color(0xFFE0526E),
    purple = Color(0xFF8F79C9),
    blue = Color(0xFF6FA8CE),
    mint = Color(0xFF3E8F6C),
    mintDeep = Color(0xFF5AD695),
    keyDark = Color(0xFF171221),
    keyDarkText = Color(0xFFF2EAFF),
    titleStart = Color(0xFFD16B8A),
    titleEnd = Color(0xFF8F79C9),
)

val LocalRetro = staticCompositionLocalOf { ChotenLight }

/** Galmuri — DOS-era style Korean pixel font (SIL OFL 1.1). */
val PixelFont = FontFamily(
    Font(R.font.galmuri11, FontWeight.Normal),
    Font(R.font.galmuri11_bold, FontWeight.Bold),
)
