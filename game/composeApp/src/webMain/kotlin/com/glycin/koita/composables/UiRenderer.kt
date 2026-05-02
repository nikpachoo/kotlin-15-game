package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.BossHealthBar
import com.glycin.koita.ui.Carousel
import com.glycin.koita.ui.CollectiblesPanel
import com.glycin.koita.ui.EnemyHealthBars
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HotkeyBar
import com.glycin.koita.ui.HotkeyEntry
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.StatsPanel
import com.glycin.koita.ui.UltimateUnlockedBanner
import com.glycin.koita.ui.VirtualDpad

private val HOTKEY_ENTRIES = listOf(
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

            VirtualDpad(input = input)

            Spacer(modifier = Modifier.weight(2.5f))

            HotkeyBar(
                entries = HOTKEY_ENTRIES,
                selectedModeIndex = gameState.selectedHotkeyIndex,
                input = input,
                modifier = Modifier.fillMaxWidth(),
                onSelect = { player.equip(it) },
            )

            Spacer(modifier = Modifier.weight(1.5f))
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

            CollectiblesPanel(
                minerals = gameState.collectedMinerals,
                simple = gameState.collectedSimple,
                rich = gameState.collectedRich,
            )

            Spacer(modifier = Modifier.weight(2f))

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ActionButton(
                    label = "Ult",
                    keyHint = "R",
                    key = Key.R,
                    input = input,
                    fillWidth = true,
                    enabled = gameState.ultimateAvailable != null,
                    onTap = { gameState.ultimateTriggered = true },
                )
                ActionButton(
                    label = "Dash",
                    keyHint = "Shift",
                    key = Key.ShiftLeft,
                    input = input,
                    fillWidth = true,
                    enabled = gameState.canDash,
                )
                ActionButton(
                    label = "Heal (${gameState.nextHealCost})",
                    keyHint = "E",
                    key = Key.E,
                    input = input,
                    fillWidth = true,
                    enabled = player.canHeal,
                    onTap = { player.heal() },
                )
                ActionButton(
                    label = "Jump",
                    keyHint = "Space",
                    key = Key.Spacebar,
                    input = input,
                    fillWidth = true,
                )
            }

            Spacer(modifier = Modifier.weight(3f))
        }

        // In-game overlays
        PickupNotification(
            gameState = gameState,
            offsetY = 150.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        UltimateUnlockedBanner(
            text = gameState.ultimateAvailable,
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

        if (gameState.isPaused) {
            PauseMenu(gameState, upgradeRepository)
        }
    }
}
