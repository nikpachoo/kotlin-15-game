package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import koita.composeapp.generated.resources.Res
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.world.isValidTile
import koita.composeapp.generated.resources.confuser_sheet
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class Confuser(
    position: Vec2,
    collisionDetector: CollisionDetector,
    world: World,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    health: Float = 8f,
) : Enemy(
    position = position,
    width = 64f,
    height = 64f,
    drawWidth = 128f,
    drawHeight = 128f,
    health = health,
    maxHealth = health,
    collisionDetector = collisionDetector,
    world = world,
) {
    override val canAttack = false
    override val contactDamage = 3
    override val dropChance = 0.1f
    override val spriteOffsetY = -32f
    override val scoreReward = 1000

    override val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.confuser_sheet,
        frameWidth = 128,
        frameHeight = 128,
        columns = 14,
        totalSprites = 42,
        frameDuration = 0.1f,
    )

    private val attackFrames = 0..8
    private val deathFrames = 14..20
    private val hurtFrames = 21..27
    private val idleFrames = 31..40
    private val moveFrames = 31..40

    private val floatSpeed = 30f
    private val pullRadius = 150f
    private val shootSpeed = 500f
    private val maxPulledTiles = 256
    private val tilesPerVolley = 64

    // Gravity field: pulled tile positions orbiting around the confuser
    private val pulledTiles = FloatArray(maxPulledTiles * 2) // x, y pairs
    private val pulledActive = BooleanArray(maxPulledTiles)
    private var pulledCount = 0

    private val orbitSpeed = 4f
    private val orbitRadius = 60f
    private var orbitAngle = 0f

    // Lava projectiles shot toward the player
    private val maxProjectiles = 64
    private val projectilePositions = FloatArray(maxProjectiles * 2)
    private val projectileVelocities = FloatArray(maxProjectiles * 2)
    private val projectileActive = BooleanArray(maxProjectiles)

    override fun updateAlways(deltaTime: Float) {
        updateProjectiles(deltaTime)
    }

    override fun updateBehavior(deltaTime: Float) {
        enemyState = EnemyState.FLYING

        val dx = player.center.x - center.x
        val dy = player.center.y - center.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist > 1f) {
            val scale = floatSpeed * deltaTime / dist
            position += Vec2(dx * scale, dy * scale)
        }

        enemyFacing = if (player.center.x >= center.x) EnemyFacing.RIGHT else EnemyFacing.LEFT

        carvePath()

        pullTilesFromWorld()
        updatePulledTiles(deltaTime)

        renderOffset = if (pulledCount > 0) {
            Vec2(
                (Random.nextFloat() - 0.5f) * 6f,
                (Random.nextFloat() - 0.5f) * 6f,
            )
        } else {
            Vec2.zero()
        }

        if (pulledCount >= maxPulledTiles) {
            shootAllTiles()
        }
    }

    private fun shootAllTiles() {
        var shot = 0
        for (i in 0 until maxPulledTiles) {
            if (!pulledActive[i]) continue
            pulledActive[i] = false
            pulledCount--
            spawnLavaProjectile(pulledTiles[i * 2], pulledTiles[i * 2 + 1])
            shot++
            if (shot >= tilesPerVolley) break
        }
    }

    private fun carvePath() {
        val tileSize = WorldConstants.TILE_SIZE
        val minTX = (position.x / tileSize).toInt()
        val maxTX = ((position.x + width) / tileSize).toInt()
        val minTY = (position.y / tileSize).toInt()
        val maxTY = ((position.y + height) / tileSize).toInt()

        for (ty in minTY..maxTY) {
            for (tx in minTX..maxTX) {
                if (isValidTile(tx, ty)) {
                    val tile = world[tx, ty]
                    if (tile != Tile.AIR && !tile.isIndestructible) {
                        world[tx, ty] = Tile.AIR
                    }
                }
            }
        }
    }

    private fun pullTilesFromWorld() {
        if (pulledCount >= maxPulledTiles) return

        val tileSize = WorldConstants.TILE_SIZE
        val affectedTiles = collisionDetector.getTilesInRadius(center, pullRadius)

        for ((tileX, tileY) in affectedTiles) {
            if (pulledCount >= maxPulledTiles) return

            val tile = world[tileX, tileY]
            if (tile == Tile.AIR || tile.isIndestructible || tile.isLiquid) continue

            world[tileX, tileY] = Tile.AIR

            val index = findFreeSlot(pulledActive) ?: continue
            val i2 = index * 2
            pulledTiles[i2] = tileX * tileSize + tileSize / 2f
            pulledTiles[i2 + 1] = tileY * tileSize + tileSize / 2f
            pulledActive[index] = true
            pulledCount++
        }
    }

    private fun findFreeSlot(active: BooleanArray): Int? {
        for (i in active.indices) {
            if (!active[i]) return i
        }
        return null
    }

    private fun updatePulledTiles(deltaTime: Float) {
        orbitAngle += orbitSpeed * deltaTime

        for (i in 0 until maxPulledTiles) {
            if (!pulledActive[i]) continue

            val i2 = i * 2

            val angleOffset = (i.toFloat() / maxPulledTiles) * TWO_PI
            val radius = orbitRadius + (i % 4) * 10f
            val angle = orbitAngle + angleOffset

            val targetX = center.x + cos(angle) * radius
            val targetY = center.y + sin(angle) * radius

            pulledTiles[i2] += (targetX - pulledTiles[i2]) * 8f * deltaTime
            pulledTiles[i2 + 1] += (targetY - pulledTiles[i2 + 1]) * 8f * deltaTime
        }
    }

    private fun spawnLavaProjectile(fromX: Float, fromY: Float) {
        val index = findFreeSlot(projectileActive) ?: return
        val i2 = index * 2

        val dx = player.center.x - fromX
        val dy = player.center.y - fromY
        val dist = sqrt(dx * dx + dy * dy)
        if (dist == 0f) return

        val spread = (Random.nextFloat() - 0.5f) * 0.4f

        projectilePositions[i2] = fromX
        projectilePositions[i2 + 1] = fromY
        projectileVelocities[i2] = (dx / dist + spread) * shootSpeed
        projectileVelocities[i2 + 1] = (dy / dist + spread) * shootSpeed
        projectileActive[index] = true
    }

    private fun updateProjectiles(deltaTime: Float) {
        val tileSize = WorldConstants.TILE_SIZE

        for (i in 0 until maxProjectiles) {
            if (!projectileActive[i]) continue

            val i2 = i * 2
            projectilePositions[i2] += projectileVelocities[i2] * deltaTime
            projectilePositions[i2 + 1] += projectileVelocities[i2 + 1] * deltaTime

            val px = projectilePositions[i2]
            val py = projectilePositions[i2 + 1]

            val tileX = (px / tileSize).toInt()
            val tileY = (py / tileSize).toInt()

            if (!isValidTile(tileX, tileY)) {
                projectileActive[i] = false
                continue
            }

            val pp = player.position
            if (px - 2f < pp.x + player.width &&
                px + 2f > pp.x &&
                py - 2f < pp.y + player.height &&
                py + 2f > pp.y
            ) {
                projectileActive[i] = false
                player.takeDamage(1)
                continue
            }

            val tile = world[tileX, tileY]
            if (tile.isSolid || tile == Tile.WATER) {
                projectileActive[i] = false
                val aboveY = tileY - 1
                if (aboveY in 0 until WorldConstants.WORLD_HEIGHT_TILES && world[tileX, aboveY] == Tile.AIR) {
                    world[tileX, aboveY] = Tile.LAVA
                    fluidSimulator.registerFluid(tileX, aboveY)
                } else {
                    if (!tile.isIndestructible) {
                        world[tileX, tileY] = Tile.LAVA
                        fluidSimulator.registerFluid(tileX, tileY)
                    }
                }
                continue
            }

            if (tile == Tile.AIR && Random.nextFloat() < 0.02f) {
                world[tileX, tileY] = Tile.LAVA
                fluidSimulator.registerFluid(tileX, tileY)
            }
        }
    }

    fun forEachPulledTile(action: (x: Float, y: Float) -> Unit) {
        for (i in 0 until maxPulledTiles) {
            if (!pulledActive[i]) continue
            action(pulledTiles[i * 2], pulledTiles[i * 2 + 1])
        }
    }

    fun forEachProjectile(action: (x: Float, y: Float) -> Unit) {
        for (i in 0 until maxProjectiles) {
            if (!projectileActive[i]) continue
            action(projectilePositions[i * 2], projectilePositions[i * 2 + 1])
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
