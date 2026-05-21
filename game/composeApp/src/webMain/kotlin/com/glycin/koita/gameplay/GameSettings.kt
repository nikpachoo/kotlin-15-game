package com.glycin.koita.gameplay

object GameSettings {
    const val BASE_LIGHT_RADIUS = 175f
    const val FALL_OFF_DISTANCE = 150f

    const val BLOCK_COST = 100
    const val TURRET_COST = BLOCK_COST * 50
    const val BOUNCY_COST = BLOCK_COST * 10
    const val DYNAMITE_COST = BLOCK_COST * 10

    // Relative spawn weights for pickups (higher = more common)
    const val PICKUP_WEIGHT_DAMAGE = 2
    const val PICKUP_WEIGHT_VISION = 3
    const val PICKUP_WEIGHT_HEALTH = 5
}