package com.glycin.koita.gameplay.pickups

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.pickup_heart

class HealthPickup(
    onPickup: () -> Unit,
    position: Vec2,
): Pickup(onPickup, position) {
    override val name = PickupCatalog.HEALTH_UP
    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.pickup_heart,
        frameWidth = 32,
        frameHeight = 32,
        columns = 6,
        totalSprites = 6,
        frameDuration = 0.1f,
    )
}
