package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.util.lerp
import com.glycin.koita.world.World
import kotlin.math.abs
import kotlin.math.sqrt

class BossEyeBeam(
    private val origin: () -> Vec2,
    private val player: Player,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
) {
    var alive = true
        private set
    var charging = true
        private set

    private val chargeDuration = 0.5f
    private val lockDuration = 1.5f
    private val beamLength = 800f
    private val beamWidth = 24f
    private val damage = 1
    private val chargeTrackingSpeed = 6f
    private val damageCooldownInterval = 0.4f
    private val terrainDamageInterval = 0.15f
    private val terrainImpactRadius = 25f

    private var timer = 0f
    private var damageCooldown = 0f
    private var terrainDamageTimer = 0f

    private var trackedTargetX = player.center.x
    private var trackedTargetY = player.center.y

    private val farEnd = Vec2(0f, 0f)
    private val impactPos = Vec2(0f, 0f)
    private var cachedStartX = 0f
    private var cachedStartY = 0f
    private var cachedEndX = 0f
    private var cachedEndY = 0f

    fun update(deltaTime: Float) {
        if (!alive) return

        timer += deltaTime
        damageCooldown -= deltaTime
        terrainDamageTimer -= deltaTime

        val o = origin()
        val target = player.center

        val aimX: Float
        val aimY: Float
        if (charging) {
            val k = (chargeTrackingSpeed * deltaTime).coerceAtMost(1f)
            trackedTargetX = trackedTargetX.lerp(target.x, k)
            trackedTargetY = trackedTargetY.lerp(target.y, k)
            aimX = trackedTargetX
            aimY = trackedTargetY
            if (timer >= chargeDuration) {
                charging = false
                timer = 0f
            }
        } else {
            aimX = target.x
            aimY = target.y
            if (timer >= lockDuration) {
                alive = false
                return
            }
        }

        val dx = aimX - o.x
        val dy = aimY - o.y
        val mag = sqrt(dx * dx + dy * dy)
        val dirX: Float
        val dirY: Float
        if (mag > 1e-4f) {
            dirX = dx / mag
            dirY = dy / mag
        } else {
            dirX = 1f
            dirY = 0f
        }

        farEnd.x = o.x + dirX * beamLength
        farEnd.y = o.y + dirY * beamLength
        val hit = collisionDetector.raycast(o, farEnd, beamLength.toInt())

        cachedStartX = o.x
        cachedStartY = o.y
        cachedEndX = hit.x
        cachedEndY = hit.y

        if (charging) return

        if (terrainDamageTimer <= 0f) {
            terrainDamageTimer = terrainDamageInterval
            destroyTerrainAtImpact()
        }

        if (damageCooldown > 0f) return

        val hitDx = hit.x - o.x
        val hitDy = hit.y - o.y
        val hitDistSq = hitDx * hitDx + hitDy * hitDy

        val projection = dx * dirX + dy * dirY
        if (projection < 0f || projection * projection > hitDistSq) return

        val perpDist = abs(dx * dirY - dy * dirX)
        if (perpDist < player.width / 2f + beamWidth / 2f) {
            player.takeDamage(damage)
            damageCooldown = damageCooldownInterval
        }
    }

    private fun destroyTerrainAtImpact() {
        impactPos.x = cachedEndX
        impactPos.y = cachedEndY
        val tiles = collisionDetector.getTilesInRadius(impactPos, terrainImpactRadius)
        explodeTerrain(tiles, impactPos, terrainImpactRadius, world, particleSystem)
    }

    fun renderBeam(action: (startX: Float, startY: Float, endX: Float, endY: Float, charging: Boolean) -> Unit) {
        if (!alive) return
        action(cachedStartX, cachedStartY, cachedEndX, cachedEndY, charging)
    }
}
