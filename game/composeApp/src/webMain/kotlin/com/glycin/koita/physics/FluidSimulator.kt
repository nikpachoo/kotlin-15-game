package com.glycin.koita.physics

import com.glycin.koita.core.Camera
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.random.Random

class FluidSimulator(
    private val world: World,
    private val camera: Camera,
) {
    private val initialFluidCapacity = 1024

    private val allFluids = mutableSetOf<Long>()
    private var updateCount = 0L

    private var fluidsToUpdate = LongArray(initialFluidCapacity)
    private var fluidsToUpdateCount = 0

    init {
        for (y in 0 until WorldConstants.WORLD_HEIGHT_TILES) {
            for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                val tile = world[x, y]
                if (tile == Tile.WATER || tile == Tile.LAVA) {
                    allFluids.add(pack(x, y))
                }
            }
        }
    }

    fun registerFluid(x: Int, y: Int) {
        allFluids.add(pack(x, y))
    }

    fun update() {
        updateCount++
        if (updateCount % 2 == 0L) return

        val bufferTiles = 10 * WorldConstants.CHUNK_SIZE

        val minTileX = ((camera.position.x - camera.canvasWidth / 2) / WorldConstants.TILE_SIZE).toInt() - bufferTiles
        val maxTileX = ((camera.position.x + camera.canvasWidth / 2) / WorldConstants.TILE_SIZE).toInt() + bufferTiles
        val minTileY = ((camera.position.y - camera.canvasHeight / 2) / WorldConstants.TILE_SIZE).toInt() - bufferTiles
        val maxTileY = ((camera.position.y + camera.canvasHeight / 2) / WorldConstants.TILE_SIZE).toInt() + bufferTiles

        val startX = minTileX.coerceIn(0, WorldConstants.WORLD_WIDTH_TILES - 1)
        val endX = maxTileX.coerceIn(0, WorldConstants.WORLD_WIDTH_TILES - 1)
        val startY = minTileY.coerceIn(0, WorldConstants.WORLD_HEIGHT_TILES - 1)
        val endY = maxTileY.coerceIn(0, WorldConstants.WORLD_HEIGHT_TILES - 1)

        fluidsToUpdateCount = 0
        for (key in allFluids) {
            val x = unpackX(key)
            val y = unpackY(key)
            if (x in startX..endX && y in startY..endY) {
                ensureCapacity()
                fluidsToUpdate[fluidsToUpdateCount++] = key
            }
        }

        // Y is in the high bits, so sorting Longs ascending == sorting by Y ascending.
        // Iterating backward gives Y-descending traversal (process bottom rows first).
        fluidsToUpdate.sort(0, fluidsToUpdateCount)

        for (i in fluidsToUpdateCount - 1 downTo 0) {
            val key = fluidsToUpdate[i]
            val x = unpackX(key)
            val y = unpackY(key)
            val tile = world[x, y]
            if (tile != Tile.WATER && tile != Tile.LAVA) {
                allFluids.remove(key)
                continue
            }
            updateFluid(x, y, tile)
        }
    }

    private fun ensureCapacity() {
        if (fluidsToUpdateCount >= fluidsToUpdate.size) {
            fluidsToUpdate = fluidsToUpdate.copyOf(fluidsToUpdate.size * 2)
        }
    }

    private fun pack(x: Int, y: Int): Long =
        (y.toLong() shl 32) or (x.toLong() and 0xFFFFFFFFL)

    private fun unpackX(key: Long): Int = key.toInt()
    private fun unpackY(key: Long): Int = (key shr 32).toInt()

    private fun oppositeFluid(fluid: Tile): Tile = if (fluid == Tile.WATER) Tile.LAVA else Tile.WATER

    private fun isOppositeFluid(fluid: Tile, targetX: Int, targetY: Int): Boolean {
        return world[targetX, targetY] == oppositeFluid(fluid)
    }

    //When water meets lava (or vice versa), both tiles become stone.
    private fun solidify(x: Int, y: Int, targetX: Int, targetY: Int) {
        world[x, y] = Tile.STONE
        world[targetX, targetY] = Tile.STONE
        allFluids.remove(pack(x, y))
        allFluids.remove(pack(targetX, targetY))
    }

    private fun moveFluid(fromKey: Long, fromX: Int, fromY: Int, toX: Int, toY: Int, fluid: Tile) {
        world[toX, toY] = fluid
        world[fromX, fromY] = Tile.AIR
        allFluids.remove(fromKey)
        allFluids.add(pack(toX, toY))
    }

    private fun updateFluid(x: Int, y: Int, fluid: Tile) {
        val sourceKey = pack(x, y)

        if (y < WorldConstants.WORLD_HEIGHT_TILES - 1) {
            val below = world[x, y + 1]
            if (below == Tile.AIR) {
                moveFluid(sourceKey, x, y, x, y + 1, fluid)
                return
            }
            if (isOppositeFluid(fluid, x, y + 1)) {
                solidify(x, y, x, y + 1)
                return
            }
        }

        val canGoDiagLeft = x > 0 && y < WorldConstants.WORLD_HEIGHT_TILES - 1 &&
                world[x - 1, y + 1] == Tile.AIR
        val canGoDiagRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 &&
                y < WorldConstants.WORLD_HEIGHT_TILES - 1 &&
                world[x + 1, y + 1] == Tile.AIR
        val lavaContactDiagLeft = x > 0 && y < WorldConstants.WORLD_HEIGHT_TILES - 1 &&
                isOppositeFluid(fluid, x - 1, y + 1)
        val lavaContactDiagRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 &&
                y < WorldConstants.WORLD_HEIGHT_TILES - 1 &&
                isOppositeFluid(fluid, x + 1, y + 1)

        if (lavaContactDiagLeft || lavaContactDiagRight) {
            val targetX = if (lavaContactDiagLeft && lavaContactDiagRight) {
                if (Random.nextBoolean()) x - 1 else x + 1
            } else if (lavaContactDiagLeft) x - 1 else x + 1
            solidify(x, y, targetX, y + 1)
            return
        }

        if (canGoDiagLeft && canGoDiagRight) {
            val newX = if (Random.nextBoolean()) x - 1 else x + 1
            moveFluid(sourceKey, x, y, newX, y + 1, fluid)
            return
        } else if (canGoDiagLeft) {
            moveFluid(sourceKey, x, y, x - 1, y + 1, fluid)
            return
        } else if (canGoDiagRight) {
            moveFluid(sourceKey, x, y, x + 1, y + 1, fluid)
            return
        }

        val canGoLeft = x > 0 && world[x - 1, y] == Tile.AIR
        val canGoRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 && world[x + 1, y] == Tile.AIR
        val lavaContactLeft = x > 0 && isOppositeFluid(fluid, x - 1, y)
        val lavaContactRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 && isOppositeFluid(fluid, x + 1, y)

        if (lavaContactLeft || lavaContactRight) {
            val targetX = if (lavaContactLeft && lavaContactRight) {
                if (Random.nextBoolean()) x - 1 else x + 1
            } else if (lavaContactLeft) x - 1 else x + 1
            solidify(x, y, targetX, y)
            return
        }

        when {
            canGoLeft && canGoRight -> {
                val newX = if (Random.nextBoolean()) x - 1 else x + 1
                moveFluid(sourceKey, x, y, newX, y, fluid)
            }
            canGoLeft -> moveFluid(sourceKey, x, y, x - 1, y, fluid)
            canGoRight -> moveFluid(sourceKey, x, y, x + 1, y, fluid)
        }
    }
}
