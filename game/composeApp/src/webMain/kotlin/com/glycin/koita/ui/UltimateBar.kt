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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.ceil

@Composable
fun UltimateBar(
    cooldownRemaining: Float,
    rechargeDuration: Float,
    ultimateName: String?,
    modifier: Modifier = Modifier,
) {
    val recharged = cooldownRemaining <= 0f
    val fill = if (recharged) 1f else (1f - cooldownRemaining / rechargeDuration).coerceIn(0f, 1f)
    val label = when {
        recharged -> ultimateName ?: "ULTIMATE: AWAITING NEW UNLOCK"
        else -> "ULTIMATE: ${ceil(cooldownRemaining).toInt()}s"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = HudColors.BANNER_LABEL,
        )

        Canvas(
            modifier = Modifier
                .width(480.dp)
                .height(14.dp),
        ) {
            drawRoundRect(
                color = HudColors.ULTIMATE_BAR_BACKGROUND,
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
            )

            if (fill > 0f) {
                drawRoundRect(
                    color = HudColors.BUTTON_ACTIVE,
                    size = Size(size.width * fill, size.height),
                    cornerRadius = CornerRadius(4f, 4f),
                )
            }

            drawRoundRect(
                color = HudColors.BANNER_GOLD_TOP,
                size = size,
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 2f),
            )
        }
    }
}
