package com.glycin.koita.world

import androidx.compose.ui.geometry.Size

object WorldConstants {
    const val VIRTUAL_WIDTH = 1024f
    const val VIRTUAL_HEIGHT = 768f

    const val TILE_SIZE = 4
    const val CHUNK_SIZE = 32
    const val CHUNK_TILE_COUNT = CHUNK_SIZE * CHUNK_SIZE
    const val CHUNK_PIXEL_WIDTH = CHUNK_SIZE * TILE_SIZE
    const val CHUNK_PIXEL_HEIGHT = CHUNK_SIZE * TILE_SIZE

    const val WORLD_WIDTH_TILES = 1024 / TILE_SIZE
    const val WORLD_HEIGHT_TILES = 8_192

    const val WORLD_WIDTH_PIXELS = WORLD_WIDTH_TILES * TILE_SIZE
    const val WORLD_HEIGHT_PIXELS = WORLD_HEIGHT_TILES * TILE_SIZE

    val STANDARD_SIZE = Size(TILE_SIZE.toFloat(), TILE_SIZE.toFloat())

    const val SURFACE_Y = (WORLD_HEIGHT_TILES * 0.025f).toInt()

    const val PORTAL_WIDTH = 16
    const val PORTAL_HEIGHT = 24
    const val PORTAL_OFFSET_Y = 6
}

fun isValidTile(tileX: Int, tileY: Int): Boolean =
    tileX in 0 until WorldConstants.WORLD_WIDTH_TILES &&
        tileY in 0 until WorldConstants.WORLD_HEIGHT_TILES

fun isOutOfWorldBounds(x: Float, y: Float): Boolean =
    x < 0f || x > WorldConstants.WORLD_WIDTH_PIXELS ||
        y < 0f || y > WorldConstants.WORLD_HEIGHT_PIXELS