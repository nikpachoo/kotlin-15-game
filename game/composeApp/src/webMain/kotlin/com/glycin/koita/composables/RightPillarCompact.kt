package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui.HudButton
import com.glycin.koita.ui.ResourceList
import com.glycin.koita.ui.ScoreReadout
import com.glycin.koita.ui.pixelFont

private val THUMBSTICK_SIZE = 100.dp
private val SIDE_CHIP_WIDTH = 60.dp
private val SIDE_CHIP_HEIGHT = 36.dp

@Composable
fun BoxScope.RightPillarCompact(
    player: Player,
    input: Input,
    gameState: GameState,
    camera: Camera,
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ScoreReadout(
            score = gameState.score,
            elapsedSeconds = gameState.elapsedTimeSeconds,
            modifier = Modifier.align(Alignment.End),
        )

        ResourceList(
            materials = gameState.collectedSimple,
            minerals = gameState.collectedMinerals,
            ore = gameState.collectedRich,
            modifier = Modifier.align(Alignment.End),
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(modifier = Modifier.fillMaxWidth().height(THUMBSTICK_SIZE)) {
            AimThumbstick(
                player = player,
                camera = camera,
                input = input,
                modifier = Modifier.align(Alignment.CenterStart),
                size = THUMBSTICK_SIZE,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(SIDE_CHIP_WIDTH)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                HOTKEY_MODES.forEachIndexed { index, mode ->
                    ModeChip(
                        label = mode.label,
                        selected = gameState.selectedHotkeyIndex == index,
                        input = input,
                        onTap = { player.equip(index) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ModeChip(
    label: String,
    selected: Boolean,
    input: Input,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HudButton(
        size = SIDE_CHIP_HEIGHT,
        active = selected,
        input = input,
        modifier = modifier,
        fillWidth = true,
        onTap = onTap,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 10.sp,
            color = Color.White,
        )
    }
}
