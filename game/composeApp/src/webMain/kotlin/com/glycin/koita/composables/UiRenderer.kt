package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import com.glycin.koita.ui.WasdIndicator
import com.glycin.koita.ui.pixelFont
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.icon_hammer_selected
import koita.composeapp.generated.resources.icon_hammer_unselected
import koita.composeapp.generated.resources.icon_pickaxe_selected
import koita.composeapp.generated.resources.icon_pickaxe_unselected
import koita.composeapp.generated.resources.icon_staff_selected
import koita.composeapp.generated.resources.icon_staff_unselected

@Composable
fun UiRenderer(
    gameState: GameState,
    player: Player,
    camera: Camera,
    enemyManager: EnemyManager,
    mouse: Mouse,
) {
    val panelWidth = with(LocalDensity.current) { camera.offsetX.toDp() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Left panel
        Column(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Health(
                currentHp = player.health,
                maxHp = player.maxHealth,
            )

            Spacer(modifier = Modifier.weight(2f))

            WasdIndicator()

            Spacer(modifier = Modifier.weight(1f))

            HotkeyBar(
                selectedIndex = gameState.selectedHotkeyIndex,
                items = listOf(
                    Res.drawable.icon_pickaxe_unselected to Res.drawable.icon_pickaxe_selected,
                    Res.drawable.icon_staff_unselected to Res.drawable.icon_staff_selected,
                    Res.drawable.icon_hammer_unselected to Res.drawable.icon_hammer_selected,
                ),
            )
        }

        // Right panel
        Column(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "${gameState.score}",
                fontFamily = pixelFont(),
                fontSize = 18.sp,
                color = Color.White,
            )
            Text(
                text = formatTime(gameState.elapsedTimeSeconds),
                fontFamily = pixelFont(),
                fontSize = 12.sp,
                color = Color.LightGray,
            )

            Spacer(modifier = Modifier.weight(1f))

            CollectibleCounter(
                collectableCount = gameState.collectedStones,
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val weaponName = when (gameState.selectedHotkeyIndex) {
                    0 -> "Pickaxe"
                    1 -> "Staff"
                    2 -> "Hammer"
                    else -> ""
                }
                Text(
                    text = weaponName,
                    fontFamily = pixelFont(),
                    fontSize = 12.sp,
                    color = Color.White,
                )
                val ultimateText = gameState.ultimateAvailable
                if (ultimateText != null) {
                    Text(
                        text = "R: $ultimateText",
                        fontFamily = pixelFont(),
                        fontSize = 10.sp,
                        color = Color(0xFFCC44FF),
                    )
                }
            }
        }

        // In-game overlays
        PickupNotification(
            gameState = gameState,
            offsetY = 150.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Notification(
            text = gameState.ultimateAvailable?.let { "Ultimate ready: $it (Press R)" },
            offsetY = 200.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            fadeInMs = 500,
            displayMs = 3000,
            fadeOutMs = 500,
        )

        Notification(
            text = if (gameState.passedPortal) "Are you ready to face the final void?" else null,
            offsetY = 100.dp,
            modifier = Modifier.align(Alignment.TopCenter),
            fadeInMs = 500,
            displayMs = 1500,
            fadeOutMs = 500,
        )

        PlacementGhost(
            player = player,
            camera = camera,
        )

        TurretChargeIndicator(
            player = player,
            mouse = mouse,
            camera = camera,
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
    val minutesStr = if (minutes < 10) "0$minutes" else "$minutes"
    val secsStr = if (secs < 10) "0$secs" else "$secs"
    return "$minutesStr:$secsStr"
}
