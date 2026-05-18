package com.glycin.koita.gameplay.weapon

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.Enemy
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.util.isOutOfWorldBounds
import com.glycin.koita.util.lerp
import com.glycin.koita.util.steerToward
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World

class Rocket(
    var position: Vec2,
    direction: Vec2,
    private val onEnd: (() -> Unit),
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : Weapon {
    override var isAlive = true
    override val bossShieldDamage: Int = 4

    val direction = direction.normalized()
    private var flightTime = 0f

    companion object {
        private const val INITIAL_SPEED = 10f
        private const val MAX_SPEED = 1200f
        private const val ACCELERATION_TIME = 1f
        private const val BASE_DAMAGE = 8
        private const val EXPLOSION_DAMAGE = 4f
        private const val BASE_IMPACT_RADIUS = 140f
        const val BASE_SIZE = 12f
        private const val HOMING_RANGE = 400f
        private const val HOMING_STRENGTH = 10f
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        flightTime += deltaTime

        if (gameState.homingMissilesUnlocked) {
            val nearestEnemy = enemyManager.findNearestAliveEnemy(position, HOMING_RANGE)
            if (nearestEnemy != null) {
                steerToward(direction, position, nearestEnemy.center, HOMING_STRENGTH, deltaTime)
            }
        }

        val t = (flightTime / ACCELERATION_TIME).coerceAtMost(1f)
        val easeIn = t * t * t
        val speed = INITIAL_SPEED.lerp(MAX_SPEED, easeIn)
        val newPos = position + (direction * speed * deltaTime)

        val hitEnemies = enemyManager.getEnemiesCollidingWith(position, BASE_SIZE, BASE_SIZE)
        if (hitEnemies.isNotEmpty()) {
            hitEnemies.forEach { enemy ->
                enemy.takeDamage(BASE_DAMAGE * gameState.damageMultiplier)
            }
            deactivate(explode = true, excludeFromSplash = hitEnemies)
            return
        }

        if (collisionDetector.isSolidAtPosition(newPos)) {
            position = newPos
            deactivate(explode = true)
            return
        }

        if (newPos.isOutOfWorldBounds()) {
            deactivate(explode = false)
            return
        }

        position = newPos
    }

    fun detonate() {
        deactivate(explode = true)
    }

    private fun deactivate(explode: Boolean, excludeFromSplash: Collection<Enemy>? = null) {
        if (explode) explode(excludeFromSplash)
        isAlive = false
        onEnd()
    }

    private fun explode(excludeFromSplash: Collection<Enemy>? = null) {
        val dynamiteCount = if (gameState.explosiveBlocks) {
            collisionDetector.getTilesInRadius(position, BASE_IMPACT_RADIUS).count { (tileX, tileY) ->
                world[tileX, tileY] == Tile.DYNAMITE
            }
        } else {
            0
        }

        val impactRadiusMultiplier = dynamiteCount / 10
        val impactRadius = if (dynamiteCount > 0) BASE_IMPACT_RADIUS * impactRadiusMultiplier else BASE_IMPACT_RADIUS

        val affectedTiles = collisionDetector.getTilesInRadius(position, impactRadius)
        SoundManager.playOneShot(Sounds.EXPLODE)
        explodeTerrain(affectedTiles, position, impactRadius, world, particleSystem)
        enemyManager.destroyShieldsInRadius(position, impactRadius, bossShieldDamage)
        enemyManager.damageInRange(position, impactRadius, EXPLOSION_DAMAGE * gameState.damageMultiplier, bossShieldDamage, excludeFromSplash)
    }
}
