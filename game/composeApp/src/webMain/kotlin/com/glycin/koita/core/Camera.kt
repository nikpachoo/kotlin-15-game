package com.glycin.koita.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.glycin.koita.world.WorldConstants
import kotlin.math.min

class Camera(
    var position: Vec2,
    var canvasWidth: Float = 0f,
    var canvasHeight: Float = 0f,
) {
    var scale: Float by mutableStateOf(1f)
        private set
    var offsetX: Float by mutableStateOf(0f)
        private set
    var offsetY: Float by mutableStateOf(0f)
        private set
    var actualWidth: Float by mutableStateOf(0f)
        private set
    var actualHeight: Float by mutableStateOf(0f)
        private set

    private var halfWidth: Float = 0f
    private var halfHeight: Float = 0f

    val screenPosition get() = Offset(halfWidth, halfHeight)

    fun updateViewport(
        newActualWidth: Float,
        newActualHeight: Float,
    ) {
        if (newActualWidth == actualWidth && newActualHeight == actualHeight) return

        actualWidth = newActualWidth
        actualHeight = newActualHeight

        val virtualWidth = WorldConstants.VIRTUAL_WIDTH
        val virtualHeight = WorldConstants.VIRTUAL_HEIGHT
        scale = min(newActualWidth / virtualWidth, newActualHeight / virtualHeight)
        offsetX = (newActualWidth - virtualWidth * scale) / 2f
        offsetY = (newActualHeight - virtualHeight * scale) / 2f
        canvasWidth = virtualWidth
        canvasHeight = virtualHeight
        halfWidth = canvasWidth / 2f
        halfHeight = canvasHeight / 2f
    }

    fun actualToVirtual(actualX: Float, actualY: Float): Offset {
        return Offset(
            (actualX - offsetX) / scale,
            (actualY - offsetY) / scale,
        )
    }

    fun virtualToActual(virtualX: Float, virtualY: Float): Offset {
        return Offset(
            virtualX * scale + offsetX,
            virtualY * scale + offsetY,
        )
    }

    inline fun DrawScope.withVirtualViewport(block: DrawScope.() -> Unit) {
        translate(left = offsetX, top = offsetY) {
            scale(scale = this@Camera.scale, pivot = Offset.Zero) {
                block()
            }
        }
    }

    fun worldToScreen(worldX: Float, worldY: Float): Offset {
        return Offset(
            worldX - position.x + halfWidth,
            worldY - position.y + halfHeight,
        )
    }

    fun screenToWorld(screenX: Float, screenY: Float): Vec2 {
        return Vec2(
            x = screenX - halfWidth + position.x,
            y = screenY - halfHeight + position.y,
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
