package com.glycin.koita.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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

@Composable
fun <T> Carousel(
    label: String,
    items: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelOf: (T) -> String,
    input: Input,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val compact = isCompact()
    val size = items.size
    val current = items.indexOf(selected).takeIf { it >= 0 } ?: 0
    val currentItem = items[current]
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = if (compact) 8.sp else 10.sp,
            color = Color.LightGray,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarouselArrow(label, isPrev = true, input = input) {
                onSelect(items[(current - 1 + size) % size])
            }
            Box(
                modifier = Modifier
                    .width(if (compact) 88.dp else 120.dp)
                    .height(if (compact) 28.dp else 36.dp)
                    .hudPanel(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labelOf(currentItem),
                    fontFamily = pixelFont(),
                    fontSize = if (compact) 11.sp else 14.sp,
                    color = HudColors.PANEL_ACCENT,
                )
            }
            CarouselArrow(label, isPrev = false, input = input) {
                onSelect(items[(current + 1) % size])
            }
        }
    }
}

@Composable
private fun CarouselArrow(
    label: String,
    isPrev: Boolean,
    input: Input,
    onTap: () -> Unit,
) {
    val compact = isCompact()
    var pressed by remember { mutableStateOf(false) }
    HudButton(
        size = if (compact) 28.dp else 36.dp,
        active = pressed,
        input = input,
        key = label to isPrev,
        onPressChange = { pressed = it },
        onTap = onTap,
    ) {
        Text(
            text = if (isPrev) "<" else ">",
            fontFamily = pixelFont(),
            fontSize = if (compact) 16.sp else 20.sp,
            color = Color.White,
        )
    }
}
