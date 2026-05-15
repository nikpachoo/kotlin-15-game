package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.isValidTile
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.null_phantom_sheet
import kotlin.random.Random

class DashingPhantom(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    private val player: Player,
    private val particleSystem: ParticleSystem,
    health: Float = 8f,
) : Enemy(
    position = position,
    width = 32f,
    height = 32f,
    drawWidth = 100f,
    drawHeight = 100f,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    override val canAttack = false
    override val contactDamage = 2
    override val dropChance = 0.12f
    override val spriteOffsetY = -24f
    override val scoreReward = 700

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.null_phantom_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 16,
        totalSprites = 32,
        frameDuration = 0.1f,
    )

    private val attackFrames = 0..8
    private val deathFrames = 9..15
    private val hurtFrames = 16..21
    private val idleFrames = 22..24
    private val moveFrames = 25..30

    private enum class WraithPhase { HOVERING, SHAKING, DASHING, COOLDOWN }

    private var phase = WraithPhase.HOVERING
    private var phaseTimer = 0f

    private val detectionRange = 500f
    private val detectionRangeSq = detectionRange * detectionRange
    private val hoverSpeed = 20f
    private val dashSpeed = 1500f
    private val shakeDuration = 1.5f
    private val cooldownDuration = 3f
    private val explosionRadius = 50f

    private var hoverDirection = Vec2(
        Random.nextFloat() * 2f - 1f,
        Random.nextFloat() * 2f - 1f,
    ).normalized()
    private var hoverChangeTimer = 0f

    private var dashDirection = Vec2.zero()
    private var dashMaxDistance = 0f
    private var dashTravelled = 0f
    private var hasHitPlayer = false

    override fun updateBehavior(deltaTime: Float) {
        when (phase) {
            WraithPhase.HOVERING -> updateHover(deltaTime)
            WraithPhase.SHAKING -> updateShake(deltaTime)
            WraithPhase.DASHING -> updateDash(deltaTime)
            WraithPhase.COOLDOWN -> updateCooldown(deltaTime)
        }
    }

    private fun updateHover(deltaTime: Float) {
        enemyState = EnemyState.FLYING

        hoverChangeTimer -= deltaTime
        if (hoverChangeTimer <= 0f) {
            hoverDirection = Vec2(
                Random.nextFloat() * 2f - 1f,
                Random.nextFloat() * 2f - 1f,
            ).normalized()
            hoverChangeTimer = 2f + Random.nextFloat() * 2f
        }

        val newPos = position + hoverDirection * hoverSpeed * deltaTime
        if (!collisionDetector.checkAABB(newPos, width, height)) {
            position = newPos
        } else {
            hoverDirection = -hoverDirection
        }

        enemyFacing = if (hoverDirection.x >= 0) EnemyFacing.RIGHT else EnemyFacing.LEFT

        if (Vec2.fastDistance(center, player.center) <= detectionRangeSq) {
            phase = WraithPhase.SHAKING
            phaseTimer = shakeDuration
        }
    }

    private fun updateShake(deltaTime: Float) {
        enemyState = EnemyState.FLYING
        phaseTimer -= deltaTime

        renderOffset = Vec2(
            (Random.nextFloat() - 0.5f) * 10f,
            (Random.nextFloat() - 0.5f) * 10f,
        )

        enemyFacing = if (player.center.x >= center.x) EnemyFacing.RIGHT else EnemyFacing.LEFT

        if (phaseTimer <= 0f) {
            renderOffset = Vec2.zero()
            dashDirection = (player.center - center).normalized()
            dashMaxDistance = Vec2.distance(center, player.center) + 50f
            dashTravelled = 0f
            hasHitPlayer = false
            phase = WraithPhase.DASHING
        }
    }

    private fun updateDash(deltaTime: Float) {
        enemyState = EnemyState.FLYING

        val step = dashSpeed * deltaTime
        val newPos = position + dashDirection * step
        dashTravelled += step

        carveLine(position, newPos)
        position = newPos

        if (!hasHitPlayer && collisionDetector.checkAABBOverlap(position, width, height, player.position, player.width, player.height)) {
            player.takeDamage(2)
            hasHitPlayer = true
        }

        if (dashTravelled >= dashMaxDistance) {
            explode()
            phase = WraithPhase.COOLDOWN
            phaseTimer = cooldownDuration
        }
    }

    private fun updateCooldown(deltaTime: Float) {
        enemyState = EnemyState.IDLE
        phaseTimer -= deltaTime
        if (phaseTimer <= 0f) {
            phase = WraithPhase.HOVERING
        }
    }

    // Clears tiles without particle effects for performance
    private fun carveLine(from: Vec2, to: Vec2) {
        val tileSize = WorldConstants.TILE_SIZE
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dist = Vec2.distance(from, to)
        if (dist == 0f) return
        val dirX = dx / dist
        val dirY = dy / dist
        val step = tileSize / 2f

        var t = 0f
        while (t <= dist) {
            val tileX = ((from.x + dirX * t) / tileSize).toInt()
            val tileY = ((from.y + dirY * t) / tileSize).toInt()

            for (dy in -1..1) {
                val ty = tileY + dy
                if (isValidTile(tileX, ty)) {
                    val tile = world[tileX, ty]
                    if (tile != Tile.AIR && !tile.isIndestructible) {
                        world[tileX, ty] = Tile.AIR
                    }
                }
            }
            t += step
        }
    }

    private fun explode() {
        val affectedTiles = collisionDetector.getTilesInRadius(center, explosionRadius)
        explodeTerrain(affectedTiles, center, explosionRadius, world, particleSystem)
    }

    override fun updateAnimation(deltaTime: Float) {
        when (enemyState) {
            EnemyState.ATTACK -> spriteAnimator.animate(deltaTime, attackFrames)
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
