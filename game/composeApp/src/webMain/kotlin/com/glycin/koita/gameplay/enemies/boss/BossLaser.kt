package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.World
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class BossLaser(
    private val origin: () -> Vec2,
    private val player: Player,
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val laserCount: Int = 6,
    private val laserLength: Float = 800f,
    private val rotationSpeed: Float = 0.75f,
    private val duration: Float = 6f,
    private val damage: Int = 1,
    private val laserWidth: Float = 6f,
) {
    var alive = true
        private set

    private var timer = 0f
    private var angle = 0f
    private var damageCooldown = 0f
    private val damageCooldownInterval = 0.5f
    private var terrainDamageTimer = 0f
    private val terrainDamageInterval = 0.15f
    private val terrainImpactRadius = 12f

    private val laserDirX = FloatArray(laserCount)
    private val laserDirY = FloatArray(laserCount)
    private val laserEndX = FloatArray(laserCount)
    private val laserEndY = FloatArray(laserCount)
    private val laserHitDistSq = FloatArray(laserCount)
    private var cachedCenterX = 0f
    private var cachedCenterY = 0f

    fun update(deltaTime: Float) {
        if (!alive) return

        timer += deltaTime
        if (timer >= duration) {
            alive = false
            return
        }

        angle += rotationSpeed * deltaTime
        damageCooldown -= deltaTime
        terrainDamageTimer -= deltaTime

        val center = origin()
        cachedCenterX = center.x
        cachedCenterY = center.y

        for (i in 0 until laserCount) {
            val laserAngle = angle + (i.toFloat() / laserCount) * TWO_PI
            laserDirX[i] = cos(laserAngle)
            laserDirY[i] = sin(laserAngle)

            val farEnd = Vec2(center.x + laserDirX[i] * laserLength, center.y + laserDirY[i] * laserLength)
            val hit = collisionDetector.raycast(center, farEnd, laserLength.toInt())
            laserEndX[i] = hit.x
            laserEndY[i] = hit.y
            val dx = hit.x - center.x
            val dy = hit.y - center.y
            laserHitDistSq[i] = dx * dx + dy * dy
        }

        if (terrainDamageTimer <= 0f) {
            terrainDamageTimer = terrainDamageInterval
            for (i in 0 until laserCount) {
                destroyTerrainAtEnd(i)
            }
        }

        if (damageCooldown > 0f) return

        val playerCenter = player.center
        val playerRadius = player.width / 2f
        val toPlayerX = playerCenter.x - center.x
        val toPlayerY = playerCenter.y - center.y

        for (i in 0 until laserCount) {
            val projection = toPlayerX * laserDirX[i] + toPlayerY * laserDirY[i]

            if (projection < 0f || projection * projection > laserHitDistSq[i]) continue

            val perpDist = abs(toPlayerX * laserDirY[i] - toPlayerY * laserDirX[i])

            if (perpDist < playerRadius + laserWidth / 2f) {
                player.takeDamage(damage)
                damageCooldown = damageCooldownInterval
                break
            }
        }
    }

    private fun destroyTerrainAtEnd(laserIndex: Int) {
        val pos = Vec2(laserEndX[laserIndex], laserEndY[laserIndex])
        val tiles = collisionDetector.getTilesInRadius(pos, terrainImpactRadius)
        explodeTerrain(tiles, pos, terrainImpactRadius, world, particleSystem)
    }

    fun forEachLaser(action: (startX: Float, startY: Float, endX: Float, endY: Float) -> Unit) {
        if (!alive) return
        for (i in 0 until laserCount) {
            action(cachedCenterX, cachedCenterY, laserEndX[i], laserEndY[i])
        }
    }
}
