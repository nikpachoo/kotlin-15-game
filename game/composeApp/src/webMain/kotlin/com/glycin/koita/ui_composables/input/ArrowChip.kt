package com.glycin.koita.ui_composables.input

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.pixelFont

private val ARROW_CORNER_RADIUS = 2.dp
private val ARROW_BORDER = 2.dp
private const val DISABLED_ALPHA = 0.35f

@Composable
fun ArrowChip(
    text: String,
    input: Input,
    key: Any,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    enabled: Boolean = true,
) {
    var pressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = remember { RoundedCornerShape(ARROW_CORNER_RADIUS) }
    val active = enabled && pressed
    val backgroundColor = if (active) HudColors.BUTTON_ACTIVE else HudColors.BUTTON_IDLE
    val borderColor = HudColors.chipBorder(active = active, hovered = hovered, enabled = enabled)
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .size(size)
            .background(backgroundColor, shape)
            .border(ARROW_BORDER, borderColor, shape)
            .hoverable(interactionSource = interactionSource, enabled = enabled)
            .uiPressable(
                input = input,
                key = key,
                onPressChange = if (enabled) { down -> pressed = down } else null,
                onTap = if (enabled) onTap else null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = 20.sp,
            color = Color.White,
        )
    }
}
