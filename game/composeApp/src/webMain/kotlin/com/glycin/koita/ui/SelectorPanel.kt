package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.util.nextAfter
import com.glycin.koita.util.prevBefore

private val PANEL_CORNER_RADIUS = 2.dp
private val PANEL_BORDER = 2.dp
private val PANEL_HEIGHT = 56.dp
private val ARROW_SIZE = 56.dp

@Composable
fun <T> SelectorPanel(
    headerLabel: String,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelOf: (T) -> String,
    input: Input,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val showArrows = items.size > 1
    val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
    val header = if (showArrows) "$headerLabel ${selectedIndex + 1}/${items.size}" else headerLabel
    val shape = remember { RoundedCornerShape(PANEL_CORNER_RADIUS) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .height(PANEL_HEIGHT)
                .background(HudColors.BUTTON_IDLE, shape)
                .border(PANEL_BORDER, HudColors.BUTTON_BORDER, shape)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = header,
                fontFamily = pixelFont(),
                fontSize = 11.sp,
                color = Color.LightGray,
            )
            Text(
                text = labelOf(selected),
                fontFamily = pixelFont(),
                fontSize = 16.sp,
                color = Color.White,
            )
        }
        if (showArrows) {
            ArrowChip(
                text = "<",
                input = input,
                key = "$headerLabel:prev",
                onTap = { onSelect(items.prevBefore(selected)) },
            )
            ArrowChip(
                text = ">",
                input = input,
                key = "$headerLabel:next",
                onTap = { onSelect(items.nextAfter(selected)) },
            )
        }
    }
}

@Composable
private fun ArrowChip(
    text: String,
    input: Input,
    key: Any,
    onTap: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val shape = remember { RoundedCornerShape(PANEL_CORNER_RADIUS) }
    val backgroundColor = if (pressed) HudColors.BUTTON_ACTIVE else HudColors.BUTTON_IDLE
    val borderColor = HudColors.chipBorder(active = pressed, hovered = hovered)
    Box(
        modifier = Modifier
            .size(ARROW_SIZE)
            .background(backgroundColor, shape)
            .border(PANEL_BORDER, borderColor, shape)
            .hoverable(interactionSource)
            .uiPressable(
                input = input,
                key = key,
                onPressChange = { pressed = it },
                onTap = onTap,
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
