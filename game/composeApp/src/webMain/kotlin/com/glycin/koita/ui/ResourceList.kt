package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.composables.compactOr
import com.glycin.koita.util.formatScore

@Composable
fun ResourceList(
    materials: Int,
    minerals: Int,
    ore: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(compactOr(6.dp, 8.dp)),
    ) {
        ResourceRow("materials", materials, HudColors.MATERIALS_COLOR)
        ResourceRow("minerals", minerals, HudColors.MINERALS_COLOR)
        ResourceRow("ore", ore, HudColors.ORE_COLOR)
    }
}

@Composable
private fun ResourceRow(
    label: String,
    count: Int,
    dotColor: Color,
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "x${count.formatScore()}",
            fontFamily = pixelFont(),
            fontSize = compactOr(18.sp, 22.sp),
            color = dotColor,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(compactOr(4.dp, 6.dp)),
        ) {
            Box(
                modifier = Modifier
                    .size(compactOr(8.dp, 10.dp))
                    .background(dotColor, CircleShape)
                    .border(1.dp, HudColors.PANEL_BORDER, CircleShape),
            )
            Text(
                text = label,
                fontFamily = pixelFont(),
                fontSize = compactOr(10.sp, 12.sp),
                color = dotColor,
            )
        }
    }
}
