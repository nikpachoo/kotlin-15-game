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
    drawWidth = 256f,
    drawHeight = 256f,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    override val canAttack = false
    override val contactDamage = 2
    override val dropChance = 0.03f
    override val spriteOffsetY = -32f

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.stone_golem_sheet,
        frameWidth = 256,
        frameHeight = 256,
        columns = 10,
        totalSprites = 200,
        frameDuration = 0.05f,
    )

    private val attackFrames = 0..25
    private val deathFrames = 30..55
    private val hurtFrames = 60..77
    private val idleFrames = 80..140
    private val moveFrames = 150..169

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
