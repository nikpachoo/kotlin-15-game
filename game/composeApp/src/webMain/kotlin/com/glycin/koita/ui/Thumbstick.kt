package com.glycin.koita.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.core.Input
import kotlin.math.hypot
import kotlin.math.max

private const val TAP_TIMEOUT_MS = 250L
private const val TAP_SLOP_FRACTION = 0.25f

@Composable
fun Thumbstick(
    input: Input,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    onMove: (Offset) -> Unit = {},
    onRelease: () -> Unit = {},
    onTap: () -> Unit = {},
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val radiusPx = this.size.width / 2f
                val tapSlopPx = radiusPx * TAP_SLOP_FRACTION
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                try {
                    awaitPointerEventScope {
                        var activePointerId: PointerId? = null
                        var pressStartMs = 0L
                        var maxMovementPx = 0f
                        var didDrag = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = activePointerId?.let { id -> event.changes.firstOrNull { it.id == id } }
                                ?: event.changes.firstOrNull()
                                ?: continue
                            val raw = change.position - center
                            val len = hypot(raw.x, raw.y)
                            val clamped = if (len > radiusPx && len > 0f) raw * (radiusPx / len) else raw

                            if (change.pressed) {
                                if (!pressed) {
                                    pressed = true
                                    activePointerId = change.id
                                    input.acquireUiCapture()
                                    pressStartMs = change.uptimeMillis
                                    maxMovementPx = 0f
                                    didDrag = false
                                }
                                maxMovementPx = max(maxMovementPx, len)
                                if (!didDrag && maxMovementPx > tapSlopPx) didDrag = true

                                knobOffset = clamped
                                if (didDrag) {
                                    val normalized = Offset(clamped.x / radiusPx, clamped.y / radiusPx)
                                    onMove(normalized)
                                }
                                change.consume()
                            } else if (pressed) {
                                val elapsedMs = change.uptimeMillis - pressStartMs
                                pressed = false
                                activePointerId = null
                                knobOffset = Offset.Zero
                                input.releaseUiCapture()
                                if (didDrag) {
                                    onRelease()
                                } else if (elapsedMs <= TAP_TIMEOUT_MS) {
                                    onTap()
                                }
                                change.consume()
                            }
                        }
                    }
                } finally {
                    if (pressed) {
                        pressed = false
                        knobOffset = Offset.Zero
                        input.releaseUiCapture()
                    }
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.width / 2f
            val knobRadius = baseRadius * 0.42f

            drawCircle(
                color = HudColors.PANEL_BACKGROUND,
                radius = baseRadius,
                center = center,
            )
            drawCircle(
                color = HudColors.BUTTON_BORDER,
                radius = baseRadius,
                center = center,
                style = Stroke(width = 3f),
            )
            drawCircle(
                color = if (pressed) HudColors.BUTTON_ACTIVE else HudColors.BUTTON_IDLE,
                radius = knobRadius,
                center = center + knobOffset,
            )
            drawCircle(
                color = Color.White,
                radius = knobRadius,
                center = center + knobOffset,
                style = Stroke(width = 2f),
            )
        }
    }
}
