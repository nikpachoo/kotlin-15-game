package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.hydra_sheet

class Hydra( //TODO: Find some different behaviour for the hydra
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    particleSystem: ParticleSystem,
    health: Float = 12f,
    private val patrolSpeed: Float = 60f,
    private val targetPosition: Vec2,
) : ShootingEnemy(
    position = position,
    width = 64f,
    height = 64f,
    drawWidth = 128f,
    drawHeight = 128f,
    health = health,
    collisionDetector = collisionDetector,
    world = world,
    particleSystem = particleSystem,
) {
    override val contactDamage = 1
    override val dropChance = 0.07f
    override val spriteOffsetY = -20f
    override val scoreReward = 400

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.hydra_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 16,
        totalSprites = 32,
        frameDuration = 0.2f,
    )

    private val attackFrames = 0..6
    private val deathFrames = 7..15
    private val hurtFrames = 16..20
    private val idleFrames = 21..23
    private val moveFrames = 24..27

    private val startPosition = position
    private var movingToTarget = true
    private var stuckCooldown = 0f

    override fun updateBehavior(deltaTime: Float) {
        if (stuckCooldown > 0f) {
            stuckCooldown -= deltaTime
            enemyState = EnemyState.IDLE
            return
        }

        val targetPos = if (movingToTarget) targetPosition else startPosition
        val direction = (targetPos - position).normalized()

        enemyFacing = if (direction.x >= 0) EnemyFacing.RIGHT else EnemyFacing.LEFT
        enemyState = EnemyState.FLYING

        val step = patrolSpeed * deltaTime
        if (Vec2.fastDistance(position, targetPos) < step * step) {
            position = targetPos
            movingToTarget = !movingToTarget
            return
        }

        val moveVector = direction * step
        val newPos = position + moveVector

        if (collisionDetector.checkAABB(newPos, width, height)) {
            movingToTarget = !movingToTarget
            stuckCooldown = 0.3f
            return
        }

        position = newPos
    }

    override fun updateAnimation(deltaTime: Float) {
        when (enemyState) {
            EnemyState.FLYING -> spriteAnimator.animate(deltaTime, moveFrames)
            EnemyState.IDLE -> spriteAnimator.animate(deltaTime, idleFrames)
            EnemyState.HURT -> spriteAnimator.animate(deltaTime, hurtFrames)
            EnemyState.DEATH -> spriteAnimator.animateOneShot(deltaTime, deathFrames) {
                isAlive = false
            }
            EnemyState.ATTACK -> spriteAnimator.animate(deltaTime, attackFrames)
            else -> spriteAnimator.animate(deltaTime, idleFrames)
        }
    }
}
