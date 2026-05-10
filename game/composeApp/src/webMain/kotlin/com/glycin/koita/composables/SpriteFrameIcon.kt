package com.glycin.koita.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.glycin.koita.core.SpriteFrame
import com.glycin.koita.core.SpriteSheet
import com.glycin.koita.core.drawSpriteFrame
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.imageResource

@Composable
fun SpriteFrameIcon(icon: SpriteFrame, size: Dp) {
    val image = imageResource(icon.sheet.sprite)
    Canvas(modifier = Modifier.size(size)) {
        drawSpriteFrame(
            image = image,
            frame = icon,
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(this.size.width.toInt(), this.size.height.toInt()),
        )
    }
}

@Composable
fun SpriteAnimationIcon(
    sheet: SpriteSheet,
    frames: IntRange,
    frameDurationMs: Int,
    size: Dp,
) {
    var index by remember(sheet, frames) { mutableStateOf(frames.first) }
    LaunchedEffect(sheet, frames, frameDurationMs) {
        if (frames.first >= frames.last || frameDurationMs <= 0) return@LaunchedEffect
        while (true) {
            delay(frameDurationMs.toLong())
            index = if (index >= frames.last) frames.first else index + 1
        }
    }
    SpriteFrameIcon(icon = sheet.frame(index), size = size)
}
