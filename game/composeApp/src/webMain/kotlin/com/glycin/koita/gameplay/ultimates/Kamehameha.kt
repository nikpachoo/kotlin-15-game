package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.core.PlayerSettings
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.math.abs
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
    id = "kamehameha",
    name = "Kamehameha",
    requiredUnlockIds = setOf("ground_pound", "sniper", "turret"),
) {
    private var phase = Phase.INACTIVE
    private var beamTimer = 0f
    private var beamDirection = Vec2.zero
    private var beamEndDistance = 0f

    private enum class Phase { INACTIVE, GROUND_POUND, BEAM }

    override fun activate(player: Player) {
        isActive = true
        phase = Phase.GROUND_POUND
        player.applyUltimateVelocity(Vec2(0f, GROUND_POUND_SPEED))
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        when (phase) {
            Phase.GROUND_POUND -> updateGroundPound(player)
            Phase.BEAM -> updateBeam(deltaTime, player)
            Phase.INACTIVE -> {}
        }
    }

    private fun updateGroundPound(player: Player) {
        val feetX = player.position.x + player.width / 2f
        val feetY = player.position.y + player.height + 2f

        if (!collisionDetector.isSolidAtPosition(feetX, feetY)) return

        val impactPoint = Vec2(feetX, player.position.y + player.height)
        val affectedTiles = collisionDetector.getTilesInRadius(impactPoint, GROUND_POUND_RADIUS)
        SoundManager.playOneShot(Sounds.EXPLODE)
        explodeTerrain(affectedTiles, impactPoint, GROUND_POUND_RADIUS, world, particleSystem)

        enemyManager.getEnemiesInRange(impactPoint, GROUND_POUND_RADIUS).forEach { enemy ->
            enemy.takeDamage(GROUND_POUND_DAMAGE)
        }

        phase = Phase.BEAM
        beamTimer = BEAM_DURATION
        player.applyUltimateVelocity(Vec2.zero)
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

        val enemies = enemyManager.getEnemiesInRange(origin, beamEndDistance)
        for (i in enemies.indices) {
            val enemy = enemies[i]
            val toEnemyX = enemy.center.x - originX
            val toEnemyY = enemy.center.y - originY
            val proj = toEnemyX * dirX + toEnemyY * dirY
            if (proj > 0f && proj < beamEndDistance) {
                val perpDist = abs(toEnemyX * dirY - toEnemyY * dirX)
                if (perpDist < BEAM_WIDTH) {
                    enemy.takeDamage(BEAM_DAMAGE_PER_SECOND * deltaTime)
                }
            }
        }
    }

    override fun deactivate(player: Player) {
        isActive = false
        phase = Phase.INACTIVE
        beamTimer = 0f
        player.clearUltimateVelocity()
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()

        when (phase) {
            Phase.GROUND_POUND -> renderGroundPound(cx, t)
            Phase.BEAM -> renderBeam(camera, player, cx, t)
            Phase.INACTIVE -> {}
        }
    }

    private fun DrawScope.renderGroundPound(cx: Offset, t: Float) {
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
        private const val GROUND_POUND_SPEED = PlayerSettings.GROUND_POUND_SPEED
        private const val GROUND_POUND_RADIUS = PlayerSettings.GROUND_POUND_RADIUS * 2f
        private const val GROUND_POUND_DAMAGE = PlayerSettings.GROUND_POUND_DAMAGE * 2f
        private const val BEAM_DURATION = 10f
        private const val BEAM_RANGE = 800f
        private const val BEAM_WIDTH = 48f
        private const val EXPLOSION_RADIUS = 30f
        private const val EXPLOSION_SPACING = 40f
        private const val BEAM_DAMAGE_PER_SECOND = 30f
    }
}
