package com.glycin.koita.ui_composables.input_compact

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player

private const val AIM_RANGE = 200f

@Composable
fun AimThumbstick(
    player: Player,
    camera: Camera,
    input: Input,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    Thumbstick(
        input = input,
        modifier = modifier,
        size = size,
        onMove = { normalized ->
            val worldX = player.center.x + normalized.x * AIM_RANGE
            val worldY = player.center.y + normalized.y * AIM_RANGE
            input.mouse.updatePosition(camera.worldToScreen(worldX, worldY), worldX, worldY)
            input.mouse.isLeftPressed = true
        },
        onRelease = {
            input.mouse.isLeftPressed = false
        },
    )
}
