package com.glycin.koita.ui_composables.input_keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.info.ResourceList
import com.glycin.koita.ui_composables.info.ScoreReadout

private val CHIP_SIZE = 56.dp
private val GROUP_GAP = 16.dp
private val ACTION_GAP = 8.dp

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
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(GROUP_GAP),
    ) {
        ScoreReadout(
            score = gameState.score,
            elapsedSeconds = gameState.elapsedTimeSeconds,
            heightMultiplier = gameState.scoreMultiplier,
        )

        ResourceList(
            materials = gameState.collectedSimple,
            minerals = gameState.collectedMinerals,
            ore = gameState.collectedRich,
        )

        Spacer(modifier = Modifier.weight(1f))

        SelectorPanel(
            headerLabel = "Block",
            items = BuildBlock.availableFor(gameState),
            selected = gameState.selectedBlock,
            onSelect = { gameState.selectedBlock = it },
            labelOf = { it.displayName },
            input = input,
            modifier = Modifier.fillMaxWidth(),
        )

        SelectorPanel(
            headerLabel = "Weapon",
            items = AttackWeapon.availableFor(gameState),
            selected = gameState.selectedWeapon,
            onSelect = { gameState.selectedWeapon = it },
            labelOf = { it.displayName },
            input = input,
            modifier = Modifier.fillMaxWidth(),
        )

        ActionStack(player = player, input = input, gameState = gameState)
    }
}

@Composable
private fun ActionStack(
    player: Player,
    input: Input,
    gameState: GameState,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ACTION_GAP),
    ) {
        KeyChipButton(
            label = "Ult",
            keyHint = "R",
            input = input,
            key = Key.R,
            size = CHIP_SIZE,
            fillWidth = true,
            enabled = gameState.ultimateAvailable != null,
            onTap = { gameState.ultimateTriggered = true },
        )
        KeyChipButton(
            label = "Dash",
            keyHint = "Shift",
            input = input,
            key = Key.ShiftLeft,
            size = CHIP_SIZE,
            fillWidth = true,
            enabled = gameState.canDash,
        )
        KeyChipButton(
            label = "Heal",
            keyHint = "E",
            input = input,
            key = Key.E,
            size = CHIP_SIZE,
            fillWidth = true,
            enabled = player.canHeal,
            cost = gameState.nextHealCost,
            costDotColor = HudColors.ORE_COLOR,
            onTap = { player.heal() },
        )
    }
}
