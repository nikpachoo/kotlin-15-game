package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.hudPanel(cornerRadius: Dp = 4.dp): Modifier = composed {
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    clip(shape)
        .background(HudColors.PANEL_BACKGROUND)
        .border(1.dp, HudColors.PANEL_BORDER, shape)
}
