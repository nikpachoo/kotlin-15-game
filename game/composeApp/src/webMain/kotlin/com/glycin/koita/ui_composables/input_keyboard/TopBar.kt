package com.glycin.koita.ui_composables.input_keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui_composables.info.Health

private val MENU_BUTTON_SIZE = 56.dp

@Composable
fun TopBar(
    player: Player,
    input: Input,
    gameState: GameState,
    panelPadding: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(panelPadding),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        KeyChipButton(
            label = "Menu",
            keyHint = "Esc",
            input = input,
            size = MENU_BUTTON_SIZE,
            onTap = { gameState.isPaused = !gameState.isPaused },
        )
        Health(
            currentHp = player.health,
            maxHp = player.maxHealth,
        )
    }
}
