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

@Composable
fun Thumbstick(
    input: Input,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    onMove: (Offset) -> Unit = {},
    onRelease: () -> Unit = {},
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .pointerInput(Unit) {
                val radiusPx = this.size.width / 2f
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                try {
                    awaitPointerEventScope {
                        var activePointerId: PointerId? = null
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
                                }
                                knobOffset = clamped
                                val normalized = Offset(clamped.x / radiusPx, clamped.y / radiusPx)
                                onMove(normalized)
                                change.consume()
                            } else if (pressed) {
                                pressed = false
                                activePointerId = null
                                knobOffset = Offset.Zero
                                input.releaseUiCapture()
                                onRelease()
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
