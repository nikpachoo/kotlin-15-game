package com.glycin.koita.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.isActive

private const val MIN_FRAME_NANOS = 16_666_666L // ~60 FPS
private const val MAX_CATCHUP_TICKS = 3L

@Composable
fun GameLoop(update: (deltaTime: Double) -> Unit) {
    LaunchedEffect(Unit) {
        var lastTime = 0L
        var accumulator = 0L
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                if (lastTime != 0L) {
                    val elapsed = frameTimeNanos - lastTime
                    accumulator = (accumulator + elapsed).coerceAtMost(MIN_FRAME_NANOS * MAX_CATCHUP_TICKS)
                    while (accumulator >= MIN_FRAME_NANOS) {
                        accumulator -= MIN_FRAME_NANOS
                        update(MIN_FRAME_NANOS / 1_000_000_000.0)
                    }
                }
                lastTime = frameTimeNanos
            }
        }
    }
}