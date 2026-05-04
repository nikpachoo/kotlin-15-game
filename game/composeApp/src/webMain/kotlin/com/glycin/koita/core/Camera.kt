package com.glycin.koita.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.glycin.koita.world.WorldConstants
import kotlin.math.max
import kotlin.math.min

class Camera(
    var position: Vec2,
    var canvasWidth: Float = 0f,
    var canvasHeight: Float = 0f,
) {
    var scale: Float = 1f
        private set
    var offsetX: Float = 0f
        private set
    var offsetY: Float = 0f
        private set
    var actualWidth: Float = 0f
        private set
    var actualHeight: Float = 0f
        private set

    private var halfWidth: Float = 0f
    private var halfHeight: Float = 0f
    private var lastCompact: Boolean = false
    private var lastPillarPx: Float = -1f

    val screenPosition get() = Offset(halfWidth, halfHeight)

    fun updateViewport(
        newActualWidth: Float,
        newActualHeight: Float,
        compact: Boolean = false,
        pillarReservationPx: Float = 0f,
    ) {
        if (newActualWidth == actualWidth &&
            newActualHeight == actualHeight &&
            compact == lastCompact &&
            pillarReservationPx == lastPillarPx
        ) return

        actualWidth = newActualWidth
        actualHeight = newActualHeight
        lastCompact = compact
        lastPillarPx = pillarReservationPx

        val virtualWidth = if (compact) WorldConstants.COMPACT_VIRTUAL_WIDTH else WorldConstants.VIRTUAL_WIDTH
        val virtualHeight = if (compact) WorldConstants.COMPACT_VIRTUAL_HEIGHT else WorldConstants.VIRTUAL_HEIGHT
        val pillar = if (compact) pillarReservationPx else 0f
        val gameAreaW = max(newActualWidth - 2f * pillar, 1f)
        scale = min(gameAreaW / virtualWidth, newActualHeight / virtualHeight)
        offsetX = pillar + (gameAreaW - virtualWidth * scale) / 2f
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
