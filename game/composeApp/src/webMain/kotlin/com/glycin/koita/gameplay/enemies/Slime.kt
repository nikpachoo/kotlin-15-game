package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.physics.PhysicsConstants
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.slime_sheet

class Slime(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    health: Float = 3f,
    private val particleSystem: ParticleSystem,
    private val moveSpeed: Float = 40f,
) : Enemy(
    position = position,
    width = 32f,
    height = 32f,
    drawWidth = 64f,
    drawHeight = 64f,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    override val canAttack = false
    override val contactDamage = 1
    override val dropChance = 0.06f
    override val spriteOffsetY = -8f
    override val scoreReward = 100

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.slime_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 16,
        totalSprites = 32,
        frameDuration = 0.1f,
    )

    private val deathFrames = 16..23
    private val hurtFrames = 9..14
    private val idleFrames = 0..2
    private val moveFrames = 24..32
    private val fallingFrame = 25

    private val maxFallSpeed = 500f
    private val jumpForce = 350f
    private var velocity = Vec2(0f, 0f)
    private var isGrounded = false
    private var direction = 1f

    private var lastPosition = position.copy()
    private var stuckTimer = 0f
    private val stuckThreshold = 0.3f
    private val stuckDistanceMin = 2f
    private val stuckDistanceMinSq = stuckDistanceMin * stuckDistanceMin

    private var trailCooldown = 0f
    private val trailInterval = 0.25f

    override fun updateBehavior(deltaTime: Float) {
        val moveX = direction * moveSpeed * deltaTime
        val newPosX = Vec2(position.x + moveX, position.y)

        if (collisionDetector.checkAABB(newPosX, width, height, position)) {
            direction = -direction
        } else {
            position = newPosX
        }

        enemyFacing = if (direction > 0) EnemyFacing.RIGHT else EnemyFacing.LEFT
        enemyState = EnemyState.WALKING

        velocity = Vec2(velocity.x, (velocity.y + PhysicsConstants.GRAVITY * deltaTime).coerceAtMost(maxFallSpeed))
        val moveY = velocity.y * deltaTime
        val newPosY = Vec2(position.x, position.y + moveY)

        if (!collisionDetector.checkAABB(newPosY, width, height, position)) {
            position = newPosY
            isGrounded = false
        } else {
            val wasFalling = velocity.y > 0f
            velocity = Vec2(velocity.x, 0f)
            if (wasFalling) {
                isGrounded = true
            }
        }

        if (!isGrounded) {
            val groundCheckPos = position + Vec2(0f, height + 2f)
            isGrounded = collisionDetector.checkAABB(groundCheckPos, width, 1f)
            if (isGrounded) {
                velocity = Vec2(velocity.x, 0f)
            }
        }

        if (isGrounded) {
            val aheadX = position.x + (if (direction > 0) width + 2f else -2f)
            val belowY = position.y + height + 4f
            val ledgeCheck = Vec2(aheadX, belowY)
            if (!collisionDetector.checkAABB(ledgeCheck, 1f, 1f)) {
                direction = -direction
            }
        }

        // Stuck detection: if barely moved, jump
        if (Vec2.fastDistance(position, lastPosition) < stuckDistanceMinSq) {
            stuckTimer += deltaTime
            if (stuckTimer >= stuckThreshold && isGrounded) {
                velocity = Vec2(0f, -jumpForce)
                isGrounded = false
                direction = -direction
                stuckTimer = 0f
            }
        } else {
            stuckTimer = 0f
        }
        lastPosition = position.copy()

        trailCooldown -= deltaTime
        if (isGrounded && trailCooldown <= 0f) {
            leaveTrail()
            trailCooldown = trailInterval
        }

        if (!isGrounded) {
            enemyState = EnemyState.FALLING
        }
    }

    private fun leaveTrail() {
        val behindX = if (direction > 0) position.x - WorldConstants.TILE_SIZE else position.x + width
        val feetY = position.y + height - WorldConstants.TILE_SIZE
        particleSystem.addParticle(Vec2(behindX, feetY), Vec2(0f, 50f), Tile.SLIME)
    }

    override fun updateAnimation(deltaTime: Float) {
        when (enemyState) {
            EnemyState.HURT -> spriteAnimator.animateOneShot(deltaTime, hurtFrames) {
                enemyState = EnemyState.IDLE
            }
            EnemyState.DEATH -> spriteAnimator.animateOneShot(deltaTime, deathFrames) {
                isAlive = false
            }
            EnemyState.IDLE -> spriteAnimator.animate(deltaTime, idleFrames)
            EnemyState.WALKING -> spriteAnimator.animate(deltaTime, moveFrames)
            EnemyState.FALLING -> spriteAnimator.setFrame(fallingFrame)
            else -> spriteAnimator.animate(deltaTime, idleFrames)
        }
    }
}
