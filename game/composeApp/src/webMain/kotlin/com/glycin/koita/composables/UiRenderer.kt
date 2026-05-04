package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
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
import com.glycin.koita.gameplay.ultimates.UltimateManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.BossHealthBar
import com.glycin.koita.ui.Carousel
import com.glycin.koita.ui.CollectiblesPanel
import com.glycin.koita.ui.EnemyHealthBars
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HotkeyBar
import com.glycin.koita.ui.HudColors
import com.glycin.koita.ui.HotkeyEntry
import com.glycin.koita.ui.Notification
import com.glycin.koita.ui.PickupNotification
import com.glycin.koita.ui.PlacementGhost
import com.glycin.koita.ui.StatsPanel
import com.glycin.koita.ui.UltimateBar
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
    val compact = isCompact()
    val panelWidth = with(LocalDensity.current) { camera.offsetX.toDp() }
    val panelPadding = if (compact) 6.dp else 16.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Left panel
        Column(
            modifier = Modifier
                .width(panelWidth)
                .fillMaxHeight()
                .padding(panelPadding),
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
                .padding(panelPadding),
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

            ActionButtonGrid(
                actions = listOf(
                    ActionSpec("Ult", "R", Key.R, gameState.ultimateAvailable != null,
                        onTap = { gameState.ultimateTriggered = true }),
                    ActionSpec("Dash", "Shift", Key.ShiftLeft, gameState.canDash),
                    ActionSpec("Heal", "E", Key.E, player.canHeal,
                        cost = gameState.nextHealCost,
                        costDotColor = HudColors.ORE_COLOR,
                        onTap = { player.heal() }),
                    ActionSpec("Jump", "Space", Key.Spacebar),
                ),
                input = input,
                compact = compact,
            )

            Spacer(modifier = Modifier.weight(3f))
        }

        // In-game overlays
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

private data class ActionSpec(
    val label: String,
    val keyHint: String,
    val key: Key,
    val enabled: Boolean = true,
    val cost: Int? = null,
    val costDotColor: Color? = null,
    val onTap: (() -> Unit)? = null,
)

@Composable
private fun ActionButtonGrid(
    actions: List<ActionSpec>,
    input: Input,
    compact: Boolean,
) {
    if (compact) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            actions.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    row.forEach { it.Render(modifier = Modifier.weight(1f), input = input) }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            actions.forEach { it.Render(modifier = Modifier, input = input) }
        }
    }
}

@Composable
private fun ActionSpec.Render(modifier: Modifier, input: Input) {
    ActionButton(
        label = label,
        keyHint = keyHint,
        key = key,
        input = input,
        modifier = modifier,
        fillWidth = true,
        enabled = enabled,
        cost = cost,
        costDotColor = costDotColor,
        onTap = onTap,
    )
}
