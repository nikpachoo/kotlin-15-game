package com.glycin.koita.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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

@Composable
fun BossHealthBar(
    healthPercent: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val barWidth = 600.dp
        val barHeight = 16.dp

        Text(
            text = "The Final Void",
            fontFamily = pixelFont(),
            fontSize = 22.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(barHeight + 8.dp)),
        )

        Canvas(
            modifier = Modifier
                .width(barWidth)
                .height(barHeight)
                .align(Alignment.BottomCenter),
        ) {
            // Background
            drawRoundRect(
                color = Color(0xFF220000),
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
            )

            // Health fill
            if (healthPercent > 0f) {
                drawRoundRect(
                    color = Color(0xFFCC44FF),
                    size = Size(size.width * healthPercent, size.height),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }

            // Border
            drawRoundRect(
                color = Color.White,
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 2f),
            )
        }
    }
}
