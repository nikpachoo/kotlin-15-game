package com.glycin.koita.core

import androidx.compose.ui.geometry.Offset
import com.glycin.koita.world.WorldConstants

class Camera(
    var position: Vec2,
    var canvasWidth: Float = 0f,
    var canvasHeight: Float = 0f,
) {
    val screenPosition get() = Offset(
        canvasWidth / 2f,
        canvasHeight / 2f,
    )

    fun worldToScreen(worldX: Float, worldY: Float): Offset {
        return Offset(
            worldX - position.x + canvasWidth / 2f,
            worldY - position.y + canvasHeight / 2f,
        )
    }

    fun screenToWorld(screenX: Float, screenY: Float): Vec2 {
        return Vec2(
            x = screenX - canvasWidth / 2f + position.x,
            y = screenY - canvasHeight / 2f + position.y,
        )
    }

    fun followPlayer(player: Player, deltaTime: Float, smoothness: Float = 10f) {
        val targetX = WorldConstants.WORLD_WIDTH_PIXELS / 2f
        val targetY = player.center.y
        position = Vec2(
            x = targetX,
            y = position.y + (targetY - position.y) * smoothness * deltaTime
        )
    }
}