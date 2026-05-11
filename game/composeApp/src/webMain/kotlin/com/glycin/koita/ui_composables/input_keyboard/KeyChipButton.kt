package com.glycin.koita.ui_composables.input_keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.ui_composables.input.uiPressable

private val CHIP_CORNER_RADIUS = 2.dp
private val CHIP_BORDER = 2.dp
private val CHIP_PADDING_HORIZONTAL = 8.dp
private val CHIP_PADDING_VERTICAL = 6.dp
private const val DISABLED_ALPHA = 0.35f
private val LABEL_SIZE = 14.sp
private val KEY_HINT_SIZE = 11.sp
private val DOT_SIZE = 8.dp

@Composable
fun KeyChipButton(
    label: String,
    keyHint: String,
    input: Input,
    modifier: Modifier = Modifier,
    key: Key? = null,
    size: Dp = 64.dp,
    fillWidth: Boolean = false,
    enabled: Boolean = true,
    selected: Boolean = false,
    cost: Int? = null,
    costDotColor: Color? = null,
    onTap: (() -> Unit)? = null,
) {
    var localPressed by remember { mutableStateOf(false) }
    val held = localPressed || (key != null && input.keyMap[key] == true)
    val active = enabled && (selected || held)

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val shape = remember { RoundedCornerShape(CHIP_CORNER_RADIUS) }
    val backgroundColor = if (active) HudColors.BUTTON_ACTIVE else HudColors.BUTTON_IDLE
    val borderColor = HudColors.chipBorder(active = active, hovered = hovered, enabled = enabled)

    val sizing = if (fillWidth) Modifier.fillMaxWidth().height(size) else Modifier.size(size)
    Box(
        modifier = modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .then(sizing)
            .background(backgroundColor, shape)
            .border(CHIP_BORDER, borderColor, shape)
            .hoverable(interactionSource = interactionSource, enabled = enabled)
            .uiPressable(
                input = input,
                key = key ?: label,
                onPressChange = if (enabled) {
                    { down ->
                        localPressed = down
                        if (onTap == null && key != null) input.keyMap[key] = down
                    }
                } else null,
                onTap = onTap?.takeIf { enabled },
            )
            .padding(horizontal = CHIP_PADDING_HORIZONTAL, vertical = CHIP_PADDING_VERTICAL),
    ) {
        Text(
            text = keyHint,
            fontFamily = pixelFont(),
            fontSize = KEY_HINT_SIZE,
            color = Color.White,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = if (cost != null) "$label:" else label,
                fontFamily = pixelFont(),
                fontSize = LABEL_SIZE,
                color = Color.White,
            )
            if (cost != null) {
                if (costDotColor != null) {
                    Box(Modifier.size(DOT_SIZE).background(costDotColor, CircleShape))
                }
                Text(
                    text = "$cost",
                    fontFamily = pixelFont(),
                    fontSize = LABEL_SIZE,
                    color = Color.White,
                )
            }
        }
    }
}
