package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.util.pulse

private val CHARGE_AURA_COLOR = Color(0x4400AAFF)
private val INNER_GLOW_COLOR = Color(0xAA44CCFF)
private val BEAM_OUTER_COLOR = Color(0x220088FF)
private val BEAM_GLOW_COLOR = Color(0x660088FF)
private val BEAM_CORE_COLOR = Color(0xFFCCEEFF)
private val BEAM_END_GLOW_COLOR = Color(0x6600AAFF)

class Kamehameha(
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
    private val mouse: Mouse,
    private val camera: Camera,
) : UltimateAttack(
    id = UltimateId.KAMEHAMEHA,
    name = "Kotlihameha",
    requiredUnlockIds = setOf(UnlockId.GROUND_POUND, UnlockId.SNIPER, UnlockId.TURRET),
) {
    override val bossShieldDamage: Int = 2
    override val usesBoostAnimation: Boolean = true

    private var phase = Phase.INACTIVE
    private var chargeTimer = 0f
    private var beamTimer = 0f
    private var beamDirection = Vec2.zero()
    private var beamEndDistance = 0f

    private enum class Phase { INACTIVE, CHARGE, BEAM }

    override fun activate(player: Player) {
        isActive = true
        phase = Phase.CHARGE
        chargeTimer = CHARGE_DURATION
        player.applyUltimateVelocity(Vec2.zero())
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        when (phase) {
            Phase.CHARGE -> updateCharge(deltaTime)
            Phase.BEAM -> updateBeam(deltaTime, player)
            Phase.INACTIVE -> {}
        }
    }

    private fun updateCharge(deltaTime: Float) {
        chargeTimer -= deltaTime
        if (chargeTimer <= 0f) {
            phase = Phase.BEAM
            beamTimer = BEAM_DURATION
        }
    }

    private fun updateBeam(deltaTime: Float, player: Player) {
        beamTimer -= deltaTime
        if (beamTimer <= 0f) {
            deactivate(player)
            return
        }

        val origin = player.center
        val originX = origin.x
        val originY = origin.y
        val mouseWorld = camera.screenToWorld(mouse.position.x, mouse.position.y)
        beamDirection = (mouseWorld - origin).normalized()
        val dirX = beamDirection.x
        val dirY = beamDirection.y

        beamEndDistance = BEAM_RANGE
        var dist = 0f
        val tileSize = WorldConstants.TILE_SIZE

        while (dist < BEAM_RANGE) {
            val posX = originX + dirX * dist
            val posY = originY + dirY * dist

            val tileX = (posX / tileSize).toInt()
            val tileY = (posY / tileSize).toInt()

            if (tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
                tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES
            ) {
                beamEndDistance = dist
                break
            }

            if (world[tileX, tileY].isIndestructible) {
                beamEndDistance = dist
                break
            }

            val pos = Vec2(posX, posY)
            val affectedTiles = collisionDetector.getTilesInRadius(pos, EXPLOSION_RADIUS)
            explodeTerrain(affectedTiles, pos, EXPLOSION_RADIUS, world, particleSystem)

            dist += EXPLOSION_SPACING
        }

        enemyManager.damageInBeam(
            origin = origin,
            direction = beamDirection,
            length = beamEndDistance,
            width = BEAM_WIDTH,
            damage = BEAM_DAMAGE_PER_SECOND * deltaTime,
            shieldDamage = bossShieldDamage,
        )
    }

    override fun deactivate(player: Player) {
        isActive = false
        phase = Phase.INACTIVE
        chargeTimer = 0f
        beamTimer = 0f
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()

        when (phase) {
            Phase.CHARGE -> renderCharge(cx, t)
            Phase.BEAM -> renderBeam(camera, player, cx, t)
            Phase.INACTIVE -> {}
        }
    }

    private fun DrawScope.renderCharge(cx: Offset, t: Float) {
        val pulse = t.pulse(0.3f, 0.2f)
        drawCircle(
            color = CHARGE_AURA_COLOR,
            radius = 40f * pulse,
            center = cx,
        )
        drawCircle(
            color = INNER_GLOW_COLOR,
            radius = 14f,
            center = cx,
        )
    }

    private fun DrawScope.renderBeam(camera: Camera, player: Player, cx: Offset, t: Float) {
        val origin = player.center
        val endWorld = origin + beamDirection * beamEndDistance
        val endScreen = camera.worldToScreen(endWorld.x, endWorld.y)

        val pulse = t.pulse(0.25f, 0.15f)

        drawLine(
            color = BEAM_OUTER_COLOR,
            start = cx,
            end = endScreen,
            strokeWidth = 96f * pulse,
        )

        drawLine(
            color = BEAM_GLOW_COLOR,
            start = cx,
            end = endScreen,
            strokeWidth = 48f * pulse,
        )

        drawLine(
            color = INNER_GLOW_COLOR,
            start = cx,
            end = endScreen,
            strokeWidth = 24f * pulse,
        )

        drawLine(
            color = BEAM_CORE_COLOR,
            start = cx,
            end = endScreen,
            strokeWidth = 8f,
        )

        val chargePulse = t.pulse(0.2f, 0.3f)
        drawCircle(
            color = CHARGE_AURA_COLOR,
            radius = 60f * chargePulse,
            center = cx,
        )
        drawCircle(
            color = BEAM_CORE_COLOR,
            radius = 20f,
            center = cx,
        )

        drawCircle(
            color = BEAM_END_GLOW_COLOR,
            radius = 40f * pulse,
            center = endScreen,
        )
        drawCircle(
            color = INNER_GLOW_COLOR,
            radius = 16f,
            center = endScreen,
        )
    }

    companion object {
        private const val CHARGE_DURATION = 0.6f
        private const val BEAM_DURATION = 10f
        private const val BEAM_RANGE = 800f
        private const val BEAM_WIDTH = 48f
        private const val EXPLOSION_RADIUS = 30f
        private const val EXPLOSION_SPACING = 40f
        private const val BEAM_DAMAGE_PER_SECOND = 10f
    }
}
