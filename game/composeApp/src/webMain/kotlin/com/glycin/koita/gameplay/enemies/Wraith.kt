package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.wraith_sheet

class Wraith(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    particleSystem: ParticleSystem,
    health: Float = 5f,
) : ShootingEnemy(
    position = position,
    width = 64f,
    height = 64f,
    drawWidth = 64f,
    drawHeight = 64f,
    health = health,
    collisionDetector = collisionDetector,
    world = world,
    particleSystem = particleSystem,
) {
    override val contactDamage = 2
    override val dropChance = 0.05f
    override val spriteOffsetY = -32f

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.wraith_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 14,
        totalSprites = 42,
        frameDuration = 0.1f,
    )

    private val attackFrames = 0..8
    private val deathFrames = 14..20
    private val hurtFrames = 21..27
    private val idleFrames = 28..30
    private val moveFrames = 31..38

    override fun updateBehavior(deltaTime: Float) {
    }

    override fun updateAnimation(deltaTime: Float) {
        when (enemyState) {
            EnemyState.ATTACK -> spriteAnimator.animateOneShot(deltaTime, attackFrames) {
                enemyState = EnemyState.IDLE
            }
            EnemyState.HURT -> spriteAnimator.animateOneShot(deltaTime, hurtFrames) {
                enemyState = EnemyState.IDLE
            }
            EnemyState.DEATH -> spriteAnimator.animateOneShot(deltaTime, deathFrames) {
                isAlive = false
            }
            EnemyState.IDLE -> spriteAnimator.animate(deltaTime, idleFrames)
            EnemyState.FLYING -> spriteAnimator.animate(deltaTime, moveFrames)
            else -> spriteAnimator.animate(deltaTime, idleFrames)
        }
    }
}
