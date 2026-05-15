package com.glycin.koita.ui_composables.input_compact

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui_composables.HOTKEY_MODES
import com.glycin.koita.ui_composables.info.ResourceList
import com.glycin.koita.ui_composables.info.ScoreReadout
import kotlin.math.PI
import kotlin.math.cos

private val ARC_GAP = 8.dp
private val ARC_RADIUS = COMPACT_THUMBSTICK_SIZE / 2 + COMPACT_SIDE_CHIP_SIZE / 2 + ARC_GAP
private val ARC_BOX_HEIGHT = COMPACT_THUMBSTICK_SIZE / 2 + ARC_RADIUS + COMPACT_SIDE_CHIP_SIZE / 2
private val ANCHOR_OFFSET = COMPACT_SIDE_CHIP_SIZE / 2 - COMPACT_THUMBSTICK_SIZE / 2
private val ARC_DIAGONAL = ARC_RADIUS * cos(PI / 4).toFloat()

private val CHIP_ARC_OFFSETS = listOf(
    DpOffset(ANCHOR_OFFSET, ANCHOR_OFFSET - ARC_RADIUS),
    DpOffset(ANCHOR_OFFSET - ARC_DIAGONAL, ANCHOR_OFFSET - ARC_DIAGONAL),
    DpOffset(ANCHOR_OFFSET - ARC_RADIUS, ANCHOR_OFFSET),
)

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
        horizontalAlignment = Alignment.End,
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ARC_BOX_HEIGHT),
        ) {
            AimThumbstick(
                player = player,
                camera = camera,
                input = input,
                modifier = Modifier.align(Alignment.BottomEnd),
                size = COMPACT_THUMBSTICK_SIZE,
            )
            HOTKEY_MODES.forEachIndexed { index, mode ->
                val offset = CHIP_ARC_OFFSETS[index]
                CompactChip(
                    label = mode.label,
                    input = input,
                    onTap = { player.equip(index) },
                    selected = gameState.selectedHotkeyIndex == index,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = offset.x, y = offset.y),
                )
            }
        }
    }
}
