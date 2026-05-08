package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun BackTab(boxSize: Dp, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = if (pressed) Color.White else MenuColors.SIDEBAR
    val arrowColor = if (pressed) Color.Black else Color.White
    val showBorder = hovered && !pressed

    Box(
        modifier = Modifier
            .size(boxSize)
            .background(backgroundColor)
            .then(if (showBorder) Modifier.border(2.dp, Color.White) else Modifier)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(compactOr(10.dp, 14.dp))
            .drawBehind {
                val w = size.width
                val h = size.height
                val arrowPath = Path().apply {
                    moveTo(0f, h * 0.5f)
                    lineTo(w * 0.45f, h * 0.1f)
                    lineTo(w * 0.45f, h * 0.35f)
                    lineTo(w, h * 0.35f)
                    lineTo(w, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.9f)
                    close()
                }
                drawPath(arrowPath, arrowColor)
            },
    )
}
