package com.glycin.koita.gameplay

object SpawnSettings {

    val ZONE_1 = SpawnZone(
        depthMin = 0.00f,
        depthMax = 0.20f,
        slime = 15..20,
        stoneGolem = 3..5,
        hydra = 3..5,
        spider = 0..0,
        wraith = 0..0,
        phantom = 0..0,
        confuser = 0..0,
        shrines = 2..3,
        pickups = 2..4,
    )

    val ZONE_2 = SpawnZone(
        depthMin = 0.20f,
        depthMax = 0.35f,
        slime = 10..15,
        stoneGolem = 4..6,
        hydra = 5..8,
        spider = 3..5,
        wraith = 1..2,
        phantom = 0..0,
        confuser = 0..0,
        shrines = 2..3,
        pickups = 5..8,
    )

    val ZONE_3 = SpawnZone(
        depthMin = 0.35f,
        depthMax = 0.50f,
        slime = 5..8,
        stoneGolem = 2..4,
        hydra = 4..6,
        spider = 5..8,
        wraith = 3..5,
        phantom = 2..4,
        confuser = 0..1,
        shrines = 1..3,
        pickups = 8..12,
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
        confuser = 2..4,
        shrines = 1..2,
        pickups = 5..8,
    )

    val ZONE_5 = SpawnZone(
        depthMin = 0.70f,
        depthMax = 0.90f,
        slime = 0..0,
        stoneGolem = 0..0,
        hydra = 1..2,
        spider = 2..3,
        wraith = 3..5,
        phantom = 8..12,
        confuser = 5..8,
        shrines = 0..1,
        pickups = 2..4,
    )

    val ALL_ZONES = listOf(ZONE_1, ZONE_2, ZONE_3, ZONE_4, ZONE_5)
}
