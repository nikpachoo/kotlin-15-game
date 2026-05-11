package com.glycin.koita.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.pixelFont

@Composable
fun Notification(
    text: String?,
    offsetY: Dp,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 32.sp,
    fadeInMs: Int = 1500,
    displayMs: Int = 5000,
    fadeOutMs: Int = 1500,
    onDismiss: (() -> Unit)? = null,
) {
    val alpha = remember { Animatable(0f) }
    val displayText = remember { mutableListOf("") }

    LaunchedEffect(text) {
        if (text == null) return@LaunchedEffect
        displayText[0] = text
        alpha.snapTo(0f)
        alpha.animateTo(1f, animationSpec = tween(fadeInMs))
        alpha.animateTo(1f, animationSpec = tween(displayMs))
        alpha.animateTo(0f, animationSpec = tween(fadeOutMs))
        onDismiss?.invoke()
    }

    if (alpha.value > 0f) {
        Text(
            text = displayText[0],
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
            modifier = modifier
                .offset(y = offsetY)
                .alpha(alpha.value)
        )
    }
}
