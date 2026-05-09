package com.glycin.koita.gameplay

import com.glycin.koita.world.WorldConstants

object SpawnSettings {

    const val ABOVE_SURFACE_MULTIPLIER = 5

    val ZONE_1 = SpawnZone(
        depthMin = 0.00f,
        depthMax = 0.20f,
        slime = 5..15,
        stoneGolem = 1..5,
        hydra = 0..2,
        spider = 0..0,
        wraith = 0..0,
        phantom = 0..0,
        confuser = 0..0,
        shrines = 2..3,
        pickups = 2..4,
        scoreMultiplier = 1,
    )

    val ZONE_2 = SpawnZone(
        depthMin = 0.20f,
        depthMax = 0.35f,
        slime = 5..10,
        stoneGolem = 4..6,
        hydra = 1..5,
        spider = 1..3,
        wraith = 1..2,
        phantom = 0..0,
        confuser = 0..0,
        shrines = 2..3,
        pickups = 5..8,
        scoreMultiplier = 2,
    )

    val ZONE_3 = SpawnZone(
        depthMin = 0.35f,
        depthMax = 0.50f,
        slime = 5..8,
        stoneGolem = 2..4,
        hydra = 2..5,
        spider = 5..8,
        wraith = 3..5,
        phantom = 2..4,
        confuser = 0..1,
        shrines = 1..3,
        pickups = 8..12,
        scoreMultiplier = 3,
    )

    val ZONE_4 = SpawnZone(
        depthMin = 0.50f,
        depthMax = 0.70f,
        slime = 2..4,
        stoneGolem = 1..2,
        hydra = 2..3,
        spider = 3..5,
        wraith = 5..8,
        phantom = 5..8,
        confuser = 5..8,
        shrines = 1..2,
        pickups = 5..8,
        scoreMultiplier = 4,
    )

    val ZONE_5 = SpawnZone(
        depthMin = 0.70f,
        depthMax = 0.95f,
        slime = 0..0,
        stoneGolem = 0..0,
        hydra = 1..2,
        spider = 2..3,
        wraith = 3..5,
        phantom = 8..12,
        confuser = 9..13,
        shrines = 0..1,
        pickups = 2..4,
        scoreMultiplier = 5,
    )

    val ALL_ZONES = listOf(ZONE_1, ZONE_2, ZONE_3, ZONE_4, ZONE_5)

    fun scoreMultiplierForY(y: Float): Int {
        val depth = 1f - y / WorldConstants.WORLD_HEIGHT_PIXELS
        if (depth >= ALL_ZONES.last().depthMax) return ABOVE_SURFACE_MULTIPLIER
        for (i in ALL_ZONES.indices) {
            val zone = ALL_ZONES[i]
            if (depth >= zone.depthMin && depth < zone.depthMax) return zone.scoreMultiplier
        }
        return 1
    }
}
