package com.glycin.koita.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.glycin.koita.gameplay.GameState

@Composable
fun PickupNotification(
    gameState: GameState,
    offsetY: Dp,
    modifier: Modifier = Modifier,
) {
    Notification(
        text = gameState.pickupNotification,
        offsetY = offsetY,
        modifier = modifier,
        fadeInMs = 200,
        displayMs = 1200,
        fadeOutMs = 400,
        onDismiss = { gameState.pickupNotification = null },
    )
}
