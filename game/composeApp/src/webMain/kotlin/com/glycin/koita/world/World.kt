package com.glycin.koita.world

import com.glycin.koita.core.Camera

class World(
    worldSizeInTiles: Int,
    worldHeightInTiles: Int,
) {
    private val chunks = HashMap<Long, Chunk>()
    val chunksWide = worldSizeInTiles / WorldConstants.CHUNK_SIZE
    val chunksHigh = worldHeightInTiles / WorldConstants.CHUNK_SIZE

    init {
        for (chunkY in 0 until chunksHigh) {
            for (chunkX in 0 until chunksWide) {
                val chunk = Chunk(chunkX, chunkY)
                chunks[packChunkCoords(chunkX, chunkY)] = chunk
            }
        }
    }

    fun getVisibleChunks(camera: Camera): List<Chunk> {
        val chunkPixelSize = WorldConstants.CHUNK_SIZE * WorldConstants.TILE_SIZE

        // Super basic Frustum Culling
        val viewLeft = camera.position.x - camera.canvasWidth / 2f
        val viewRight = camera.position.x + camera.canvasWidth / 2f
        val viewTop = camera.position.y - camera.canvasHeight / 2f
        val viewBottom = camera.position.y + camera.canvasHeight / 2f

        val startChunkX = (viewLeft / chunkPixelSize).toInt().coerceIn(0, chunksWide - 1)
        val endChunkX = (viewRight / chunkPixelSize).toInt().coerceIn(0, chunksWide - 1)
        val startChunkY = (viewTop / chunkPixelSize).toInt().coerceIn(0, chunksHigh - 1)
        val endChunkY = (viewBottom / chunkPixelSize).toInt().coerceIn(0, chunksHigh - 1)

        val visibleChunks = mutableListOf<Chunk>()
        for (chunkY in startChunkY..endChunkY) {
            for (chunkX in startChunkX..endChunkX) {
                chunks[packChunkCoords(chunkX, chunkY)]?.let { visibleChunks.add(it) }
            }
        }
        return visibleChunks
    }

    operator fun get(worldX: Int, worldY: Int): Tile {
        val chunkX = worldX / WorldConstants.CHUNK_SIZE
        val chunkY = worldY / WorldConstants.CHUNK_SIZE
        val localX = worldX % WorldConstants.CHUNK_SIZE
        val localY = worldY % WorldConstants.CHUNK_SIZE
        return getChunk(chunkX, chunkY)?.getTileAt(localX, localY) ?: Tile.AIR
    }

    operator fun set(worldX: Int, worldY: Int, tile: Tile) {
        val chunkX = worldX / WorldConstants.CHUNK_SIZE
        val chunkY = worldY / WorldConstants.CHUNK_SIZE
        val localX = worldX % WorldConstants.CHUNK_SIZE
        val localY = worldY % WorldConstants.CHUNK_SIZE
        getChunk(chunkX, chunkY)?.setTileAt(localX, localY, tile)
    }

    fun getChunk(chunkX: Int, chunkY: Int): Chunk? = chunks[packChunkCoords(chunkX, chunkY)]

    // Pack chunk coordinates into a single Long for the HashMap key
    private fun packChunkCoords(chunkX: Int, chunkY: Int): Long =
        (chunkX.toLong() shl 32) or (chunkY.toLong() and 0xFFFFFFFF)
}