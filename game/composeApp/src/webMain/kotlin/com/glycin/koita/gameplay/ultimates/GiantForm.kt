package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.util.lerp
import com.glycin.koita.util.pulse

class GiantForm(
    private val gameState: GameState,
    private val enemyManager: EnemyManager,
) : UltimateAttack(
    id = "giant_form",
    name = "Giant Form",
    requiredUnlockIds = setOf("slow_fall", "super_soaker", "resource_shield"),
) {
    private var timer = 0f
    private var currentScale = 1f

    override fun activate(player: Player) {
        isActive = true
        timer = DURATION
        currentScale = 1f
    }

    override fun update(deltaTime: Float, player: Player) {
        if (!isActive) return

        timer -= deltaTime
        if (timer <= 0f) {
            deactivate(player)
            return
        }

        val growPhase = DURATION - timer
        val newScale = if (growPhase < GROW_TIME) {
            1f.lerp(TARGET_SCALE, growPhase / GROW_TIME)
        } else if (timer < SHRINK_TIME) {
            1f.lerp(TARGET_SCALE, timer / SHRINK_TIME)
        } else {
            TARGET_SCALE
        }

        if (newScale != currentScale) {
            currentScale = newScale
            player.giantScale = currentScale
            gameState.miningRadiusMultiplier = currentScale
        }

        if (currentScale < TARGET_SCALE * 0.5f) return

        val center = player.center
        val auraRadius = AURA_RADIUS * currentScale

        val enemies = enemyManager.getEnemiesInRange(center, auraRadius)
        for (i in enemies.indices) {
            enemies[i].takeDamage(DAMAGE_PER_SECOND * deltaTime)
        }
    }

    override fun deactivate(player: Player) {
        isActive = false
        timer = 0f
        currentScale = 1f
        player.giantScale = 1f
        gameState.miningRadiusMultiplier = 1f
    }

    override fun isFinished() = !isActive

    override fun DrawScope.render(camera: Camera, player: Player, frameCount: Long) {
        val cx = camera.worldToScreen(player.center.x, player.center.y)
        val t = frameCount.toFloat()
        val pulse = t.pulse(0.2f, 0.1f)
        val radius = AURA_RADIUS * currentScale * pulse

        drawCircle(
            color = Color(0x1800FFAA),
            radius = radius,
            center = cx,
        )

        drawCircle(
            color = Color(0x3300FFAA),
            radius = radius * 0.7f,
            center = cx,
        )

        drawCircle(
            color = Color(0x5500FFAA),
            radius = radius * 0.3f,
            center = cx,
        )
    }

    companion object {
        private const val DURATION = 15f
        private const val TARGET_SCALE = 4f
        private const val GROW_TIME = 1f
        private const val SHRINK_TIME = 1f
        private const val AURA_RADIUS = 40f
        private const val DAMAGE_PER_SECOND = 20f
    }
}
