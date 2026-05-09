package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import koita.composeapp.generated.resources.Res
import com.glycin.koita.util.TWO_PI
import koita.composeapp.generated.resources.spider_sheet
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class Spider(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    health: Float = 4f,
) : Enemy(
    position = position,
    width = 64f,
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
    override val dropChance = 0.02f
    override val spriteOffsetY = 0f
    override val scoreReward = 200

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.spider_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 9,
        totalSprites = 36,
        frameDuration = 0.1f,
    )

    private val attackFrames = 9..17
    private val deathFrames = 18..26
    private val hurtFrames = 3..7
    private val idleFrames = 0..2
    private val moveFrames = 27..35

    private var webCooldown = Random.nextFloat() * 3f + 2f
    private val webInterval = 2f
    private val webSpeed = 80f
    private val moveSpeed = 30f
    private val searchRadius = 15

    private var activeWeb: ActiveWeb? = null
    private var moveTarget: Vec2? = null
    private var movePauseTimer = 0f

    private val centerTileX get() = ((position.x + width / 2f) / WorldConstants.TILE_SIZE).toInt()
    private val centerTileY get() = ((position.y + height / 2f) / WorldConstants.TILE_SIZE).toInt()

    override fun updateBehavior(deltaTime: Float) {
        updateWeb(deltaTime)
        updateMovement(deltaTime)
    }

    private fun updateWeb(deltaTime: Float) {
        activeWeb?.let { web ->
            web.progress += webSpeed * deltaTime
            while (web.currentStep < web.progress.toInt()) {
                web.currentStep++
                val tileX = web.startTileX + (web.dirX * web.currentStep).roundToInt()
                val tileY = web.startTileY + (web.dirY * web.currentStep).roundToInt()

                val outOfBounds = tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                        tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES

                if (outOfBounds) {
                    activeWeb = null
                    return
                }

                val tile = world[tileX, tileY]
                when {
                    tile.isIndestructible || tile.isLiquid -> {
                        activeWeb = null
                        return
                    }
                    tile != Tile.WEB -> world[tileX, tileY] = Tile.WEB
                }
            }
        } ?: run {
            webCooldown -= deltaTime
            if (webCooldown <= 0f) {
                startWeb()
                webCooldown = webInterval + Random.nextFloat() * 2f
                movePauseTimer = 0.5f + Random.nextFloat() * 1.5f
            }
        }
    }

    private fun updateMovement(deltaTime: Float) {
        if (movePauseTimer > 0f) {
            movePauseTimer -= deltaTime
            enemyState = EnemyState.IDLE
            return
        }

        moveTarget?.let { target ->
            enemyState = EnemyState.WALKING
            val direction = (target - position).normalized()
            enemyFacing = if (direction.x >= 0) EnemyFacing.RIGHT else EnemyFacing.LEFT

            val step = moveSpeed * deltaTime
            if (Vec2.fastDistance(position, target) <= step * step) {
                position = target
                moveTarget = null
            } else {
                position += direction * step
            }
        } ?: run {
            enemyState = EnemyState.IDLE
            pickWebMoveTarget()
        }
    }

    private fun pickWebMoveTarget() {
        val tileSize = WorldConstants.TILE_SIZE
        val cx = centerTileX
        val cy = centerTileY

        moveTarget = buildList {
            for (dy in -searchRadius..searchRadius) {
                for (dx in -searchRadius..searchRadius) {
                    if (dx == 0 && dy == 0) continue
                    val tx = cx + dx
                    val ty = cy + dy
                    if (tx !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                        ty !in 0 until WorldConstants.WORLD_HEIGHT_TILES) continue
                    if (world[tx, ty] == Tile.WEB) {
                        add(Vec2(tx * tileSize.toFloat(), ty * tileSize.toFloat()))
                    }
                }
            }
        }.randomOrNull()
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
            else -> spriteAnimator.animate(deltaTime, idleFrames)
        }
    }

    private fun startWeb() {
        val angle = Random.nextFloat() * TWO_PI
        activeWeb = ActiveWeb(
            dirX = cos(angle),
            dirY = sin(angle),
            startTileX = centerTileX,
            startTileY = centerTileY,
        )
    }

    private data class ActiveWeb(
        val dirX: Float,
        val dirY: Float,
        val startTileX: Int,
        val startTileY: Int,
        var currentStep: Int = 0,
        var progress: Float = 0f,
    )
}
