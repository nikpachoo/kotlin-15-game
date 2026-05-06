package com.glycin.koita.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.ultimates.UltimateManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.ui.BossHealthBar
import com.glycin.koita.ui.EnemyHealthBars
import com.glycin.koita.ui.HotkeyEntry
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.UltimateBar
import com.glycin.koita.ui.UltimateUnlockedBanner

internal const val AIM_RANGE = 200f

internal val HOTKEY_ENTRIES = listOf(
    HotkeyEntry(keyHint = "3", label = "Build", modeIndex = 2),
    HotkeyEntry(keyHint = "2", label = "Attack", modeIndex = 1),
    HotkeyEntry(keyHint = "1", label = "Mine", modeIndex = 0),
)

@Composable
fun UiRenderer(
    gameState: GameState,
    player: Player,
    camera: Camera,
    enemyManager: EnemyManager,
    input: Input,
    upgradeRepository: UpgradeRepository,
) {
    val compact = isCompact()
    val panelWidth = with(LocalDensity.current) { camera.offsetX.toDp() }
    val panelPadding = if (compact) 6.dp else 16.dp

    Box(modifier = Modifier.fillMaxSize()) {
        if (compact) {
            var autoFire by remember { mutableStateOf(false) }
            LeftPillarCompact(
                player = player,
                input = input,
                gameState = gameState,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
                autoFire = autoFire,
                onToggleAutoFire = { autoFire = !autoFire },
            )
            RightPillarCompact(
                player = player,
                input = input,
                gameState = gameState,
                camera = camera,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
                autoFire = autoFire,
            )
        } else {
            LeftPillarRegular(player, input, gameState, panelWidth, panelPadding)
            RightPillarRegular(player, input, gameState, panelWidth, panelPadding)
        }

        PickupNotification(
            gameState = gameState,
            offsetY = 150.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        UltimateUnlockedBanner(
            text = gameState.ultimateBannerName,
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

        EnemyHealthBars(
            enemyManager = enemyManager,
            player = player,
            camera = camera,
            gameState = gameState,
        )

        if (gameState.bossSpawned) {
            BossHealthBar(
                healthPercent = gameState.bossHealthPercent,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (gameState.ultimateAvailable != null || gameState.ultimateCooldownRemaining > 0f) {
            UltimateBar(
                cooldownRemaining = gameState.ultimateCooldownRemaining,
                rechargeDuration = UltimateManager.ULTIMATE_RECHARGE_DURATION,
                ultimateName = gameState.ultimateAvailable,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        if (gameState.isPaused) {
            PauseMenu(gameState, upgradeRepository)
        }
    }
}

