package com.glycin.koita.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.ui.BossHealthBar
import com.glycin.koita.ui.CollectibleCounter
import com.glycin.koita.ui.EnemyHealthBars
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HotkeyBar
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.TurretChargeIndicator
import com.glycin.koita.ui.pixelFont
import koita.composeapp.generated.resources.*

@Composable
fun UiRenderer(
    gameState: GameState,
    player: Player,
    camera: Camera,
    enemyManager: EnemyManager,
    mouse: Mouse,
) {
    val padding = 20f

    val worldTopScreen = camera.worldToScreen(0f, 0f).y
    val uiTopBound = worldTopScreen.coerceAtLeast(padding)

    val leftPadding = padding.dp
    val rightPadding = padding.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Health(
            currentHp = player.health,
            maxHp = player.maxHealth,
            offsetX = leftPadding,
            offsetY = uiTopBound.dp,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Text(
                text = "${gameState.score}",
                fontFamily = pixelFont(),
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = uiTopBound.dp)
            )
        }

        PickupNotification(
            gameState = gameState,
            offsetY = uiTopBound.dp + 150.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Notification(
            text = gameState.ultimateAvailable?.let { "Ultimate ready: $it (Press R)" },
            offsetY = uiTopBound.dp + 200.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            fadeInMs = 500,
            displayMs = 3000,
            fadeOutMs = 500,
        )

        Notification(
            text = if (gameState.passedPortal) "Are you ready to face the final void?" else null,
            offsetY = uiTopBound.dp + 100.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            fadeInMs = 500,
            displayMs = 1500,
            fadeOutMs = 500,
        )

        Text(
            text = formatTime(gameState.elapsedTimeSeconds),
            fontFamily = pixelFont(),
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -rightPadding, y = uiTopBound.dp)
        )

        HotkeyBar(
            offsetX = leftPadding,
            offsetY = (camera.canvasHeight - padding - 192f).dp,
            selectedIndex = gameState.selectedHotkeyIndex,
            items = listOf(
                Res.drawable.icon_pickaxe_unselected to Res.drawable.icon_pickaxe_selected,
                Res.drawable.icon_staff_unselected to Res.drawable.icon_staff_selected,
                Res.drawable.icon_hammer_unselected to Res.drawable.icon_hammer_selected,
            )
        )

        CollectibleCounter(
            collectableCount = gameState.collectedStones,
            offsetX = (camera.canvasWidth - padding - 100f).dp,
            offsetY = (camera.canvasHeight - padding - 30f).dp,
        )

        PlacementGhost(
            player = player,
            camera = camera,
        )

        TurretChargeIndicator(
            player = player,
            mouse = mouse,
        )

        EnemyHealthBars(
            enemyManager = enemyManager,
            player = player,
            camera = camera,
            gameState = gameState,
        )

        if (gameState.bossSpawned) {
            BossHealthBar(
                healthPercent = gameState.bossHealthPercent,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (gameState.isPaused) {
            PauseMenu(gameState)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    // Manual formatting instead of String.format since you need JVM for that...
    val minutesStr = if (minutes < 10) "0$minutes" else "$minutes"
    val secsStr = if (secs < 10) "0$secs" else "$secs"
    return "$minutesStr:$secsStr"
}