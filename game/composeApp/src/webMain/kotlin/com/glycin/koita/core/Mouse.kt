package com.glycin.koita.core

import androidx.compose.ui.geometry.Offset

class Mouse(
    var position: Offset = Offset.Zero,
    var worldPosition: Vec2 = Vec2.zero(),
    var isLeftPressed: Boolean = false,
    var isRightPressed: Boolean = false,
    var isLeftJustPressed: Boolean = false,
    var isRightJustPressed: Boolean = false
) {
    private var wasLeftPressed = false
    private var wasRightPressed = false

    fun updatePosition(screenPos: Offset, wPosition: Vec2) {
        position = screenPos
        worldPosition = wPosition
    }

    fun updateButtons(leftPressed: Boolean, rightPressed: Boolean) {
        isLeftJustPressed = leftPressed && !wasLeftPressed
        isRightJustPressed = rightPressed && !wasRightPressed

        isLeftPressed = leftPressed
        isRightPressed = rightPressed

        wasLeftPressed = leftPressed
        wasRightPressed = rightPressed
    }

    fun reset() {
        isLeftJustPressed = false
        isRightJustPressed = false
    }
}