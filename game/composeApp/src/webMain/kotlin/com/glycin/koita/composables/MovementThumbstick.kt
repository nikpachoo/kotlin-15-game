package com.glycin.koita.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import com.glycin.koita.ui.Thumbstick
import kotlin.time.Clock

private const val MOVE_THRESHOLD = 0.3f
private const val DOUBLE_TAP_WINDOW_MS = 300L

private class MovementTapState {
    var lastLeftTapMs: Long = 0L
    var lastRightTapMs: Long = 0L
    var inLeftZone: Boolean = false
    var inRightZone: Boolean = false
}

@Composable
fun MovementThumbstick(
    input: Input,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
) {
    val tapState = remember { MovementTapState() }
    val keyMap = input.keyMap

    fun set(key: Key, value: Boolean) {
        if (keyMap[key] != value) keyMap[key] = value
    }

    Thumbstick(
        input = input,
        modifier = modifier,
        size = size,
        onMove = { normalized ->
            val inLeftZone = normalized.x < -MOVE_THRESHOLD
            val inRightZone = normalized.x > MOVE_THRESHOLD

            set(Key.A, inLeftZone)
            set(Key.D, inRightZone)
            set(Key.Spacebar, normalized.y < -MOVE_THRESHOLD)
            set(Key.S, normalized.y > MOVE_THRESHOLD)

            val leftRisingEdge = inLeftZone && !tapState.inLeftZone
            val rightRisingEdge = inRightZone && !tapState.inRightZone

            var dashFired = false
            if (leftRisingEdge) {
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - tapState.lastLeftTapMs <= DOUBLE_TAP_WINDOW_MS) {
                    dashFired = true
                    tapState.lastLeftTapMs = 0L
                } else {
                    tapState.lastLeftTapMs = now
                }
            }
            if (rightRisingEdge) {
                val now = Clock.System.now().toEpochMilliseconds()
                if (now - tapState.lastRightTapMs <= DOUBLE_TAP_WINDOW_MS) {
                    dashFired = true
                    tapState.lastRightTapMs = 0L
                } else {
                    tapState.lastRightTapMs = now
                }
            }

            if (dashFired) {
                set(Key.ShiftLeft, true)
            } else if (leftRisingEdge || rightRisingEdge || (!inLeftZone && !inRightZone)) {
                set(Key.ShiftLeft, false)
            }

            tapState.inLeftZone = inLeftZone
            tapState.inRightZone = inRightZone
        },
        onRelease = {
            set(Key.A, false)
            set(Key.D, false)
            set(Key.Spacebar, false)
            set(Key.S, false)
            set(Key.ShiftLeft, false)
            tapState.lastLeftTapMs = 0L
            tapState.lastRightTapMs = 0L
            tapState.inLeftZone = false
            tapState.inRightZone = false
        },
    )
}
