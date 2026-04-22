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
import com.glycin.koita.core.Input

private val DPAD_BUTTON_SIZE = 60.dp

@Composable
fun VirtualDpad(
    input: Input,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DpadButton("W", Key.W, input)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DpadButton("A", Key.A, input)
            Spacer(modifier = Modifier.size(DPAD_BUTTON_SIZE))
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
    HudButton(
        size = DPAD_BUTTON_SIZE,
        active = input.keyMap[key] == true,
        input = input,
        key = key,
        onPressChange = { down -> input.keyMap[key] = down },
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 18.sp,
            color = Color.White,
        )
    }
}
