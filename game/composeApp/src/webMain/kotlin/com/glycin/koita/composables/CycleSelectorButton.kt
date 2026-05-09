package com.glycin.koita.composables

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import com.glycin.koita.ui.HudButton
import com.glycin.koita.ui.pixelFont

@Composable
internal fun CycleSelectorButton(
    currentLabel: String,
    input: Input,
    onTap: () -> Unit,
) {
    HudButton(
        size = 44.dp,
        active = false,
        input = input,
        fillWidth = true,
        onTap = onTap,
    ) {
        Text(
            text = "$currentLabel  cycle >",
            fontFamily = pixelFont(),
            fontSize = 12.sp,
            color = Color.White,
        )
    }
}
