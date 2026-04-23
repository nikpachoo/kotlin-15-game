package com.glycin.koita.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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

private val HOTKEY_BUTTON_SIZE = 64.dp

data class HotkeyEntry(
    val keyHint: String,
    val label: String,
    val modeIndex: Int,
)

@Composable
fun HotkeyBar(
    entries: List<HotkeyEntry>,
    selectedModeIndex: Int,
    input: Input,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        entries.forEach { entry ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.keyHint,
                    fontFamily = pixelFont(),
                    fontSize = 18.sp,
                    color = Color.White,
                )
                HotkeyButton(
                    label = entry.label,
                    selected = selectedModeIndex == entry.modeIndex,
                    input = input,
                    key = entry.modeIndex,
                    onTap = { onSelect(entry.modeIndex) },
                )
            }
        }
    }
}

@Composable
private fun HotkeyButton(
    label: String,
    selected: Boolean,
    input: Input,
    key: Any,
    onTap: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    HudButton(
        size = HOTKEY_BUTTON_SIZE,
        active = selected || pressed,
        input = input,
        key = key,
        fillWidth = true,
        onPressChange = { pressed = it },
        onTap = onTap,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
        )
    }
}
