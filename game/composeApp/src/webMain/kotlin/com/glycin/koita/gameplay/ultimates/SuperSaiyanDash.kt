package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.util.pulse
import com.glycin.koita.world.World
import kotlin.math.sin

private val AURA_OUTER_COLOR = Color(0x22FFD700)
private val FLAME_COLOR = Color(0x44FFAA00)
private val GLOW_COLOR = Color(0xAAFFEE44)
private val CORE_COLOR = Color(0xFFFFFFCC)
private val AURA_RING_COLOR = Color(0x33FFD700)

class SuperSaiyanDash(
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : UltimateAttack(
    id = UltimateId.SUPER_SAIYAN_DASH,
    name = "Super Saiyan Dash",
    requiredUnlockIds = setOf(UnlockId.DASH, UnlockId.LASER, UnlockId.BOUNCY_BLOCKS),
) {
    override val bossShieldDamage: Int = 1

    private var timer = 0f

    override fun activate(player: Player) {
        isActive = true
        timer = DURATION
        player.applyUltimateVelocity(Vec2(0f, -FLY_SPEED))
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        timer -= deltaTime
        if (timer <= 0f) {
            deactivate(player)
            return
        }

        val center = player.center

        val affectedTiles = collisionDetector.getTilesInRadius(center, DESTRUCTION_RADIUS)
        val hitIndestructible = explodeTerrain(affectedTiles, center, DESTRUCTION_RADIUS, world, particleSystem)

        if (hitIndestructible) {
            deactivate(player)
            return
        }

        enemyManager.damageInRange(center, DESTRUCTION_RADIUS, DAMAGE_PER_TICK * deltaTime, bossShieldDamage)
    }

    override fun deactivate(player: Player) {
        isActive = false
        timer = 0f
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()

        for (i in 0 until 12) {
            val trailY = cx.y + 20f + i * 18f
            val flicker = sin((t + i * 7f) * 0.3f) * 6f
            val trailAlpha = (0.6f - i * 0.045f).coerceAtLeast(0f)
            val trailWidth = (14f - i * 0.8f).coerceAtLeast(2f)
            drawRect(
                color = Color(1f, 0.85f - i * 0.03f, 0f, trailAlpha),
                topLeft = Offset(cx.x - trailWidth / 2f + flicker, trailY),
                size = Size(trailWidth, 14f),
            )
        }

        val pulse = t.pulse(0.15f, 0.12f)
        drawCircle(
            color = AURA_OUTER_COLOR,
            radius = DESTRUCTION_RADIUS * pulse,
            center = cx,
        )

        val flameStretch = t.pulse(0.2f, 0.08f)
        drawOval(
            color = FLAME_COLOR,
            topLeft = Offset(cx.x - 40f, cx.y - 70f * flameStretch),
            size = Size(80f, 140f * flameStretch),
        )

        drawCircle(
            color = GLOW_COLOR,
            radius = 20f,
            center = cx,
        )
        drawCircle(
            color = CORE_COLOR,
            radius = 8f,
            center = cx,
        )

        drawCircle(
            color = AURA_RING_COLOR,
            radius = DESTRUCTION_RADIUS,
            center = cx,
            style = Stroke(width = 1.5f),
        )
    }

    companion object {
        private const val DURATION = 10f
        private const val DESTRUCTION_RADIUS = 125f
        private const val DAMAGE_PER_TICK = 50f
        private const val FLY_SPEED = 1200f
    }
}
