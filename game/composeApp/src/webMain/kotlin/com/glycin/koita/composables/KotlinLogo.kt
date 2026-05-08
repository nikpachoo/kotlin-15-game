package com.glycin.koita.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp

@Composable
internal fun KotlinLogo(boxSize: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(boxSize)
            .drawWithCache {
                val logoPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height / 2f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                onDrawBehind { drawPath(logoPath, Color.White) }
            },
    )
}
