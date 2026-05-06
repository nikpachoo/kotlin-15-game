package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.Carousel
import com.glycin.koita.ui.CollectiblesPanel
import com.glycin.koita.ui.HudColors
import com.glycin.koita.ui.StatsPanel

@Composable
fun BoxScope.RightPillarRegular(
    player: Player,
    input: Input,
    gameState: GameState,
    panelWidth: Dp,
    panelPadding: Dp,
) {
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
        )

        Spacer(modifier = Modifier.weight(3f))
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
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        actions.forEach { it.Render(input = input) }
    }
}

@Composable
private fun ActionSpec.Render(input: Input) {
    ActionButton(
        label = label,
        keyHint = keyHint,
        key = key,
        input = input,
        fillWidth = true,
        enabled = enabled,
        cost = cost,
        costDotColor = costDotColor,
        onTap = onTap,
    )
}
