package com.glycin.koita.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.composables.isCompact
import com.glycin.koita.core.Input

@Composable
fun VirtualDpad(
    input: Input,
    modifier: Modifier = Modifier,
) {
    val compact = isCompact()
    val buttonSize = if (compact) 44.dp else 60.dp
    val gap = if (compact) 2.dp else 4.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        DpadButton("W", Key.W, input)
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            DpadButton("A", Key.A, input)
            Spacer(modifier = Modifier.size(buttonSize))
            DpadButton("D", Key.D, input)
        }
        DpadButton("S", Key.S, input)
    }
}

@Composable
private fun DpadButton(
    label: String,
    key: Key,
    input: Input,
) {
    val compact = isCompact()
    HudButton(
        size = if (compact) 44.dp else 60.dp,
        active = input.keyMap[key] == true,
        input = input,
        key = key,
        onPressChange = { down -> input.keyMap[key] = down },
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = if (compact) 14.sp else 18.sp,
            color = Color.White,
        )
    }
}
