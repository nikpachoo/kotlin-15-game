package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.composables.WorldRendererColors
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.util.angleTo
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.util.pulse
import com.glycin.koita.world.World
import kotlin.math.cos
import kotlin.math.sin

private val AURA_FILL_COLOR = Color(0x22FF4400)
private val AURA_RING_COLOR = Color(0x33FF4400)
private val ROCKET_BODY_COLOR = Color(0xFFCC2200)
private val ROCKET_FIN_COLOR = Color(0xFF991100)
private val NOSE_COLOR = HudColors.BANNER_LABEL
private val NOSE_CORE_COLOR = WorldRendererColors.SNIPER_BULLET_CORE

class RocketRide(
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
    private val mouse: Mouse,
) : UltimateAttack(
    id = UltimateId.ROCKET_RIDE,
    name = "Ride the Rocket",
    requiredUnlockIds = setOf(UnlockId.JETPACK, UnlockId.EXPLODING_BLOCKS, UnlockId.ROCKET_LAUNCHER),
) {
    override val bossShieldDamage: Int = 1
    override val usesBoostAnimation: Boolean = true

    private var timer = 0f
    private var dismountRequested = false
    private var currentDirection = Vec2.zero()

    override fun onReactivate(): Boolean {
        dismountRequested = true
        return true
    }

    override fun activate(player: Player) {
        isActive = true
        timer = DURATION
        dismountRequested = false

        val mouseWorld = mouse.worldPosition
        currentDirection = (mouseWorld - player.center).normalized()
        player.applyUltimateVelocity(currentDirection * FLY_SPEED)
        SoundManager.playOneShot(Sounds.EXPLODE)
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        timer -= deltaTime
        if (dismountRequested) {
            deactivate(player)
            return
        }
        if (timer <= 0f) {
            performFinalExplosion(player)
            deactivate(player)
            return
        }

        val mouseWorld = mouse.worldPosition
        currentDirection = (mouseWorld - player.center).normalized()
        player.applyUltimateVelocity(currentDirection * FLY_SPEED)

        val center = player.center

        val affectedTiles = collisionDetector.getTilesInRadius(center, DESTRUCTION_RADIUS)
        val hitIndestructible = explodeTerrain(affectedTiles, center, DESTRUCTION_RADIUS, world, particleSystem)

        if (hitIndestructible) {
            performFinalExplosion(player)
            deactivate(player)
            return
        }

        enemyManager.damageInRange(center, DESTRUCTION_RADIUS, DAMAGE_PER_TICK * deltaTime, bossShieldDamage)
    }

    private fun performFinalExplosion(player: Player) {
        val center = player.center
        val affectedTiles = collisionDetector.getTilesInRadius(center, FINAL_EXPLOSION_RADIUS)
        explodeTerrain(affectedTiles, center, FINAL_EXPLOSION_RADIUS, world, particleSystem)
        enemyManager.damageInRange(center, FINAL_EXPLOSION_RADIUS, FINAL_EXPLOSION_DAMAGE, bossShieldDamage)
        SoundManager.playOneShot(Sounds.EXPLODE)
    }

    override fun deactivate(player: Player) {
        isActive = false
        timer = 0f
        dismountRequested = false
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()
        val dirX = currentDirection.x
        val dirY = currentDirection.y
        val angle = angleTo(0f, 0f, dirX, dirY)

        // Exhaust trail behind the rocket
        for (i in 0 until 10) {
            val dist = 20f + i * 16f
            val flicker = sin((t + i * 5f) * 0.4f) * 5f
            val perpFlicker = cos((t + i * 3f) * 0.35f) * flicker
            val trailX = cx.x - dirX * dist + (-dirY) * perpFlicker
            val trailY = cx.y - dirY * dist + dirX * perpFlicker
            val trailAlpha = (0.7f - i * 0.06f).coerceAtLeast(0f)
            val trailSize = (12f - i * 0.8f).coerceAtLeast(2f)
            drawCircle(
                color = Color(1f, 0.4f + (1f - i / 10f) * 0.4f, 0f, trailAlpha),
                radius = trailSize,
                center = Offset(trailX, trailY),
            )
        }

        // Smoke puffs
        for (i in 0 until 5) {
            val dist = 60f + i * 24f
            val drift = sin((t + i * 11f) * 0.15f) * 10f
            val smokeX = cx.x - dirX * dist + (-dirY) * drift
            val smokeY = cx.y - dirY * dist + dirX * drift
            val smokeAlpha = (0.25f - i * 0.04f).coerceAtLeast(0f)
            drawCircle(
                color = Color(0.6f, 0.6f, 0.6f, smokeAlpha),
                radius = 8f + i * 2f,
                center = Offset(smokeX, smokeY),
            )
        }

        // Destruction radius aura
        val pulse = t.pulse(0.15f, 0.1f)
        drawCircle(
            color = AURA_FILL_COLOR,
            radius = DESTRUCTION_RADIUS * pulse,
            center = cx,
        )
        drawCircle(
            color = AURA_RING_COLOR,
            radius = DESTRUCTION_RADIUS,
            center = cx,
            style = Stroke(width = 1.5f),
        )

        // Rocket body (rotated oval)
        rotate(degrees = angle, pivot = cx) {
            drawOval(
                color = ROCKET_BODY_COLOR,
                topLeft = Offset(cx.x - 24f, cx.y - 10f),
                size = Size(48f, 20f),
            )
            // Fins
            drawRect(
                color = ROCKET_FIN_COLOR,
                topLeft = Offset(cx.x - 24f, cx.y - 14f),
                size = Size(10f, 28f),
            )
        }

        // Bright nose cone
        val noseX = cx.x + dirX * 24f
        val noseY = cx.y + dirY * 24f
        drawCircle(
            color = NOSE_COLOR,
            radius = 6f,
            center = Offset(noseX, noseY),
        )
        drawCircle(
            color = NOSE_CORE_COLOR,
            radius = 3f,
            center = Offset(noseX, noseY),
        )
    }

    companion object {
        private const val DURATION = 6f
        private const val FLY_SPEED = 900f
        private const val DESTRUCTION_RADIUS = 80f
        private const val DAMAGE_PER_TICK = 40f
        private const val FINAL_EXPLOSION_RADIUS = 400f
        private const val FINAL_EXPLOSION_DAMAGE = 500f
    }
}
