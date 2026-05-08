package com.glycin.koita.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BAR_BACKGROUND_COLOR = Color(0xFF220000)
private val BAR_FILL_COLOR = Color(0xFFCC44FF)

@Composable
fun BossHealthBar(
    healthPercent: Float,
    bossName: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = bossName,
            fontFamily = pixelFont(),
            fontSize = 22.sp,
            color = Color.White,
        )

        Canvas(
            modifier = Modifier
                .width(600.dp)
                .height(16.dp),
        ) {
            drawRoundRect(
                color = BAR_BACKGROUND_COLOR,
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
            )

            if (healthPercent > 0f) {
                drawRoundRect(
                    color = BAR_FILL_COLOR,
                    size = Size(size.width * healthPercent, size.height),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }

            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 2f),
            )
        }
    }
}
