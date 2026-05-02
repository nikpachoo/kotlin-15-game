package com.glycin.koita.world

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint

private val tilePaints: Array<Paint> = Array(Tile.entries.size) { i ->
    Paint().apply { color = Tile.entries[i].color }
}

private val transparentPaint: Paint = Paint().apply { color = Color.Transparent }

class Chunk(
    val xGridPos: Int,
    val yGridPos: Int,
) {
    @PublishedApi
    internal val tiles = ByteArray(WorldConstants.CHUNK_TILE_COUNT) { Tile.AIR.ordinal.toByte() }

    private var cachedBitmap: ImageBitmap? = null

    var isDirty: Boolean = true

    fun getTileAt(localX: Int, localY: Int): Tile {
        return tiles[index(localX, localY)].toTile()
    }

    fun setTileAt(localX: Int, localY: Int, tile: Tile) {
        val idx = index(localX, localY)
        val newValue = tile.ordinal.toByte()
        if(tiles[idx] != newValue) {
            tiles[idx] = newValue
            isDirty = true
        }
    }

    fun fillWith(tile: Tile) {
        val value = tile.ordinal.toByte()
        for (i in tiles.indices) {
            if (!tiles[i].toTile().isIndestructible) {
                tiles[i] = value
            }
        }
        isDirty = true
    }

    fun markClean() {
        isDirty = false
    }

    inline fun forEach(action: (localX: Int, localY: Int, tile: Tile) -> Unit) {
        for(y in 0 until WorldConstants.CHUNK_SIZE) {
            for(x in 0 until WorldConstants.CHUNK_SIZE) {
                action(x, y, tiles[index(x, y)].toTile())
            }
        }
    }

    fun getTileOrNull(localX: Int, localY: Int): Tile? {
        if (localX !in 0 until WorldConstants.CHUNK_SIZE ||
            localY !in 0 until WorldConstants.CHUNK_SIZE) {
            return null
        }
        return tiles[index(localX, localY)].toTile()
    }

    fun generateBitmap(): ImageBitmap {
        if (!isDirty) cachedBitmap?.let { return it }

        val bitmap = ImageBitmap(WorldConstants.CHUNK_PIXEL_WIDTH, WorldConstants.CHUNK_PIXEL_HEIGHT)
        val canvas = Canvas(bitmap)

        canvas.drawRect(
            left = 0f,
            top = 0f,
            right = WorldConstants.CHUNK_PIXEL_WIDTH.toFloat(),
            bottom = WorldConstants.CHUNK_PIXEL_HEIGHT.toFloat(),
            paint = transparentPaint,
        )

        forEach { localX, localY, tile ->
            if (tile == Tile.AIR) return@forEach

            canvas.drawRect(
                left = (localX * WorldConstants.TILE_SIZE).toFloat(),
                top = (localY * WorldConstants.TILE_SIZE).toFloat(),
                right = ((localX + 1) * WorldConstants.TILE_SIZE).toFloat(),
                bottom = ((localY + 1) * WorldConstants.TILE_SIZE).toFloat(),
                paint = tilePaints[tile.ordinal],
            )
        }

        cachedBitmap = bitmap
        isDirty = false

        bitmap.prepareToDraw() // Let the GPU know it has work to do!

        return bitmap
    }

    @PublishedApi
    internal fun index(localX: Int, localY: Int): Int = localY * WorldConstants.CHUNK_SIZE + localX
}