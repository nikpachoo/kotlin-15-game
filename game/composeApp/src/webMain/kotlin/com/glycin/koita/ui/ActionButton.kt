package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input

private val ACTION_BUTTON_SIZE = 64.dp
private const val DISABLED_ALPHA = 0.35f

@Composable
fun ActionButton(
    label: String,
    keyHint: String,
    key: Key,
    input: Input,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    cost: Int? = null,
    costDotColor: Color? = null,
    onTap: (() -> Unit)? = null,
) {
    val pressed = input.keyMap[key] == true
    val columnModifier = if (fillWidth) modifier.fillMaxWidth() else modifier
    Column(
        modifier = columnModifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = keyHint,
            fontFamily = pixelFont(),
            fontSize = 18.sp,
            color = Color.White,
        )
        HudButton(
            size = ACTION_BUTTON_SIZE,
            active = enabled && pressed,
            input = input,
            key = key,
            fillWidth = fillWidth,
            onPressChange = if (enabled && onTap == null) {
                { down -> input.keyMap[key] = down }
            } else null,
            onTap = onTap?.takeIf { enabled },
        ) {
            if (cost != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "$label (",
                        fontFamily = pixelFont(),
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                    if (costDotColor != null) {
                        Box(Modifier.size(8.dp).background(costDotColor, CircleShape))
                    }
                    Text(
                        text = "$cost)",
                        fontFamily = pixelFont(),
                        fontSize = 14.sp,
                        color = Color.White,
                    )
                }
            } else {
                Text(
                    text = label,
                    fontFamily = pixelFont(),
                    fontSize = 14.sp,
                    color = Color.White,
                )
            }
        }
    }
}
