package com.glycin.koita.gameplay.weapon

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.Enemy
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World

class Laser(
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : Weapon {
    var start = Vec2.zero()
    var end = Vec2.zero()
    var isActive = false

    override val isAlive: Boolean get() = isActive
    override val bossShieldDamage: Int = 1

    var didDamageTick: Boolean = false
        private set

    private var terrainDamageTimer = 0f
    private var damageCooldown = 0f
    private val maxRange: Int = 500
    private val damagePerSecond: Float = 0.25f
    private val terrainDamageInterval: Float = 0.15f
    private val impactRadius: Float = 20f

    fun update(deltaTime: Float, origin: Vec2, target: Vec2) {
        start = origin
        end = collisionDetector.raycast(origin, target, maxRange)
        didDamageTick = false

        if (damageCooldown > 0f) damageCooldown -= deltaTime

        if (damageCooldown <= 0f) {
            didDamageTick = true
            val dir = (end - start).normalized()
            val length = Vec2.distance(start, end)
            val step = 8f
            var dist = 0f
            val hitEnemies = mutableSetOf<Enemy>()

            // TODO: Collide and stop when hitting enemies
            while (dist < length) {
                val point = start + dir * dist
                val enemies = enemyManager.getEnemiesCollidingWith(point, step, step)
                enemies.forEach { enemy ->
                    if (hitEnemies.add(enemy)) {
                        enemy.takeDamage(damagePerSecond * gameState.damageMultiplier)
                    }
                }
                dist += step
            }
            damageCooldown = 0.1f
        }

        terrainDamageTimer -= deltaTime
        if (terrainDamageTimer <= 0f) {
            terrainDamageTimer = terrainDamageInterval
            destroyTerrainAt(end)
        }
    }

    private fun destroyTerrainAt(position: Vec2) {
        val dynamiteCount = if (gameState.explosiveBlocks) {
            collisionDetector.getTilesInRadius(position, impactRadius).count { (tileX, tileY) ->
                world[tileX, tileY] == Tile.DYNAMITE
            }
        } else {
            0
        }

        val impactRadiusMultiplier = dynamiteCount / 5
        val effectiveRadius = if (dynamiteCount > 0) impactRadius * impactRadiusMultiplier else impactRadius

        val affectedTiles = collisionDetector.getTilesInRadius(position, effectiveRadius)
        explodeTerrain(affectedTiles, position, effectiveRadius, world, particleSystem)
    }
}
