package com.glycin.koita.gameplay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player

class FogOfWar(
    private val camera: Camera,
    private val player: Player,
    private val gameState: GameState,
) {
    fun render(
        drawScope: DrawScope,
    ) {
        with(drawScope) {
            val playerScreenPos = camera.worldToScreen(
                player.center.x,
                player.center.y,
            )
            val lightRadius = GameSettings.BASE_LIGHT_RADIUS * gameState.visionMultiplier
            val totalRadius = lightRadius + (GameSettings.FALL_OFF_DISTANCE * gameState.visionFallOfMultiplier)
            val innerStop = lightRadius / totalRadius

            val gradient = Brush.radialGradient(
                0.0f to Color.Transparent,
                innerStop to Color.Transparent,
                1.0f to Color.Black,
                center = Offset(playerScreenPos.x, playerScreenPos.y),
                radius = totalRadius
            )

            drawRect(
                brush = gradient,
                topLeft = Offset.Zero,
                size = Size(camera.canvasWidth, camera.canvasHeight),
            )
        }
    }
}