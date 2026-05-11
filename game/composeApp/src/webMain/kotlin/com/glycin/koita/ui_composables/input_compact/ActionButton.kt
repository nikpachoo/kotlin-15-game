package com.glycin.koita.ui_composables.input_compact

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
import com.glycin.koita.ui_composables.input.HudButton
import com.glycin.koita.ui_composables.isCompact
import com.glycin.koita.ui_composables.pixelFont

private const val DISABLED_ALPHA = 0.35f

@Composable
fun ActionButton(
    label: String,
    keyHint: String,
    key: Key?,
    input: Input,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    cost: Int? = null,
    costDotColor: Color? = null,
    onTap: (() -> Unit)? = null,
    size: Dp? = null,
) {
    val compact = isCompact()
    val buttonSize = size ?: if (compact) 44.dp else 64.dp
    val keyHintSize = if (compact) 13.sp else 18.sp
    val labelSize = if (compact) 11.sp else 14.sp
    var localPressed by remember { mutableStateOf(false) }
    val pressed = localPressed || (key != null && input.keyMap[key] == true)
    val columnModifier = if (fillWidth) modifier.fillMaxWidth() else modifier
    Column(
        modifier = columnModifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
    ) {
        if (!compact) {
            Text(
                text = keyHint,
                fontFamily = pixelFont(),
                fontSize = keyHintSize,
                color = Color.White,
            )
        }
        HudButton(
            size = buttonSize,
            active = enabled && pressed,
            input = input,
            key = key ?: Unit,
            fillWidth = fillWidth,
            onPressChange = if (enabled) {
                { down ->
                    localPressed = down
                    if (onTap == null && key != null) input.keyMap[key] = down
                }
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
                        fontSize = labelSize,
                        color = Color.White,
                    )
                    if (costDotColor != null) {
                        Box(Modifier.size(if (compact) 6.dp else 8.dp).background(costDotColor, CircleShape))
                    }
                    Text(
                        text = "$cost)",
                        fontFamily = pixelFont(),
                        fontSize = labelSize,
                        color = Color.White,
                    )
                }
            } else {
                Text(
                    text = label,
                    fontFamily = pixelFont(),
                    fontSize = labelSize,
                    color = Color.White,
                )
            }
        }
    }
}
