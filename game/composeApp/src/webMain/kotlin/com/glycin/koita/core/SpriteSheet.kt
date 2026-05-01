package com.glycin.koita.core

import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.jetbrains.compose.resources.DrawableResource

data class SpriteSheet(
    val sprite: DrawableResource,
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int,
) {
    fun frame(index: Int): SpriteFrame = SpriteFrame(this, index)
}

data class SpriteFrame(
    val sheet: SpriteSheet,
    val index: Int,
) {
    val srcOffset: IntOffset get() = IntOffset(
        (index % sheet.columns) * sheet.frameWidth,
        (index / sheet.columns) * sheet.frameHeight,
    )
    val srcSize: IntSize get() = IntSize(sheet.frameWidth, sheet.frameHeight)
}

fun DrawScope.drawSpriteFrame(
    image: ImageBitmap,
    frame: SpriteFrame,
    dstOffset: IntOffset,
    dstSize: IntSize,
    filterQuality: FilterQuality = FilterQuality.None,
) {
    drawImage(
        image = image,
        srcOffset = frame.srcOffset,
        srcSize = frame.srcSize,
        dstOffset = dstOffset,
        dstSize = dstSize,
        filterQuality = filterQuality,
    )
}
