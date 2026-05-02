package com.glycin.koita.gameplay.ultimates

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.composables.WorldRendererColors
import com.glycin.koita.core.Camera
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.isOutOfWorldBounds
import com.glycin.koita.util.pulse
import com.glycin.koita.util.steerToward
import com.glycin.koita.world.World
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SwarmBarrage(
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : UltimateAttack(
    id = UltimateId.SWARM_BARRAGE,
    name = "Swarm Barrage",
    requiredUnlockIds = setOf(UnlockId.DOUBLE_JUMP, UnlockId.HOMING_MISSILES, UnlockId.INVULNERABLE),
) {
    private val missilePositions = FloatArray(MISSILE_POOL_SIZE * 2)
    private val missileDirections = FloatArray(MISSILE_POOL_SIZE * 2)
    private val missileActive = BooleanArray(MISSILE_POOL_SIZE)
    private var launchTimer = 0f
    private var barrageTimer = 0f
    private var nextMissileIndex = 0
    private var spawnCooldown = 0f

    override fun activate(player: Player) {
        isActive = true
        launchTimer = 0f
        barrageTimer = 0f
        nextMissileIndex = 0
        spawnCooldown = 0f
        missileActive.fill(false)

        player.applyUltimateVelocity(Vec2(0f, -LAUNCH_SPEED))
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        launchTimer += deltaTime

        if (launchTimer < LAUNCH_DURATION) return

        player.applyUltimateVelocity(Vec2(0f, 0f))

        barrageTimer += deltaTime

        if (barrageTimer >= BARRAGE_DURATION) {
            deactivate(player)
            return
        }

        spawnCooldown -= deltaTime
        if (spawnCooldown <= 0f) {
            spawnMissile(player)
            spawnCooldown = SPAWN_INTERVAL
        }

        for (i in 0 until MISSILE_POOL_SIZE) {
            if (!missileActive[i]) continue

            val i2 = i * 2
            val px = missilePositions[i2]
            val py = missilePositions[i2 + 1]

            val targetCenter = enemyManager.findNearestTargetCenter(px, py, HOMING_RANGE)
            if (targetCenter != null) {
                steerToward(
                    directions = missileDirections,
                    i2 = i2,
                    originX = px,
                    originY = py,
                    targetX = targetCenter.x,
                    targetY = targetCenter.y,
                    strength = HOMING_STRENGTH,
                    deltaTime = deltaTime,
                )
            }

            val newX = px + missileDirections[i2] * MISSILE_SPEED * deltaTime
            val newY = py + missileDirections[i2 + 1] * MISSILE_SPEED * deltaTime

            if (isOutOfWorldBounds(newX, newY)) {
                missileActive[i] = false
                continue
            }

            if (enemyManager.damageFirstColliding(newX - MISSILE_SIZE / 2, newY - MISSILE_SIZE / 2, MISSILE_SIZE, MISSILE_SIZE, DAMAGE_PER_MISSILE)) {
                explodeMissile(newX, newY)
                missileActive[i] = false
                continue
            }

            if (collisionDetector.isSolidAtPosition(newX, newY)) {
                explodeMissile(newX, newY)
                missileActive[i] = false
                continue
            }

            missilePositions[i2] = newX
            missilePositions[i2 + 1] = newY
        }
    }

    private fun spawnMissile(player: Player) {
        val i = nextMissileIndex
        nextMissileIndex = (nextMissileIndex + 1) % MISSILE_POOL_SIZE

        val i2 = i * 2
        val angle = (Random.nextFloat() * 2.0 * PI).toFloat()
        missilePositions[i2] = player.center.x
        missilePositions[i2 + 1] = player.center.y
        missileDirections[i2] = cos(angle)
        missileDirections[i2 + 1] = sin(angle)
        missileActive[i] = true
    }

    private fun explodeMissile(x: Float, y: Float) {
        val pos = Vec2(x, y)
        val affectedTiles = collisionDetector.getTilesInRadius(pos, EXPLOSION_RADIUS)
        explodeTerrain(affectedTiles, pos, EXPLOSION_RADIUS, world, particleSystem)
    }

    override fun deactivate(player: Player) {
        isActive = false
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()

        val pulse = t.pulse(0.2f, 0.15f)
        drawCircle(
            color = WorldRendererColors.MISSILE_AURA_OUTER,
            radius = 60f * pulse,
            center = cx,
        )
        drawCircle(
            color = WorldRendererColors.MISSILE_AURA_INNER,
            radius = 12f,
            center = cx,
        )

        for (i in 0 until MISSILE_POOL_SIZE) {
            if (!missileActive[i]) continue
            val i2 = i * 2
            val screenPos = camera.worldToScreen(missilePositions[i2], missilePositions[i2 + 1])
            drawCircle(
                color = WorldRendererColors.MISSILE,
                radius = 5f,
                center = screenPos,
            )
            drawCircle(
                color = WorldRendererColors.MISSILE_CORE,
                radius = 2.5f,
                center = screenPos,
            )
            drawRect(
                color = WorldRendererColors.MISSILE_TAIL,
                topLeft = Offset(screenPos.x - missileDirections[i2] * 12f - 2f, screenPos.y - missileDirections[i2 + 1] * 12f - 2f),
                size = Size(4f, 4f),
            )
        }
    }

    companion object {
        private const val MISSILE_POOL_SIZE = 50
        private const val MISSILE_SPEED = 500f
        private const val MISSILE_SIZE = 8f
        private const val DAMAGE_PER_MISSILE = 5f
        private const val EXPLOSION_RADIUS = 40f
        private const val HOMING_RANGE = 600f
        private const val HOMING_STRENGTH = 5f
        private const val LAUNCH_SPEED = 800f
        private const val LAUNCH_DURATION = 0.5f
        private const val BARRAGE_DURATION = 5f
        private const val SPAWN_INTERVAL = 0.1f
    }
}
