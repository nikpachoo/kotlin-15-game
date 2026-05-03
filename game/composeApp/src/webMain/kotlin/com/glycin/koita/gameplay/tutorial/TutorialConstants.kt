package com.glycin.koita.gameplay.tutorial

import com.glycin.koita.core.Vec2
import com.glycin.koita.world.WorldConstants

object TutorialConstants {
    private const val TILE_SIZE = WorldConstants.TILE_SIZE

    const val SPAWN_TILE_X = WorldConstants.WORLD_WIDTH_TILES / 2
    const val SPAWN_TILE_Y = WorldConstants.WORLD_HEIGHT_TILES - WorldConstants.CHUNK_SIZE * 4

    const val ARENA_HALF_WIDTH_TILES = 40
    const val ARENA_FLOOR_TOP_TILE = SPAWN_TILE_Y + 6
    const val ARENA_FLOOR_BOTTOM_TILE = ARENA_FLOOR_TOP_TILE + 3
    const val ARENA_CEILING_BOTTOM_TILE = SPAWN_TILE_Y - 44
    const val ARENA_CEILING_TOP_TILE = ARENA_CEILING_BOTTOM_TILE - 1
    const val ARENA_WALL_TOP_TILE = SPAWN_TILE_Y - 64

    const val ARENA_LEFT_TILE = SPAWN_TILE_X - ARENA_HALF_WIDTH_TILES
    const val ARENA_RIGHT_TILE = SPAWN_TILE_X + ARENA_HALF_WIDTH_TILES

    val SPAWN_POSITION = Vec2(
        (SPAWN_TILE_X * TILE_SIZE).toFloat(),
        (SPAWN_TILE_Y * TILE_SIZE).toFloat(),
    )

    val ORE_LEFT_RANGE: IntRange = (SPAWN_TILE_X - 8)..(SPAWN_TILE_X - 6)
    val ORE_RIGHT_RANGE: IntRange = (SPAWN_TILE_X + 6)..(SPAWN_TILE_X + 8)
    const val ORE_TILE_Y = ARENA_FLOOR_TOP_TILE

    const val DETECT_LEFT_TILE = ARENA_LEFT_TILE + 2
    const val DETECT_RIGHT_TILE = ARENA_RIGHT_TILE - 2
    const val FLOOR_DETECT_TOP_TILE = ARENA_FLOOR_TOP_TILE
    const val FLOOR_DETECT_BOTTOM_TILE = ARENA_FLOOR_BOTTOM_TILE
    const val BUILD_PAD_TOP_TILE = ARENA_CEILING_BOTTOM_TILE + 1
    const val BUILD_PAD_BOTTOM_TILE = ARENA_FLOOR_TOP_TILE - 1

    const val MOVE_THRESHOLD_PX = 48f
    const val JUMP_THRESHOLD_PX = 36f
}
