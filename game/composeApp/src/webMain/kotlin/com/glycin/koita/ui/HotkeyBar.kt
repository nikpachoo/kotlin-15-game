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
import com.glycin.koita.composables.isCompact
import com.glycin.koita.core.Input

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
    val compact = isCompact()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        entries.forEach { entry ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
            ) {
                if (!compact) {
                    Text(
                        text = entry.keyHint,
                        fontFamily = pixelFont(),
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                }
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
    val compact = isCompact()
    var pressed by remember { mutableStateOf(false) }
    HudButton(
        size = if (compact) 44.dp else 64.dp,
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
            fontSize = if (compact) 11.sp else 14.sp,
            color = Color.White,
        )
    }
}
