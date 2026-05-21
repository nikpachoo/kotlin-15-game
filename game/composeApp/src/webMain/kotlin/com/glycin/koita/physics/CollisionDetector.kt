package com.glycin.koita.physics

import com.glycin.koita.core.Vec2
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

class CollisionDetector(
    private val world: World,
) {
    // Portal bounds in pixels
    var portalX = 0f
    var portalY = 0f
    var portalWidth = 0f
    var portalHeight = 0f
    var portalDestY = 0f

    // Reusable arrays to avoid allocations in the hot path
    // [0]=minX, [1]=maxX, [2]=minY, [3]=maxY
    private val boundsA = IntArray(4)
    private val boundsB = IntArray(4)

    fun checkAABB(position: Vec2, width: Float, height: Float): Boolean {
        fillBounds(boundsA, position, width, height)
        return checkBounds(boundsA, ignore = null)
    }

    fun checkAABB(position: Vec2, width: Float, height: Float, currentPosition: Vec2): Boolean {
        fillBounds(boundsA, position, width, height)
        fillBounds(boundsB, currentPosition, width, height)
        return checkBounds(boundsA, ignore = boundsB) //TODO: This ignore check is abit meh how to solve the player getting stuck?
    }

    private fun checkBounds(bounds: IntArray, ignore: IntArray?): Boolean {
        for (tileY in bounds[2]..bounds[3]) {
            for (tileX in bounds[0]..bounds[1]) {
                if (tileX < 0 || tileX >= WorldConstants.WORLD_WIDTH_TILES ||
                    tileY < 0 || tileY >= WorldConstants.WORLD_HEIGHT_TILES) {
                    return true
                }

                if (world[tileX, tileY].isSolid) {
                    if (ignore != null &&
                        tileX in ignore[0]..ignore[1] &&
                        tileY in ignore[2]..ignore[3]) continue
                    return true
                }
            }
        }

        return false
    }

    private fun fillBounds(out: IntArray, position: Vec2, width: Float, height: Float) {
        val tileSize = WorldConstants.TILE_SIZE
        out[0] = (position.x / tileSize).toInt()
        out[1] = ((position.x + width) / tileSize).toInt()
        out[2] = (position.y / tileSize).toInt()
        out[3] = ((position.y + height) / tileSize).toInt()
    }

    fun checkAABBOverlap(
        posA: Vec2, widthA: Float, heightA: Float,
        posB: Vec2, widthB: Float, heightB: Float,
    ): Boolean {
        return posA.x < posB.x + widthB &&
                posA.x + widthA > posB.x &&
                posA.y < posB.y + heightB &&
                posA.y + heightA > posB.y
    }

    fun isSolidAtPosition(position: Vec2): Boolean =
        isSolidAtPosition(position.x, position.y)

    fun isSolidAtPosition(x: Float, y: Float): Boolean {
        val tileX = (x / WorldConstants.TILE_SIZE).toInt()
        val tileY = (y / WorldConstants.TILE_SIZE).toInt()

        if (isOutOfWorldBounds(tileX, tileY)) return true

        return world[tileX, tileY].isSolid
    }

    fun getTilesInRadius(center: Vec2, radius: Float): List<Pair<Int, Int>> {
        val tiles = mutableListOf<Pair<Int, Int>>()
        val tileSize = WorldConstants.TILE_SIZE
        val radiusInTiles = (radius / tileSize).toInt() + 1

        val centerTileX = (center.x / tileSize).toInt()
        val centerTileY = (center.y / tileSize).toInt()

        val radiusSquared = radius * radius

        for (dy in -radiusInTiles..radiusInTiles) {
            for (dx in -radiusInTiles..radiusInTiles) {
                val tileX = centerTileX + dx
                val tileY = centerTileY + dy

                if (tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                    tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES) continue

                val tileCenterX = tileX * tileSize + tileSize / 2f
                val tileCenterY = tileY * tileSize + tileSize / 2f

                val distXSquared = (tileCenterX - center.x) * (tileCenterX - center.x)
                val distYSquared = (tileCenterY - center.y) * (tileCenterY - center.y)

                if (distXSquared + distYSquared <= radiusSquared) {
                    tiles.add(Pair(tileX, tileY))
                }
            }
        }

        return tiles
    }

    fun raycast(origin: Vec2, target: Vec2, maxRayRange: Int): Vec2 {
        val dir = (target - origin).normalized()
        val step = WorldConstants.TILE_SIZE.toFloat() / 2f
        val maxSteps = (maxRayRange / step).toInt()

        var current = origin
        for (i in 0..maxSteps) {
            val next = origin + dir * (step * i)
            if (isSolidAtPosition(next)) {
                return current
            }
            current = next
        }
        return current
    }

    private fun isOutOfWorldBounds(tileX: Int, tileY: Int): Boolean {
        return (tileX < 0 || tileX >= WorldConstants.WORLD_WIDTH_TILES ||
            tileY < 0 || tileY >= WorldConstants.WORLD_HEIGHT_TILES)
    }
}