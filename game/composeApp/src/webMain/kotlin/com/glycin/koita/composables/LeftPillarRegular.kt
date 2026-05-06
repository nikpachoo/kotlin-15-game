package com.glycin.koita.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HotkeyBar
import com.glycin.koita.ui.VirtualDpad

@Composable
fun LeftPillarRegular(
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
}
