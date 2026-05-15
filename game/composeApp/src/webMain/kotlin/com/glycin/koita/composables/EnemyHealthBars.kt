package com.glycin.koita.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameSettings
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.ui_composables.HudColors

private val BAR_BACKGROUND_COLOR = Color(0xFF440000)
private val BAR_FILL_COLOR = HudColors.BRIGHT_GREEN

@Composable
fun EnemyHealthBars(
    enemyManager: EnemyManager,
    player: Player,
    camera: Camera,
    gameState: GameState,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        with(camera) { withVirtualViewport {
            val visionRange = (GameSettings.BASE_LIGHT_RADIUS * gameState.visionMultiplier) + GameSettings.FALL_OFF_DISTANCE
            enemyManager.getEnemiesInRange(player.center, visionRange).forEach { enemy ->
                val screenPos = camera.worldToScreen(enemy.position.x, enemy.position.y - (enemy.width / 2))
                val barWidth = 24f
                val barHeight = 4f
                val healthPercentage = enemy.health / enemy.maxHealth

                drawRect(
                    color = BAR_BACKGROUND_COLOR,
                    topLeft = Offset(screenPos.x, screenPos.y),
                    size = Size(barWidth, barHeight)
                )

                drawRect(
                    color = BAR_FILL_COLOR,
                    topLeft = Offset(screenPos.x, screenPos.y),
                    size = Size(barWidth * healthPercentage, barHeight)
                )

                drawRect(
                    color = Color.White,
                    topLeft = Offset(screenPos.x - 1f, screenPos.y - 1f),
                    size = Size(barWidth + 2f, barHeight + 2f),
                    style = Stroke(width = 1f)
                )
            }
        } }
    }
}
