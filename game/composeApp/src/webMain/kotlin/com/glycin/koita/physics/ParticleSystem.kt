package com.glycin.koita.physics

import com.glycin.koita.core.Vec2
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.isOutOfWorldBounds
import com.glycin.koita.world.isValidTile
import com.glycin.koita.world.WorldConstants

class ParticleSystem(
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val fluidSimulator: FluidSimulator,
) {
    private val positions = FloatArray(PhysicsConstants.MAX_PARTICLES * 2) // Stored as X,Y pairs
    private val velocities = FloatArray(PhysicsConstants.MAX_PARTICLES * 2) // Stored as X,Y pairs
    private val tiles = IntArray(PhysicsConstants.MAX_PARTICLES)
    private val lifetimes = FloatArray(PhysicsConstants.MAX_PARTICLES)
    private val active = BooleanArray(PhysicsConstants.MAX_PARTICLES)

    private var particleCount = 0

    fun addParticle(position: Vec2, velocity: Vec2, tile: Tile) {
        if (particleCount >= PhysicsConstants.MAX_PARTICLES) return

        var index = -1
        for (i in 0 until PhysicsConstants.MAX_PARTICLES) {
            if (!active[i]) {
                index = i
                break
            }
        }

        if (index == -1) return

        val i2 = index * 2
        positions[i2] = position.x + WorldConstants.TILE_SIZE / 2f
        positions[i2 + 1] = position.y + WorldConstants.TILE_SIZE / 2f
        velocities[i2] = velocity.x
        velocities[i2 + 1] = velocity.y
        tiles[index] = tile.ordinal
        lifetimes[index] = 3f
        active[index] = true
        particleCount++
    }

    fun update(deltaTime: Float) {
        for (i in 0 until PhysicsConstants.MAX_PARTICLES) {
            if (!active[i]) continue

            val i2 = i * 2

            lifetimes[i] -= deltaTime
            if (lifetimes[i] <= 0f) {
                active[i] = false
                particleCount--
                continue
            }

            velocities[i2 + 1] += PhysicsConstants.GRAVITY * deltaTime

            val newX = positions[i2] + velocities[i2] * deltaTime
            val newY = positions[i2 + 1] + velocities[i2 + 1] * deltaTime

            if (collisionDetector.isSolidAtPosition(Vec2(newX, newY))) {
                val settleTileX = (newX / WorldConstants.TILE_SIZE).toInt()
                val settleTileY = (newY / WorldConstants.TILE_SIZE).toInt()

                val settleCandidates = listOf(
                    Pair(settleTileX, settleTileY - 1),  // Above
                    Pair(settleTileX - 1, settleTileY),  // Left
                    Pair(settleTileX + 1, settleTileY),  // Right
                    Pair(settleTileX, settleTileY)       // Current
                )

                for ((tx, ty) in settleCandidates) {
                    if (isValidTile(tx, ty) && world[tx, ty] == Tile.AIR) {

                        val tile = Tile.entries[tiles[i]]
                        world[tx, ty] = tile
                        if (tile.isLiquid) {
                            fluidSimulator.registerFluid(tx, ty)
                        }
                        break
                    }
                }

                active[i] = false
                particleCount--
                continue
            }

            if (isOutOfWorldBounds(newX, newY)) {
                active[i] = false
                particleCount--
                continue
            }

            // No collision, move to new position
            positions[i2] = newX
            positions[i2 + 1] = newY
        }
    }

    fun forEachActive(action: (x: Float, y: Float, tile: Tile) -> Unit) {
        for (i in 0 until PhysicsConstants.MAX_PARTICLES) {
            if (!active[i]) continue

            val tileSize = WorldConstants.TILE_SIZE.toFloat()
            val i2 = i * 2
            // Snap to grid when rendering for cleaner look
            val tileX = ((positions[i2] - tileSize / 2f) / tileSize).toInt()
            val tileY = ((positions[i2 + 1] - tileSize / 2f) / tileSize).toInt()

            action(
                tileX * tileSize,
                tileY * tileSize,
                Tile.entries[tiles[i]]
            )
        }
    }

    fun clear() {
        active.fill(false)
        particleCount = 0
    }
}