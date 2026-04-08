package com.glycin.koita.gameplay

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

class ResourceShield(
    private val player: Player,
    private val gameState: GameState,
    private val world: World,
) {
    private val shieldTiles = mutableSetOf<Long>()
    private val shieldRadius = 6
    private val innerRadius = shieldRadius - 1
    private val repairDelay = 1f // seconds of vulnerability after being hit
    private var repairTimer = 0f
    private var pendingRepairCost = 0

    fun update(deltaTime: Float) {
        if (!gameState.resourceShield) {
            clearShield()
            return
        }

        val centerTileX = (player.center.x / WorldConstants.TILE_SIZE).toInt()
        val centerTileY = (player.center.y / WorldConstants.TILE_SIZE).toInt()

        val facing = (player.dronePosition - player.center).normalized()

        val desiredTiles = mutableSetOf<Long>()
        val origin = Vec2.zero

        for (dy in -shieldRadius..shieldRadius) {
            for (dx in -shieldRadius..shieldRadius) {
                val offset = Vec2(dx.toFloat(), dy.toFloat())
                val dist = Vec2.distance(origin, offset)
                if (dist < innerRadius || dist > shieldRadius) continue

                if (offset.dot(facing) <= 0f) continue

                val tx = centerTileX + dx
                val ty = centerTileY + dy
                if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                    ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
                ) {
                    desiredTiles.add(pack(tx, ty))
                }
            }
        }

        val toRemove = mutableListOf<Long>()
        var destroyedByEnemy = 0
        for (packed in shieldTiles) {
            val (tx, ty) = unpack(packed)
            if (world[tx, ty] != Tile.SHIELD) {
                destroyedByEnemy++
                toRemove.add(packed)
            } else if (packed !in desiredTiles) {
                world[tx, ty] = Tile.AIR
                toRemove.add(packed)
            }
        }
        shieldTiles.removeAll(toRemove.toSet())

        if (destroyedByEnemy > 0) {
            pendingRepairCost += destroyedByEnemy * 100
            repairTimer = repairDelay
        }

        if (repairTimer > 0f) {
            repairTimer -= deltaTime
            return
        }

        if (pendingRepairCost > 0) {
            if (gameState.collectedStones < pendingRepairCost) return
            gameState.collectedStones -= pendingRepairCost
            pendingRepairCost = 0
        }

        for (packed in desiredTiles) {
            if (packed in shieldTiles) continue

            val (tx, ty) = unpack(packed)
            if (world[tx, ty] != Tile.AIR) continue

            world[tx, ty] = Tile.SHIELD
            shieldTiles.add(packed)
        }
    }

    private fun clearShield() {
        for (packed in shieldTiles) {
            val (tx, ty) = unpack(packed)
            if (world[tx, ty] == Tile.SHIELD) {
                world[tx, ty] = Tile.AIR
            }
        }
        shieldTiles.clear()
    }

    // Packs two tile coordinates into a single Long to avoid Pair allocations in the hot path.
    // x is stored in the upper 32 bits, y in the lower 32 bits. Same pattern as World.kt chunk keys.
    private fun pack(x: Int, y: Int): Long = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFF)

    // Extracts the (x, y) tile coordinates back out from a packed Long.
    private fun unpack(packed: Long): Pair<Int, Int> =
        Pair((packed shr 32).toInt(), (packed and 0xFFFFFFFF).toInt())
}
