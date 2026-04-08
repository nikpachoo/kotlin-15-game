package com.glycin.koita.gameplay.weapon

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.activate
import com.glycin.koita.util.lerp
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

class Sniper(
    private val gameState: GameState,
    private val world: World,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : Weapon {

    var isActive = false
    var isCharging = false
        private set
    var chargeTime = 0f
        private set

    var guideStart = Vec2()
        private set
    var guideLineLeft = Vec2()
        private set
    var guideLineRight = Vec2()
        private set

    var bulletActive = false
    var bulletStart = Vec2()
        private set
    var bulletEnd = Vec2()
        private set
    var bulletDamage = 0f
        private set

    override val isAlive: Boolean
        get() = isCharging || bulletActive

    private var wasActive = false
    private var bulletFlashTimer = 0f
    private var lastOrigin = Vec2()
    private var lastBaseAngle = 0f

    companion object {
        private const val MAX_CHARGE_TIME = 3f
        private const val MIN_DAMAGE = 1f
        private const val MAX_DAMAGE = 10f
        private const val MAX_RANGE = 2000f
        private const val INITIAL_SPREAD_RADIANS = 0.785f
        private const val BULLET_FLASH_DURATION = 0.15f
        private const val RAY_STEP = 2f
        private const val ENEMY_CHECK_INTERVAL = 8
    }

    private fun currentSpread() = INITIAL_SPREAD_RADIANS * (1f - chargeTime / MAX_CHARGE_TIME)

    fun update(deltaTime: Float, origin: Vec2, target: Vec2) {
        if (isActive) {
            chargeTime = (chargeTime + deltaTime).coerceAtMost(MAX_CHARGE_TIME)
            isCharging = true

            val dx = target.x - origin.x
            val dy = target.y - origin.y
            lastBaseAngle = atan2(dy, dx)
            lastOrigin.x = origin.x
            lastOrigin.y = origin.y

            val spread = currentSpread()
            val leftAngle = lastBaseAngle - spread
            val rightAngle = lastBaseAngle + spread

            guideStart.x = origin.x
            guideStart.y = origin.y
            guideLineLeft.x = origin.x + cos(leftAngle) * MAX_RANGE
            guideLineLeft.y = origin.y + sin(leftAngle) * MAX_RANGE
            guideLineRight.x = origin.x + cos(rightAngle) * MAX_RANGE
            guideLineRight.y = origin.y + sin(rightAngle) * MAX_RANGE
        } else if (wasActive) {
            fire()
        }

        if (bulletActive) {
            bulletFlashTimer -= deltaTime
            if (bulletFlashTimer <= 0f) {
                bulletActive = false
            }
        }

        wasActive = isActive
    }

    private fun fire() {
        val damage = MIN_DAMAGE.lerp(MAX_DAMAGE, chargeTime / MAX_CHARGE_TIME)
        val spread = currentSpread()
        val randomOffset = (Random.nextFloat() * 2f - 1f) * spread
        val fireAngle = lastBaseAngle + randomOffset
        val dirX = cos(fireAngle)
        val dirY = sin(fireAngle)

        SoundManager.playOneShot(Sounds.SHOOT)
        firePiercingShot(lastOrigin, dirX, dirY, damage)

        bulletStart.x = lastOrigin.x
        bulletStart.y = lastOrigin.y
        bulletDamage = damage
        bulletActive = true
        bulletFlashTimer = BULLET_FLASH_DURATION
        chargeTime = 0f
        isCharging = false
    }

    private fun firePiercingShot(origin: Vec2, dirX: Float, dirY: Float, damage: Float) {
        val maxSteps = (MAX_RANGE / RAY_STEP).roundToInt()
        var posX = origin.x
        var posY = origin.y

        for (i in 0..<maxSteps) {
            posX += dirX * RAY_STEP
            posY += dirY * RAY_STEP

            val tileX = (posX / WorldConstants.TILE_SIZE).toInt()
            val tileY = (posY / WorldConstants.TILE_SIZE).toInt()

            if (tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES) {
                bulletEnd.x = posX
                bulletEnd.y = posY
                return
            }

            val tile = world[tileX, tileY]

            if (tile.isIndestructible) {
                bulletEnd.x = posX
                bulletEnd.y = posY
                return
            }

            if (tile.isFragile) {
                world[tileX, tileY] = Tile.AIR
            } else if (tile != Tile.AIR && !tile.isLiquid) {
                tile.activate(
                    tileX = tileX,
                    tileY = tileY,
                    sourcePosition = origin,
                    particleSystem = particleSystem,
                    impactRadius = MAX_RANGE,
                )
                world[tileX, tileY] = Tile.AIR
            }

            if (i % ENEMY_CHECK_INTERVAL == 0) {
                val enemy = enemyManager.findFirstEnemyCollidingWith(posX, posY, 4f, 4f)
                if (enemy != null) {
                    enemy.takeDamage(damage * gameState.damageMultiplier)
                    bulletEnd.x = posX
                    bulletEnd.y = posY
                    return
                }
            }
        }

        bulletEnd.x = posX
        bulletEnd.y = posY
    }
}
