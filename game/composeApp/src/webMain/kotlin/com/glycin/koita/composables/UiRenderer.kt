package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
import com.glycin.koita.ui.KeyChipButton
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.UltimateBar
import com.glycin.koita.ui.UltimateUnlockedBanner

internal data class HotkeyMode(val label: String, val keyHint: String, val key: Key)

internal val HOTKEY_MODES = listOf(
    HotkeyMode("Mine", "1", Key.One),
    HotkeyMode("Attack", "2", Key.Two),
    HotkeyMode("Build", "3", Key.Three),
)

@Composable
internal fun ModeRow(
    player: Player,
    input: Input,
    gameState: GameState,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        HOTKEY_MODES.forEachIndexed { index, mode ->
            KeyChipButton(
                label = mode.label,
                keyHint = mode.keyHint,
                input = input,
                modifier = Modifier.weight(1f),
                key = mode.key,
                size = size,
                fillWidth = true,
                selected = gameState.selectedHotkeyIndex == index,
                onTap = { player.equip(index) },
            )
        }
    }
}

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
            LeftPillarCompact(
                player = player,
                input = input,
                gameState = gameState,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
            )
            RightPillarCompact(
                player = player,
                input = input,
                gameState = gameState,
                camera = camera,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
            )
        } else {
            LeftPillarRegular(
                player = player,
                input = input,
                gameState = gameState,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
            )
            RightPillarRegular(
                player = player,
                input = input,
                gameState = gameState,
                panelWidth = panelWidth,
                panelPadding = panelPadding,
            )
            TopBar(
                player = player,
                input = input,
                gameState = gameState,
                panelPadding = panelPadding,
            )
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
                bossName = gameState.bossName,
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

