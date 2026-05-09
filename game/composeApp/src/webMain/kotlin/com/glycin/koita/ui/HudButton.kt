package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glycin.koita.composables.isCompact
import com.glycin.koita.core.Input

@Composable
fun HudButton(
    size: Dp,
    active: Boolean,
    input: Input,
    key: Any = Unit,
    modifier: Modifier = Modifier,
    fillWidth: Boolean = false,
    onPressChange: ((Boolean) -> Unit)? = null,
    onTap: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val sizing = if (fillWidth) Modifier.fillMaxWidth().height(size) else Modifier.size(size)
    val shape: Shape = if (isCompact()) CircleShape else RectangleShape
    Box(
        modifier = modifier
            .then(sizing)
            .background(if (active) HudColors.BUTTON_ACTIVE else HudColors.BUTTON_IDLE, shape)
            .border(2.dp, HudColors.BUTTON_BORDER, shape)
            .uiPressable(
                input = input,
                key = key,
                onPressChange = onPressChange,
                onTap = onTap,
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
