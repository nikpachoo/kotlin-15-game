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
    private val allFluids = mutableSetOf<Pair<Int, Int>>()
    private var updateCount = 0L // Used to update at half the framerate

    init {
        allFluids.clear()
        for (y in 0 until WorldConstants.WORLD_HEIGHT_TILES) {
            for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                val tile = world[x, y]
                if (tile == Tile.WATER || tile == Tile.LAVA) {
                    allFluids.add(Pair(x, y))
                }
            }
        }
    }

    fun registerFluid(x: Int, y: Int) {
        allFluids.add(Pair(x, y))
    }

    fun update() {
        updateCount++
        if(updateCount % 2 == 0L) return

        val bufferChunks = 10

        //TODO: Maybe make a Camera.getFrustum function
        val minTileX = ((camera.position.x - camera.canvasWidth / 2) / WorldConstants.TILE_SIZE).toInt() - (bufferChunks * WorldConstants.CHUNK_SIZE)
        val maxTileX = ((camera.position.x + camera.canvasWidth / 2) / WorldConstants.TILE_SIZE).toInt() + (bufferChunks * WorldConstants.CHUNK_SIZE)
        val minTileY = ((camera.position.y - camera.canvasHeight / 2) / WorldConstants.TILE_SIZE).toInt() - (bufferChunks * WorldConstants.CHUNK_SIZE)
        val maxTileY = ((camera.position.y + camera.canvasHeight / 2) / WorldConstants.TILE_SIZE).toInt() + (bufferChunks * WorldConstants.CHUNK_SIZE)

        val startX = minTileX.coerceIn(0, WorldConstants.WORLD_WIDTH_TILES - 1)
        val endX = maxTileX.coerceIn(0, WorldConstants.WORLD_WIDTH_TILES - 1)
        val startY = minTileY.coerceIn(0, WorldConstants.WORLD_HEIGHT_TILES - 1)
        val endY = maxTileY.coerceIn(0, WorldConstants.WORLD_HEIGHT_TILES - 1)

        val fluidsToUpdate = mutableListOf<Pair<Int, Int>>()

        for ((x, y) in allFluids) {
            if (x in startX..endX && y in startY..endY) {
                fluidsToUpdate.add(Pair(x, y))
            }
        }

        fluidsToUpdate.sortByDescending { it.second }

        for ((x, y) in fluidsToUpdate) {
            val tile = world[x, y]
            if (tile != Tile.WATER && tile != Tile.LAVA) {
                allFluids.remove(Pair(x, y))
                continue
            }

            updateFluid(x, y, tile)
        }
    }

    private fun oppositeFluid(fluid: Tile): Tile = if (fluid == Tile.WATER) Tile.LAVA else Tile.WATER

    private fun isOppositeFluid(fluid: Tile, targetX: Int, targetY: Int): Boolean {
        return world[targetX, targetY] == oppositeFluid(fluid)
    }

    //When water meets lava (or vice versa), both tiles become stone.
    private fun solidify(x: Int, y: Int, targetX: Int, targetY: Int) {
        world[x, y] = Tile.STONE
        world[targetX, targetY] = Tile.STONE
        allFluids.remove(Pair(x, y))
        allFluids.remove(Pair(targetX, targetY))
    }

    private fun updateFluid(x: Int, y: Int, fluid: Tile) {
        if (y < WorldConstants.WORLD_HEIGHT_TILES - 1) {
            val below = world[x, y + 1]
            if (below == Tile.AIR) {
                world[x, y + 1] = fluid
                world[x, y] = Tile.AIR
                allFluids.remove(Pair(x, y))
                allFluids.add(Pair(x, y + 1))
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
                if (Random.nextDouble(1.0) < 0.5) x - 1 else x + 1
            } else if (lavaContactDiagLeft) x - 1 else x + 1
            solidify(x, y, targetX, y + 1)
            return
        }

        if (canGoDiagLeft && canGoDiagRight) {
            val newX = if (Random.nextDouble(1.0) < 0.5) x - 1 else x + 1
            world[newX, y + 1] = fluid
            world[x, y] = Tile.AIR
            allFluids.remove(Pair(x, y))
            allFluids.add(Pair(newX, y + 1))
            return
        } else if (canGoDiagLeft) {
            world[x - 1, y + 1] = fluid
            world[x, y] = Tile.AIR
            allFluids.remove(Pair(x, y))
            allFluids.add(Pair(x - 1, y + 1))
            return
        } else if (canGoDiagRight) {
            world[x + 1, y + 1] = fluid
            world[x, y] = Tile.AIR
            allFluids.remove(Pair(x, y))
            allFluids.add(Pair(x + 1, y + 1))
            return
        }

        val canGoLeft = x > 0 && world[x - 1, y] == Tile.AIR
        val canGoRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 && world[x + 1, y] == Tile.AIR
        val lavaContactLeft = x > 0 && isOppositeFluid(fluid, x - 1, y)
        val lavaContactRight = x < WorldConstants.WORLD_WIDTH_TILES - 1 && isOppositeFluid(fluid, x + 1, y)

        if (lavaContactLeft || lavaContactRight) {
            val targetX = if (lavaContactLeft && lavaContactRight) {
                if (Random.nextDouble(1.0) < 0.5) x - 1 else x + 1
            } else if (lavaContactLeft) x - 1 else x + 1
            solidify(x, y, targetX, y)
            return
        }

        when {
            canGoLeft && canGoRight -> {
                val newX = if (Random.nextDouble(1.0) < 0.5) x - 1 else x + 1
                world[newX, y] = fluid
                world[x, y] = Tile.AIR
                allFluids.remove(Pair(x, y))
                allFluids.add(Pair(newX, y))
            }
            canGoLeft -> {
                world[x - 1, y] = fluid
                world[x, y] = Tile.AIR
                allFluids.remove(Pair(x, y))
                allFluids.add(Pair(x - 1, y))
            }
            canGoRight -> {
                world[x + 1, y] = fluid
                world[x, y] = Tile.AIR
                allFluids.remove(Pair(x, y))
                allFluids.add(Pair(x + 1, y))
            }
        }
    }
}