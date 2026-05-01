package com.glycin.koita.gameplay.pickups

import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.pickup_damage
import koita.composeapp.generated.resources.pickup_heart
import koita.composeapp.generated.resources.pickup_light
import org.jetbrains.compose.resources.DrawableResource

object PickupCatalog {
    const val DAMAGE_UP = "Damage Up"
    const val HEALTH_UP = "Health Up"
    const val VISION_UP = "Vision Up"

    const val FRAME_SIZE = 32

    val all: List<PickupEntry> = listOf(
        PickupEntry(DAMAGE_UP, Res.drawable.pickup_damage),
        PickupEntry(HEALTH_UP, Res.drawable.pickup_heart),
        PickupEntry(VISION_UP, Res.drawable.pickup_light),
    )

    data class PickupEntry(val name: String, val sprite: DrawableResource)
}
