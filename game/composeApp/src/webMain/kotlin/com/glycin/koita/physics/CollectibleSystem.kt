package com.glycin.koita.physics

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.physics.PhysicsConstants.MAX_COLLECTIBLES
import com.glycin.koita.world.Tile
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.random.nextInt

// Similar to particle system, but focuses on moving tiles and other objects towards the player
class CollectibleSystem(
    private val gameState: GameState,
    private val maxCollectibles: Int = MAX_COLLECTIBLES,
) {
    private val positions = FloatArray(maxCollectibles * 2) // X,Y pairs
    private val tiles = IntArray(maxCollectibles)
    private val active = BooleanArray(maxCollectibles)

    private var collectibleCount = 0
    private val baseSpeed = 150f
    private val accelerationFactor = 2f
    private val collectDistance = 16f

    fun spawn(x: Float, y: Float, tile: Tile) {
        if (collectibleCount >= maxCollectibles) return

        var index = -1
        for (i in 0 until maxCollectibles) {
            if (!active[i]) {
                index = i
                break
            }
        }

        if (index == -1) return

        val i2 = index * 2
        positions[i2] = x
        positions[i2 + 1] = y
        tiles[index] = tile.ordinal
        active[index] = true
        collectibleCount++
    }

    fun update(deltaTime: Float, playerCenter: Vec2) {
        for (i in 0 until maxCollectibles) {
            if (!active[i]) continue

            val i2 = i * 2
            val x = positions[i2]
            val y = positions[i2 + 1]

            val dx = playerCenter.x - x
            val dy = playerCenter.y - y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance < collectDistance) {
                active[i] = false
                collectibleCount--
                onCollected(Tile.entries[tiles[i]])
                continue
            }

            val dirX = dx / distance
            val dirY = dy / distance

            val currentSpeed = baseSpeed + (1f / (distance / 100f + 1f)) * baseSpeed * accelerationFactor

            positions[i2] = x + dirX * currentSpeed * deltaTime
            positions[i2 + 1] = y + dirY * currentSpeed * deltaTime
        }
    }

    fun forEachActive(action: (x: Float, y: Float, tile: Tile) -> Unit) {
        for (i in 0 until maxCollectibles) {
            if (!active[i]) continue

            val i2 = i * 2
            action(
                positions[i2],
                positions[i2 + 1],
                Tile.entries[tiles[i]]
            )
        }
    }

    private fun onCollected(tile: Tile) {
        if(Random.nextInt(0..100) < 50) {
            gameState.collectedStones++
        }
        gameState.score += tile.ordinal
    }

    fun clear() {
        active.fill(false)
        collectibleCount = 0
    }
}