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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dobedub.chotentuner.ui.theme.LocalRetro

@Composable
fun SettingsOverlay(
    a4: Int,
    onA4: (Int) -> Unit,
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
                .width(300.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PixelText("기준음 A4 보정", fontSize = 13.sp, bold = true)
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RetroButton(
                        onClick = { onA4(a4 - 1) },
                        enabled = a4 > 435,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(42.dp),
                    ) {
                        PixelText("-", fontSize = 18.sp, bold = true)
                    }
                    RetroPanel(Modifier.width(120.dp)) {
                        PixelText("$a4 Hz", fontSize = 16.sp, bold = true)
                    }
                    RetroButton(
                        onClick = { onA4(a4 + 1) },
                        enabled = a4 < 445,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(42.dp),
                    ) {
                        PixelText("+", fontSize = 18.sp, bold = true)
                    }
                }
                Spacer(Modifier.height(8.dp))
                PixelText("범위 435~445 Hz", fontSize = 9.sp, color = palette.textMuted)
                Spacer(Modifier.height(12.dp))
                RetroButton(onClick = { onA4(440) }, background = palette.blue) {
                    PixelText("기본값 440 으로!", fontSize = 11.sp, bold = true)
                }
                Spacer(Modifier.height(12.dp))
                PixelText("초텐짱의 비밀 설정창~☆", fontSize = 9.sp, color = palette.textMuted)
            }
        }
    }
}
