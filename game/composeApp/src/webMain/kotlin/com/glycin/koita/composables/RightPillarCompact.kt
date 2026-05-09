package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import com.glycin.koita.gameplay.modes.BuildBlock
import com.glycin.koita.ui.HudButton
import com.glycin.koita.ui.ResourceList
import com.glycin.koita.ui.ScoreReadout
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.util.nextAfter

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

        Box(modifier = Modifier.fillMaxWidth().height(COMPACT_ACTION_BOX_HEIGHT)) {
            AimThumbstick(
                player = player,
                camera = camera,
                input = input,
                modifier = Modifier.align(Alignment.CenterStart),
                size = COMPACT_THUMBSTICK_SIZE,
            )
            HOTKEY_MODES.forEachIndexed { index, mode ->
                val isEdge = index == 0 || index == HOTKEY_MODES.lastIndex
                val alignment = when (index) {
                    0 -> Alignment.TopEnd
                    HOTKEY_MODES.lastIndex -> Alignment.BottomEnd
                    else -> Alignment.CenterEnd
                }
                val offsetX = if (isEdge) -COMPACT_CHIP_INSET else 0.dp
                ModeChip(
                    label = mode.label,
                    selected = gameState.selectedHotkeyIndex == index,
                    input = input,
                    modifier = Modifier.align(alignment).offset(x = offsetX),
                    onTap = { player.equip(index) },
                )
            }
        }

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
private fun ModeChip(
    label: String,
    selected: Boolean,
    input: Input,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HudButton(
        size = COMPACT_SIDE_CHIP_SIZE,
        active = selected,
        input = input,
        modifier = modifier,
        onTap = onTap,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 11.sp,
            color = Color.White,
        )
    }
}
