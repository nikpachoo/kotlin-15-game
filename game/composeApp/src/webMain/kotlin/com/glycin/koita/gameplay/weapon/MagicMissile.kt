package com.glycin.koita.gameplay.weapon

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.util.isOutOfWorldBounds
import com.glycin.koita.util.steerToward
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.isOutOfWorldBounds

class MagicMissile(
    var position: Vec2,
    direction: Vec2,
    val baseSize: Float = 8f,
    private val onEnd: (() -> Unit),
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : Weapon {
    override var isAlive = true

    private var direction = direction.normalized()

    companion object {
        private const val BASE_SPEED = 600f
        private const val BASE_DAMAGE = 2
        private const val BASE_IMPACT_RADIUS = 70f
        private const val HOMING_RANGE = 400f
        private const val HOMING_STRENGTH = 10f
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        if (gameState.homingMissilesUnlocked) {
            val nearestEnemy = enemyManager.findNearestAliveEnemy(position, HOMING_RANGE)
            if (nearestEnemy != null) {
                steerToward(direction, position, nearestEnemy.center, HOMING_STRENGTH, deltaTime)
            }
        }

        val newPos = position + (direction * BASE_SPEED * deltaTime)

        // TODO: Can this be optimized?
        val hitEnemies = enemyManager.getEnemiesCollidingWith(position, baseSize, baseSize)
        if (hitEnemies.isNotEmpty()) {
            hitEnemies.forEach { enemy ->
                enemy.takeDamage(BASE_DAMAGE * gameState.damageMultiplier)
            }
            deactivate(explode = true)
            return
        }

        if (collisionDetector.isSolidAtPosition(newPos)) {
            deactivate(explode = true)
            return
        }

        if (newPos.isOutOfWorldBounds()) {
            deactivate(explode = false)
            return
        }

        position = newPos
    }

    private fun deactivate(explode: Boolean) {
        if (explode) explode()
        isAlive = false
        onEnd()
    }

    private fun explode() {
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
    }
}