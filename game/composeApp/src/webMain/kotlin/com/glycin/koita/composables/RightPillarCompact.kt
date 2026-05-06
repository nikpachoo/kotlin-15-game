package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
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
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui.ActionButton
import com.glycin.koita.ui.CollectiblesPanel
import com.glycin.koita.ui.HudButton
import com.glycin.koita.ui.StatsPanel
import com.glycin.koita.ui.Thumbstick
import com.glycin.koita.ui.pixelFont

@Composable
fun BoxScope.RightPillarCompact(
    player: Player,
    input: Input,
    gameState: GameState,
    camera: Camera,
    panelWidth: Dp,
    panelPadding: Dp,
    autoFire: Boolean,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StatsPanel(
                score = gameState.score,
                elapsedSeconds = null,
                modifier = Modifier.weight(1f),
            )
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
        }

        CollectiblesPanel(
            minerals = gameState.collectedMinerals,
            simple = gameState.collectedSimple,
            rich = gameState.collectedRich,
        )

        Spacer(modifier = Modifier.weight(1f))

        ActionButton(
            label = "Jump",
            keyHint = "Space",
            key = Key.Spacebar,
            input = input,
        )

        Thumbstick(
            input = input,
            onMove = { normalized ->
                val worldX = player.center.x + normalized.x * AIM_RANGE
                val worldY = player.center.y + normalized.y * AIM_RANGE
                input.mouse.updatePosition(camera.worldToScreen(worldX, worldY), worldX, worldY)
                gameState.autoFireActive = autoFire
            },
            onRelease = {
                gameState.autoFireActive = false
            },
        )

        ActionButton(
            label = "Down",
            keyHint = "S",
            key = Key.S,
            input = input,
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ActionButton(
                label = "Dash",
                keyHint = "Shift",
                key = Key.ShiftLeft,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
                enabled = gameState.canDash,
            )
            ActionButton(
                label = "Ult",
                keyHint = "R",
                key = Key.R,
                input = input,
                modifier = Modifier.weight(1f),
                fillWidth = true,
                enabled = gameState.ultimateAvailable != null,
                onTap = { gameState.ultimateTriggered = true },
            )
        }
    }
}
