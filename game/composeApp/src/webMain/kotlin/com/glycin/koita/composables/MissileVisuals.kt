package com.glycin.koita.composables

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope

internal fun DrawScope.drawMissileVisual(center: Offset, dirX: Float, dirY: Float) {
    drawCircle(
        color = WorldRendererColors.MISSILE,
        radius = 5f,
        center = center,
    )
    drawCircle(
        color = WorldRendererColors.MISSILE_CORE,
        radius = 2.5f,
        center = center,
    )
    drawRect(
        color = WorldRendererColors.MISSILE_TAIL,
        topLeft = Offset(center.x - dirX * 12f - 2f, center.y - dirY * 12f - 2f),
        size = Size(4f, 4f),
    )
}
