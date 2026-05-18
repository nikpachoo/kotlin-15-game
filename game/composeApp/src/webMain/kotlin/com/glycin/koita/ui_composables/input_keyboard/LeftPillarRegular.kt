package com.glycin.koita.ui_composables.input_keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui_composables.ModeRow
import com.glycin.koita.ui_composables.input.PillarSide
import com.glycin.koita.ui_composables.input.pillarContainer

private val CHIP_SIZE = REGULAR_CHIP_SIZE
private val GAP = REGULAR_CHIP_GAP
private val GROUP_GAP = 12.dp

@Composable
fun LeftPillarRegular(
    player: Player,
    input: Input,
    gameState: GameState,
    panelWidth: Dp,
    panelPadding: Dp,
) {
    Column(
        modifier = Modifier.pillarContainer(input, PillarSide.LEFT, panelWidth, panelPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(GROUP_GAP),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        KeyboardCluster(input = input)

        KeyChipButton(
            label = "Jump",
            keyHint = "Space",
            input = input,
            key = Key.Spacebar,
            size = CHIP_SIZE,
            fillWidth = true,
        )

        ModeRow(player = player, input = input, gameState = gameState, size = CHIP_SIZE)
    }
}

@Composable
private fun KeyboardCluster(input: Input) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(GAP),
    ) {
        KeyChipButton(label = "↑", keyHint = "W", input = input, key = Key.W, size = CHIP_SIZE)
        Row(horizontalArrangement = Arrangement.spacedBy(GAP)) {
            KeyChipButton(label = "←", keyHint = "A", input = input, key = Key.A, size = CHIP_SIZE)
            KeyChipButton(label = "↓", keyHint = "S", input = input, key = Key.S, size = CHIP_SIZE)
            KeyChipButton(label = "→", keyHint = "D", input = input, key = Key.D, size = CHIP_SIZE)
        }
    }
}
