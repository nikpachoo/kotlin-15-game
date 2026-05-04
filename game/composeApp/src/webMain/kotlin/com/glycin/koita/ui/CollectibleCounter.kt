package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.composables.isCompact

private val COLLECTIBLES_CORNER_RADIUS = 6.dp
private val MATERIALS_COLOR = Color(0xFF5D564E)
private val MINERAL_COLOR = Color(0xFF7B4D41)

@Composable
fun CollectiblesPanel(
    minerals: Int,
    simple: Int,
    rich: Int,
    modifier: Modifier = Modifier,
) {
    val compact = isCompact()
    Column(
        modifier = modifier
            .hudPanel(COLLECTIBLES_CORNER_RADIUS)
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 6.dp else 8.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        CollectibleCounter("MATERIALS", simple, MATERIALS_COLOR)
        CollectibleCounter("MINERAL", minerals, MINERAL_COLOR)
        CollectibleCounter("ORE", rich, HudColors.ORE_COLOR)
    }
}

@Composable
private fun CollectibleCounter(
    label: String,
    collectableCount: Int,
    color: Color,
) {
    val compact = isCompact()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 10.dp else 12.dp)
                .background(color, shape = CircleShape)
                .border(1.dp, HudColors.PANEL_BORDER, CircleShape)
        )

        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = if (compact) 10.sp else 12.sp,
            color = Color.LightGray,
            modifier = Modifier
                .weight(1f)
                .padding(start = if (compact) 6.dp else 8.dp),
        )

        Text(
            text = "x$collectableCount",
            fontFamily = pixelFont(),
            fontSize = if (compact) 11.sp else 14.sp,
            color = Color.White,
        )
    }
}
