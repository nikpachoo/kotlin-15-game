package com.glycin.koita.gameplay.weapon

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.util.isOutOfWorldBounds
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.isOutOfWorldBounds
import com.glycin.koita.world.isValidTile
import com.glycin.koita.world.WorldConstants
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SuperSoaker(
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val fluidSimulator: FluidSimulator,
    private val enemyManager: EnemyManager,
) : Weapon {

    var isActive = false
    override val isAlive: Boolean get() = isActive || droplets.isNotEmpty()
    override val bossShieldDamage: Int = 1

    val droplets = mutableListOf<WaterDroplet>()

    private var spawnTimer = 0f
    private val spawnInterval = 1f / 15f
    private val maxDroplets = 40
    private val dropletSpeed = 350f
    private val dropletSize = 4f
    private val spreadRadians = 0.15f
    private val damagePerHit = 0.15f
    private val maxLifetime = 0.75f

    data class WaterDroplet(
        var position: Vec2,
        val velocity: Vec2,
        var alive: Boolean = true,
        var age: Float = 0f,
    )

    fun update(deltaTime: Float, origin: Vec2, target: Vec2) {
        if (isActive) {
            spawnTimer += deltaTime
            while (spawnTimer >= spawnInterval && droplets.size < maxDroplets) {
                spawnTimer -= spawnInterval
                val baseDir = (target - origin).normalized()
                val baseAngle = atan2(baseDir.y, baseDir.x)
                val angle = baseAngle + Random.nextFloat() * spreadRadians * 2f - spreadRadians
                val dir = Vec2(cos(angle), sin(angle))

                droplets.add(
                    WaterDroplet(
                        position = origin.copy(),
                        velocity = dir * dropletSpeed,
                    )
                )
            }
        } else {
            spawnTimer = 0f
        }

        for (droplet in droplets) {
            if (!droplet.alive) continue

            droplet.age += deltaTime

            if (droplet.age >= maxLifetime) {
                placeWater(droplet.position)
                droplet.alive = false
                continue
            }

            val newPos = droplet.position + droplet.velocity * deltaTime

            if (newPos.isOutOfWorldBounds()) {
                droplet.alive = false
                continue
            }

            val hitEnemies = enemyManager.getEnemiesCollidingWith(newPos, dropletSize, dropletSize)
            if (hitEnemies.isNotEmpty()) {
                hitEnemies.forEach { enemy ->
                    enemy.takeDamage(damagePerHit * gameState.damageMultiplier)
                }
                placeWater(newPos)
                droplet.alive = false
                continue
            }

            if (collisionDetector.isSolidAtPosition(newPos)) {
                placeWater(droplet.position)
                droplet.alive = false
                continue
            }

            droplet.position = newPos
        }

        droplets.removeAll { !it.alive }
    }

    private fun placeWater(position: Vec2) {
        val tileX = (position.x / WorldConstants.TILE_SIZE).toInt()
        val tileY = (position.y / WorldConstants.TILE_SIZE).toInt()

        if (!isValidTile(tileX, tileY)) return

        if (world[tileX, tileY] == Tile.AIR) {
            world[tileX, tileY] = Tile.WATER
            fluidSimulator.registerFluid(tileX, tileY)
        }
    }
}
