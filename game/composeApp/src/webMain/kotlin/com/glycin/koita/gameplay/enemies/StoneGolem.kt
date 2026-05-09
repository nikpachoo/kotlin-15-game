package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.PhysicsConstants
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.stone_golem_sheet

class StoneGolem(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    health: Float = 5f,
    private val moveSpeed: Float = 25f,
    private val player: Player,
) : Enemy(
    position = position,
    width = 32f,
    height = 32f,
    drawWidth = 128f,
    drawHeight = 128f,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    override val canAttack = false
    override val contactDamage = 2
    override val dropChance = 0.03f
    override val spriteOffsetY = -32f
    override val scoreReward = 300

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.stone_golem_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 10,
        totalSprites = 50,
        frameDuration = 0.15f,
    )

    private val attackFrames = 0..8
    private val deathFrames = 10..18
    private val hurtFrames = 20..27
    private val idleFrames = 30..32
    private val moveFrames = 40..45

    private val digRadius = 2
    private val maxFallSpeed = 500f
    private var verticalVelocity = 0f

    override fun updateBehavior(deltaTime: Float) {
        val direction = (player.position - position).normalized()
        enemyFacing = if (direction.x >= 0) EnemyFacing.RIGHT else EnemyFacing.LEFT
        enemyState = EnemyState.WALKING

        val movement = direction * moveSpeed * deltaTime
        position += movement

        verticalVelocity = (verticalVelocity + PhysicsConstants.GRAVITY * deltaTime).coerceAtMost(maxFallSpeed)
        val fallDistance = verticalVelocity * deltaTime
        val fallenPos = Vec2(position.x, position.y + fallDistance)
        if (!collisionDetector.checkAABB(fallenPos, width, height, position)) {
            position = fallenPos
        } else {
            verticalVelocity = 0f
        }

        digTilesAround()
    }

    private fun digTilesAround() {
        val tileSize = WorldConstants.TILE_SIZE
        val centerTileX = ((position.x + width / 2f) / tileSize).toInt()
        val centerTileY = ((position.y + height / 2f) / tileSize).toInt()

        for (dy in -digRadius..digRadius) {
            for (dx in -digRadius..digRadius) {
                val tileX = centerTileX + dx
                val tileY = centerTileY + dy

                if (tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                    tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES) continue

                val tile = world[tileX, tileY]
                if (tile.isSolid && !tile.isIndestructible) {
                    world[tileX, tileY] = Tile.AIR
                }
            }
        }
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
            EnemyState.WALKING -> spriteAnimator.animate(deltaTime, moveFrames)
            EnemyState.FALLING -> spriteAnimator.setFrame(38)
            else -> spriteAnimator.animate(deltaTime, idleFrames)
        }
    }
}
