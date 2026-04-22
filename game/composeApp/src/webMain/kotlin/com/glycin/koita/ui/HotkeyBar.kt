package com.glycin.koita.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.core.Input
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource

private val HOTKEY_BUTTON_SIZE = 64.dp

@Composable
fun HotkeyBar(
    selectedIndex: Int,
    spriteSheet: DrawableResource,
    frameIndices: IntArray,
    frameSize: Int,
    input: Input,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit = {},
) {
    val sheet = imageResource(spriteSheet)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        frameIndices.forEachIndexed { index, frame ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "${index + 1}",
                    fontFamily = pixelFont(),
                    fontSize = 18.sp,
                    color = Color.White,
                )
                HotkeyButton(
                    sheet = sheet,
                    frame = frame,
                    frameSize = frameSize,
                    selected = selectedIndex == index,
                    input = input,
                    key = index,
                    onTap = { onSelect(index) },
                )
            }
        }
    }
}

@Composable
private fun HotkeyButton(
    sheet: ImageBitmap,
    frame: Int,
    frameSize: Int,
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
        onPressChange = { pressed = it },
        onTap = onTap,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawImage(
                image = sheet,
                srcOffset = IntOffset(frame * frameSize, 0),
                srcSize = IntSize(frameSize, frameSize),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(size.width.toInt(), size.height.toInt()),
                filterQuality = FilterQuality.None,
            )
        }
    }
}
