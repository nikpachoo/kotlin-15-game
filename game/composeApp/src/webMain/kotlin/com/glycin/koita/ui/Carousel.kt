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
import com.glycin.koita.core.Input

private val ARROW_BUTTON_SIZE = 36.dp
private val DISPLAY_WIDTH = 120.dp
private val DISPLAY_HEIGHT = 36.dp

@Composable
fun Carousel(
    label: String,
    items: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    input: Input,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return
    val size = items.size
    val current = selectedIndex.coerceIn(0, size - 1)
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 10.sp,
            color = Color.LightGray,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CarouselArrow(label, isPrev = true, input = input) {
                onSelect((current - 1 + size) % size)
            }
            Box(
                modifier = Modifier
                    .width(DISPLAY_WIDTH)
                    .height(DISPLAY_HEIGHT)
                    .hudPanel(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = items[current],
                    fontFamily = pixelFont(),
                    fontSize = 14.sp,
                    color = HudColors.PANEL_ACCENT,
                )
            }
            CarouselArrow(label, isPrev = false, input = input) {
                onSelect((current + 1) % size)
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
    var pressed by remember { mutableStateOf(false) }
    HudButton(
        size = ARROW_BUTTON_SIZE,
        active = pressed,
        input = input,
        key = label to isPrev,
        onPressChange = { pressed = it },
        onTap = onTap,
    ) {
        Text(
            text = if (isPrev) "<" else ">",
            fontFamily = pixelFont(),
            fontSize = 20.sp,
            color = Color.White,
        )
    }
}
