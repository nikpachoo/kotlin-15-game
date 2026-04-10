package com.glycin.koita.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.modes.BuildMode

@Composable
fun TurretChargeIndicator(
    player: Player,
    mouse: Mouse,
    camera: Camera,
) {
    val currentWeapon = player.currentWeapon
    if (currentWeapon !is BuildMode) return

    val progress = currentWeapon.holdProgress
    if (progress <= 0f) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        with(camera) { withVirtualViewport {
            val centerX = mouse.position.x
            val centerY = mouse.position.y
            val radius = INDICATOR_RADIUS
            val strokeWidth = INDICATOR_STROKE_WIDTH

            drawArc(
                color = TRACK_COLOR,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth),
            )

            val arcColor = if (progress >= 1f) COMPLETE_COLOR else FILL_COLOR
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth),
            )
        } }
    }
}

private const val INDICATOR_RADIUS = 18f
private const val INDICATOR_STROKE_WIDTH = 3f
private val TRACK_COLOR = Color.White.copy(alpha = 0.2f)
private val FILL_COLOR = Color(0xFF00CED1)
private val COMPLETE_COLOR = Color(0xFF00FF88)
