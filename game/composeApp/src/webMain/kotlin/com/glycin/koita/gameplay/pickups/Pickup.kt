package com.glycin.koita.gameplay.pickups

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2

abstract class Pickup(
    val onPickup: () -> Unit,
    val position: Vec2,
) {
    var vy: Float = 0f
    abstract val spriteAnimator: SpriteAnimator
    abstract val name: String
    val size = 32

    fun update(deltaTime: Float) {
        spriteAnimator.animate(deltaTime, 0..<spriteAnimator.totalSprites)
    }
}
