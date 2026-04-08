package com.glycin.koita.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive

@Composable
fun GameLoop(update: (deltaTime: Double) -> Unit) {
    LaunchedEffect(Unit) {
        var lastTime = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastTime != 0L) {
                    val deltaNanos = frameTimeNanos - lastTime
                    val deltaTimeSeconds = deltaNanos / 1_000_000_000.0
                    update(deltaTimeSeconds)
                }
                lastTime = frameTimeNanos
            }
        }
    }
}