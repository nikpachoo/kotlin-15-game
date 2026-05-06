package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.modes.AttackWeapon
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.Health
import com.glycin.koita.ui.HudButton
import com.glycin.koita.ui.HudColors
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.util.nextAfter

@Composable
fun LeftPillarCompact(
    player: Player,
    input: Input,
    gameState: GameState,
    panelWidth: Dp,
    panelPadding: Dp,
    autoFire: Boolean,
    onToggleAutoFire: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(panelWidth)
            .fillMaxHeight()
            .padding(panelPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Health(currentHp = player.health, maxHp = player.maxHealth)

        Spacer(modifier = Modifier.weight(1f))

        HudButton(
            size = 44.dp,
            active = autoFire,
            input = input,
            fillWidth = true,
            onTap = onToggleAutoFire,
        ) {
            Text(
                text = "AUTO",
                fontFamily = pixelFont(),
                fontSize = 13.sp,
                color = Color.White,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ActionButton(
                label = "Left",
                keyHint = "A",
                key = Key.A,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
            )
            ActionButton(
                label = "Right",
                keyHint = "D",
                key = Key.D,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            val currentMode = HOTKEY_ENTRIES.firstOrNull { it.modeIndex == gameState.selectedHotkeyIndex }
            ActionButton(
                label = currentMode?.label ?: "Mode",
                keyHint = "",
                key = null,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
                onTap = { player.equip((gameState.selectedHotkeyIndex + 1) % HOTKEY_ENTRIES.size) },
            )
            ActionButton(
                label = "Heal",
                keyHint = "E",
                key = Key.E,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
                enabled = player.canHeal,
                cost = gameState.nextHealCost,
                costDotColor = HudColors.ORE_COLOR,
                onTap = { player.heal() },
            )
        }

        CycleSelectorButton(
            currentLabel = gameState.selectedWeapon.displayName,
            input = input,
            onTap = {
                gameState.selectedWeapon = AttackWeapon.availableFor(gameState).nextAfter(gameState.selectedWeapon)
            },
        )

        CycleSelectorButton(
            currentLabel = gameState.selectedBlock.displayName,
            input = input,
            onTap = {
                gameState.selectedBlock = BuildBlock.availableFor(gameState).nextAfter(gameState.selectedBlock)
            },
        )
    }
}

@Composable
private fun CycleSelectorButton(
    currentLabel: String,
    input: Input,
    onTap: () -> Unit,
) {
    HudButton(
        size = 44.dp,
        active = false,
        input = input,
        fillWidth = true,
        onTap = onTap,
    ) {
        Text(
            text = "$currentLabel  cycle >",
            fontFamily = pixelFont(),
            fontSize = 12.sp,
            color = Color.White,
        )
    }
}
