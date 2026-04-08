package com.glycin.koita.gameplay

import com.glycin.koita.world.WorldConstants

data class SpawnZone(
    val depthMin: Float,
    val depthMax: Float,
    val slime: IntRange,
    val stoneGolem: IntRange,
    val hydra: IntRange,
    val spider: IntRange,
    val wraith: IntRange,
    val phantom: IntRange,
    val confuser: IntRange,
    val shrines: IntRange,
    val pickups: IntRange,
) {
    val minTileY: Int get() = ((1.0f - depthMax) * WorldConstants.WORLD_HEIGHT_TILES).toInt()
    val maxTileY: Int get() = ((1.0f - depthMin) * WorldConstants.WORLD_HEIGHT_TILES).toInt()
}
