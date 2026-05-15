package com.glycin.koita.ui_composables.input_compact

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
import com.glycin.koita.ui_composables.info.Health
import com.glycin.koita.ui_composables.input.HudButton
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.util.nextAfter

@Composable
fun LeftPillarCompact(
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            HudButton(
                size = 40.dp,
                active = false,
                input = input,
                onTap = { gameState.isPaused = !gameState.isPaused },
            ) {
                Text(
                    text = "ESC",
                    fontFamily = pixelFont(),
                    fontSize = 11.sp,
                    color = Color.White,
                )
            }
            Health(currentHp = player.health, maxHp = player.maxHealth)
        }

        Spacer(modifier = Modifier.weight(1f))

        CompactChip(
            label = gameState.selectedWeapon.displayName,
            input = input,
            onTap = {
                gameState.selectedWeapon = AttackWeapon.availableFor(gameState).nextAfter(gameState.selectedWeapon)
            },
            fontSize = 10.sp,
            fillWidth = true,
            trailing = ">",
        )
        CompactChip(
            label = gameState.selectedBlock.displayName,
            input = input,
            onTap = {
                gameState.selectedBlock = BuildBlock.availableFor(gameState).nextAfter(gameState.selectedBlock)
            },
            fontSize = 10.sp,
            fillWidth = true,
            trailing = ">",
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MovementThumbstick(
                input = input,
                size = COMPACT_THUMBSTICK_SIZE,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ActionButton(
                    label = "Heal",
                    keyHint = "E",
                    key = Key.E,
                    input = input,
                    enabled = player.canHeal,
                    size = COMPACT_SIDE_CHIP_SIZE,
                    onTap = { player.heal() },
                )
                ActionButton(
                    label = "Ult",
                    keyHint = "R",
                    key = Key.R,
                    input = input,
                    enabled = gameState.ultimateAvailable != null,
                    size = COMPACT_SIDE_CHIP_SIZE,
                    onTap = { gameState.ultimateTriggered = true },
                )
            }
        }
    }
}
