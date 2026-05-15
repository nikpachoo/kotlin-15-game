package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.World
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val AURA_OUTER_YELLOW = Color(0xFFFFD835)
private val AURA_INNER_CREAM = Color(0xFFFFF4B5)
private val AURA_GLOW = Color(0x44FFAA00)
private val AURA_RING_COLOR = Color(0x33FFD700)
private val SHOCKWAVE_OUTER = Color(0xCCFF6600)
private val SHOCKWAVE_INNER = Color(0xCCFFEE66)

class SuperSaiyanDash(
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) : UltimateAttack(
    id = UltimateId.SUPER_SAIYAN_DASH,
    name = "Super Kodee Dash",
    requiredUnlockIds = setOf(UnlockId.DASH, UnlockId.LASER, UnlockId.BOUNCY_BLOCKS),
) {
    override val bossShieldDamage: Int = 1
    override val usesBoostAnimation: Boolean = true

    private var timer = 0f
    private var pinTimer = NOT_PINNED

    override fun activate(player: Player) {
        isActive = true
        timer = DURATION
        pinTimer = NOT_PINNED
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

        if (pinTimer >= 0f) {
            pinTimer += deltaTime
            if (pinTimer >= PIN_DURATION) {
                deactivate(player)
                return
            }
        } else {
            val affectedTiles = collisionDetector.getTilesInRadius(center, DESTRUCTION_RADIUS)
            val hitIndestructible = explodeTerrain(affectedTiles, center, DESTRUCTION_RADIUS, world, particleSystem)
            if (hitIndestructible) {
                pinTimer = 0f
                player.applyUltimateVelocity(Vec2.zero())
            }
        }

        enemyManager.damageInRange(center, DESTRUCTION_RADIUS, DAMAGE_PER_TICK * deltaTime, bossShieldDamage)
    }

    override fun deactivate(player: Player) {
        isActive = false
        timer = 0f
        pinTimer = NOT_PINNED
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()
        val elapsed = DURATION - timer

        drawAmbientGlow(cx, t)
        drawTrailLine(cx)
        drawAura(cx, t, scale = 1f, alpha = 1f, phaseOffset = 0f)
        drawDestructionRing(cx)
        if (elapsed < SHOCKWAVE_DURATION) {
            drawActivationShockwave(cx, elapsed / SHOCKWAVE_DURATION)
        }
    }

    private fun DrawScope.drawAmbientGlow(cx: Offset, t: Float) {
        val pulse = 0.92f + sin(t * 0.18f) * 0.08f
        drawOval(
            color = AURA_GLOW,
            topLeft = Offset(cx.x - 110f * pulse, cx.y - 130f * pulse),
            size = Size(220f * pulse, 260f * pulse),
        )
    }

    private fun DrawScope.drawTrailLine(cx: Offset) {
        drawLine(
            color = AURA_OUTER_YELLOW,
            start = cx,
            end = Offset(cx.x, cx.y + TRAIL_LENGTH),
            strokeWidth = TRAIL_WIDTH,
        )
    }

    private fun DrawScope.drawAura(
        center: Offset,
        t: Float,
        scale: Float,
        alpha: Float,
        phaseOffset: Float,
    ) {
        val outerHalfX = AURA_OUTER_HALF_X * scale
        val outerHalfY = AURA_OUTER_HALF_Y * scale
        val spikeLen = SPIKE_LENGTH * scale
        val spikeAmp = SPIKE_AMP * scale
        val midHalfX = AURA_MID_HALF_X * scale
        val midHalfY = AURA_MID_HALF_Y * scale
        val innerHalfX = AURA_INNER_HALF_X * scale
        val innerHalfY = AURA_INNER_HALF_Y * scale
        val coreHalfX = AURA_CORE_HALF_X * scale
        val coreHalfY = AURA_CORE_HALF_Y * scale

        val yellowPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addSpikeContour(this, center, outerHalfX, outerHalfY, spikeLen, spikeAmp, t, phaseOffset)
            addOvalContour(this, center, midHalfX, midHalfY)
        }
        drawPath(yellowPath, AURA_OUTER_YELLOW.copy(alpha = alpha))

        val creamPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addOvalContour(this, center, innerHalfX, innerHalfY)
            addOvalContour(this, center, coreHalfX, coreHalfY)
        }
        drawPath(creamPath, AURA_INNER_CREAM.copy(alpha = alpha * 0.85f))
    }

    private fun addSpikeContour(
        path: Path,
        center: Offset,
        halfX: Float,
        halfY: Float,
        spikeLen: Float,
        spikeAmp: Float,
        t: Float,
        phaseOffset: Float,
    ) {
        val total = SPIKE_COUNT * 2
        for (i in 0 until total) {
            val isTip = i % 2 == 0
            val angle = (i.toFloat() / total) * TWO_PI - HALF_PI
            val tipExtra = if (isTip) spikeLen + sin(t * 0.25f + i + phaseOffset) * spikeAmp else 0f
            val rx = halfX + tipExtra
            val ry = halfY + tipExtra
            val x = center.x + cos(angle) * rx
            val y = center.y + sin(angle) * ry
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }

    private fun addOvalContour(path: Path, center: Offset, halfX: Float, halfY: Float) {
        val steps = 24
        for (i in 0 until steps) {
            val angle = (i.toFloat() / steps) * TWO_PI - HALF_PI
            val x = center.x + cos(angle) * halfX
            val y = center.y + sin(angle) * halfY
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
    }

    private fun DrawScope.drawDestructionRing(cx: Offset) {
        drawCircle(
            color = AURA_RING_COLOR,
            radius = DESTRUCTION_RADIUS,
            center = cx,
            style = Stroke(width = 1.5f),
        )
    }

    private fun DrawScope.drawActivationShockwave(cx: Offset, t01: Float) {
        val eased = 1f - (1f - t01) * (1f - t01)
        val radius = 40f + eased * 260f
        val alpha = (1f - t01).coerceAtLeast(0f)
        drawCircle(
            color = SHOCKWAVE_OUTER.copy(alpha = alpha * 0.8f),
            radius = radius,
            center = cx,
            style = Stroke(width = 6f),
        )
        drawCircle(
            color = SHOCKWAVE_INNER.copy(alpha = alpha),
            radius = radius * 0.85f,
            center = cx,
            style = Stroke(width = 3f),
        )
    }

    companion object {
        private const val DURATION = 10f
        private const val DESTRUCTION_RADIUS = 125f
        private const val DAMAGE_PER_TICK = 50f
        private const val FLY_SPEED = 1200f
        private const val SHOCKWAVE_DURATION = 0.45f
        private const val PIN_DURATION = 1.5f
        private const val NOT_PINNED = -1f

        private const val SPIKE_COUNT = 18
        private const val AURA_OUTER_HALF_X = 60f
        private const val AURA_OUTER_HALF_Y = 75f
        private const val SPIKE_LENGTH = 16f
        private const val SPIKE_AMP = 5f
        private const val AURA_MID_HALF_X = 50f
        private const val AURA_MID_HALF_Y = 60f
        private const val AURA_INNER_HALF_X = 50f
        private const val AURA_INNER_HALF_Y = 60f
        private const val AURA_CORE_HALF_X = 40f
        private const val AURA_CORE_HALF_Y = 50f

        private const val TRAIL_LENGTH = 640f
        private const val TRAIL_WIDTH = 12f

        private const val HALF_PI = (PI / 2.0).toFloat()
    }
}
