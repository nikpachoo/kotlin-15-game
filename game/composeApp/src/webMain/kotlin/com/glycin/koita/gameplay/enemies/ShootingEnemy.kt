package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

abstract class ShootingEnemy(
    position: Vec2,
    width: Float,
    height: Float,
    drawWidth: Float = width,
    drawHeight: Float = height,
    health: Float,
    collisionDetector: CollisionDetector,
    world: World,
    protected val particleSystem: ParticleSystem,
) : Enemy(
    position = position,
    width = width,
    height = height,
    drawWidth = drawWidth,
    drawHeight = drawHeight,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    private val _missiles = mutableListOf<EnemyMissile>()
    val activeMissiles: List<EnemyMissile> get() = _missiles

    protected open val shootCooldownMin = 2f
    protected open val shootCooldownMax = 5f
    protected open val spreadRadians = 0.26f

    private var shootCooldown = randomCooldown()

    fun tickAttack(deltaTime: Float, playerPos: Vec2) {
        if (enemyState == EnemyState.HURT || enemyState == EnemyState.ATTACK || enemyState == EnemyState.DEATH) return
        shootCooldown -= deltaTime
        if (shootCooldown <= 0f) {
            startAttack()
            shootMissile(playerPos)
            shootCooldown = randomCooldown()
        }
    }

    fun tickMissiles(deltaTime: Float) {
        if (_missiles.isEmpty()) return
        for (i in 0..<_missiles.size) _missiles[i].update(deltaTime)
        for (i in _missiles.size - 1 downTo 0) {
            if (!_missiles[i].isAlive) _missiles.removeAt(i)
        }
    }

    private fun shootMissile(playerPos: Vec2) {
        val direction = (playerPos - center).normalized()
        val angleError = (Random.nextFloat() * 2f - 1f) * spreadRadians
        val angle = atan2(direction.y, direction.x) + angleError
        _missiles.add(
            EnemyMissile(
                position = center.copy(),
                direction = Vec2(cos(angle), sin(angle)),
                collisionDetector = collisionDetector,
                world = world,
                particleSystem = particleSystem,
            )
        )
    }

    private fun randomCooldown() = Random.nextFloat() * (shootCooldownMax - shootCooldownMin) + shootCooldownMin
}
