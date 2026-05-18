package com.glycin.koita.ui_composables.input

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.HudColors

enum class PillarSide { LEFT, RIGHT }

fun Modifier.pillarFadeBackground(side: PillarSide): Modifier = composed {
    val brush = remember(side) {
        val opaque = HudColors.PANEL_BACKGROUND
        val transparent = opaque.copy(alpha = 0f)
        val colors = when (side) {
            PillarSide.LEFT -> listOf(opaque, transparent)
            PillarSide.RIGHT -> listOf(transparent, opaque)
        }
        Brush.horizontalGradient(colors)
    }
    background(brush)
}

fun Modifier.uiHoverCapture(input: Input): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    DisposableEffect(hovered) {
        if (hovered) {
            input.acquireUiCapture()
            onDispose { input.releaseUiCapture() }
        } else {
            onDispose { }
        }
    }
    hoverable(interactionSource)
}

fun Modifier.pillarContainer(
    input: Input,
    side: PillarSide,
    panelWidth: Dp,
    panelPadding: Dp,
): Modifier = this
    .width(panelWidth)
    .fillMaxHeight()
    .pillarFadeBackground(side)
    .uiHoverCapture(input)
    .padding(panelPadding)
