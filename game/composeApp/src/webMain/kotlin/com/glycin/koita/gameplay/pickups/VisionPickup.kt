package com.glycin.koita.gameplay.pickups

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.pickup_light

class VisionPickup(
    onPickup: () -> Unit,
    position: Vec2,
): Pickup(onPickup, position) {
    override val name = "Vision Up"
    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.pickup_light,
        frameWidth = 32,
        frameHeight = 32,
        columns = 6,
        totalSprites = 6,
        frameDuration = 0.1f,
    )
}
