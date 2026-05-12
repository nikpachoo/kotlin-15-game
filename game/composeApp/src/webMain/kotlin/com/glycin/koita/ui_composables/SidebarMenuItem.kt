package com.glycin.koita.ui_composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private const val HOVER_ANIMATION_MS = 300

@Composable
internal fun SidebarMenuItem(
    text: String,
    fontSize: TextUnit,
    verticalPadding: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val active = hovered || focused
    val hoverOffset by animateDpAsState(
        targetValue = if (active) compactOr(12.dp, 20.dp) else 0.dp,
        animationSpec = tween(durationMillis = HOVER_ANIMATION_MS, easing = EaseOut),
    )
    val textColor by animateColorAsState(
        targetValue = if (active) Color.White else Color.Black,
        animationSpec = tween(durationMillis = HOVER_ANIMATION_MS, easing = EaseOut),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier
                .offset(x = hoverOffset)
                .padding(
                    horizontal = compactOr(24.dp, 48.dp),
                    vertical = verticalPadding,
                ),
        )
    }
}
