package com.glycin.koita.ui_composables.input_compact

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.input.HudButton
import com.glycin.koita.ui_composables.pixelFont

@Composable
internal fun CycleSelectorButton(
    currentLabel: String,
    input: Input,
    onTap: () -> Unit,
) {
    HudButton(
        size = COMPACT_SIDE_CHIP_SIZE,
        active = false,
        input = input,
        onTap = onTap,
    ) {
        Text(
            text = currentLabel,
            fontFamily = pixelFont(),
            fontSize = 10.sp,
            color = Color.White,
        )
    }
}
