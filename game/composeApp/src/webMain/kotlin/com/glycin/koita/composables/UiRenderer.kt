package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.DroneAnimator
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.BossHealthBar
import com.glycin.koita.ui.Carousel
import com.glycin.koita.ui.CollectibleCounter
import com.glycin.koita.ui.EnemyHealthBars
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HotkeyBar
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.StatsPanel
import com.glycin.koita.ui.VirtualDpad
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.drone_sheet

private val HOTKEY_FRAMES = intArrayOf(
    DroneAnimator.MINING_ICON_FRAME,
    DroneAnimator.ATTACK_ICON_FRAME,
    DroneAnimator.BUILD_ICON_FRAME,
)

@Composable
fun UiRenderer(
    gameState: GameState,
    player: Player,
    camera: Camera,
    enemyManager: EnemyManager,
    input: Input,
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

            Spacer(modifier = Modifier.weight(5f))

            VirtualDpad(input = input)

            Spacer(modifier = Modifier.weight(1f))

            HotkeyBar(
                selectedIndex = gameState.selectedHotkeyIndex,
                spriteSheet = Res.drawable.drone_sheet,
                frameIndices = HOTKEY_FRAMES,
                frameSize = DroneAnimator.FRAME_SIZE,
                input = input,
                onSelect = { player.equip(it) },
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
            StatsPanel(
                score = gameState.score,
                elapsedSeconds = gameState.elapsedTimeSeconds,
            )

            Spacer(modifier = Modifier.weight(1f))

            CollectibleCounter(
                collectableCount = gameState.collectedStones,
            )

            Spacer(modifier = Modifier.weight(5f))

            Carousel(
                label = "BLOCK",
                items = BuildBlock.availableFor(gameState),
                selected = gameState.selectedBlock,
                onSelect = { gameState.selectedBlock = it },
                labelOf = { it.displayName },
                input = input,
            )

            Spacer(modifier = Modifier.size(8.dp))

            Carousel(
                label = "WEAPON",
                items = AttackWeapon.availableFor(gameState),
                selected = gameState.selectedWeapon,
                onSelect = { gameState.selectedWeapon = it },
                labelOf = { it.displayName },
                input = input,
            )

            Spacer(modifier = Modifier.size(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    label = "Jump",
                    keyHint = "Space",
                    key = Key.Spacebar,
                    input = input,
                )
                ActionButton(
                    label = "Heal",
                    keyHint = "E",
                    key = Key.E,
                    input = input,
                )
                ActionButton(
                    label = "Ult",
                    keyHint = "R",
                    key = Key.R,
                    input = input,
                    enabled = gameState.ultimateAvailable != null,
                    onTap = { gameState.ultimateTriggered = true },
                )
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
