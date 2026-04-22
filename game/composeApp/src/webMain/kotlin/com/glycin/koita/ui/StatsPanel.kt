package com.glycin.koita.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val STATS_CORNER_RADIUS = 6.dp

@Composable
fun StatsPanel(
    score: Int,
    elapsedSeconds: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .hudPanel(STATS_CORNER_RADIUS)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$score",
            fontFamily = pixelFont(),
            fontSize = 22.sp,
            color = HudColors.PANEL_ACCENT,
        )
        Text(
            text = formatTime(elapsedSeconds),
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.LightGray,
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = (seconds / 60).toString().padStart(2, '0')
    val s = (seconds % 60).toString().padStart(2, '0')
    return "$m:$s"
}
