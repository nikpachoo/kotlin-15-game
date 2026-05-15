package com.glycin.koita.ui_composables.input_compact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.input.HudButton
import com.glycin.koita.ui_composables.pixelFont

private val TRAILING_HORIZONTAL_PADDING = 20.dp

@Composable
internal fun CompactChip(
    label: String,
    input: Input,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    fontSize: TextUnit = 11.sp,
    fillWidth: Boolean = false,
    trailing: String? = null,
) {
    HudButton(
        size = COMPACT_SIDE_CHIP_SIZE,
        active = selected,
        input = input,
        modifier = modifier,
        fillWidth = fillWidth,
        onTap = onTap,
    ) {
        if (trailing == null) {
            ChipLabel(text = label, fontSize = fontSize)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TRAILING_HORIZONTAL_PADDING),
                contentAlignment = Alignment.Center,
            ) {
                ChipLabel(text = label, fontSize = fontSize)
                ChipLabel(
                    text = trailing,
                    fontSize = fontSize,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}

@Composable
private fun ChipLabel(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontFamily = pixelFont(),
        fontSize = fontSize,
        color = Color.White,
        modifier = modifier,
    )
}
