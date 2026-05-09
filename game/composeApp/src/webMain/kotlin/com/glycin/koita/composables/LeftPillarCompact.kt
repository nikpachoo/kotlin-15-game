package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val THUMBSTICK_SIZE = 100.dp
private val SIDE_BUTTON_WIDTH = 60.dp

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
        horizontalAlignment = Alignment.CenterHorizontally,
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

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth().height(THUMBSTICK_SIZE)) {
            MovementThumbstick(
                input = input,
                modifier = Modifier.align(Alignment.CenterEnd),
                size = THUMBSTICK_SIZE,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(SIDE_BUTTON_WIDTH)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                ActionButton(
                    label = "Heal",
                    keyHint = "E",
                    key = Key.E,
                    input = input,
                    fillWidth = true,
                    enabled = player.canHeal,
                    cost = gameState.nextHealCost,
                    costDotColor = HudColors.ORE_COLOR,
                    onTap = { player.heal() },
                )
                ActionButton(
                    label = "Ult",
                    keyHint = "R",
                    key = Key.R,
                    input = input,
                    fillWidth = true,
                    enabled = gameState.ultimateAvailable != null,
                    onTap = { gameState.ultimateTriggered = true },
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
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
