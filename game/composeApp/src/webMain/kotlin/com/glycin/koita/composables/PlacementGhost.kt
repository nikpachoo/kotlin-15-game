package com.glycin.koita.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.modes.BuildMode
import com.glycin.koita.world.WorldConstants

@Composable
fun PlacementGhost(
    player: Player,
    camera: Camera,
    input: Input,
) {
    if (input.uiCapturing) return

    val currentWeapon = player.currentWeapon

    if (currentWeapon !is BuildMode) return

    Canvas(modifier = Modifier.fillMaxSize()) {
        with(camera) { withVirtualViewport {
            currentWeapon.ghostTileX?.let { ghostX ->
                currentWeapon.ghostTileY?.let { ghostY ->
                    val ghostWorldX = ghostX * WorldConstants.TILE_SIZE.toFloat()
                    val ghostWorldY = ghostY * WorldConstants.TILE_SIZE.toFloat()
                    val ghostScreenPos = camera.worldToScreen(ghostWorldX, ghostWorldY)

                    val ghostColor = if (currentWeapon.isGhostValid) {
                        Color.Green.copy(alpha = 0.5f)
                    } else {
                        Color.Red.copy(alpha = 0.5f)
                    }

                    drawRect(
                        color = ghostColor,
                        topLeft = ghostScreenPos,
                        size = Size(
                            5f * WorldConstants.TILE_SIZE.toFloat(),
                            5f * WorldConstants.TILE_SIZE.toFloat(),
                        )
                    )
                }
            }
        } }
    }
}