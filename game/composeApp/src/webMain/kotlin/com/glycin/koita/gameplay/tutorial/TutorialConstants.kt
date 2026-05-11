package com.glycin.koita.gameplay.tutorial

import com.glycin.koita.core.Vec2
import com.glycin.koita.world.WorldConstants

object TutorialConstants {
    private const val TILE_SIZE = WorldConstants.TILE_SIZE

    const val SPAWN_TILE_X = WorldConstants.WORLD_WIDTH_TILES / 2
    const val SPAWN_TILE_Y = WorldConstants.WORLD_HEIGHT_TILES - WorldConstants.CHUNK_SIZE * 4

    val SPAWN_POSITION = Vec2(
        (SPAWN_TILE_X * TILE_SIZE).toFloat(),
        (SPAWN_TILE_Y * TILE_SIZE).toFloat(),
    )

    const val MOVE_THRESHOLD_PX = 48f
    const val JUMP_THRESHOLD_PX = 36f
    const val DIG_UP_THRESHOLD_PX = TILE_SIZE * 60f
}
