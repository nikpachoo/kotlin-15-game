package com.glycin.koita.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.glycin.koita.core.Input

fun Modifier.uiPressable(
    input: Input,
    key: Any? = Unit,
    onPressChange: ((Boolean) -> Unit)? = null,
    onTap: (() -> Unit)? = null,
): Modifier = pointerInput(key) {
    detectTapGestures(
        onPress = {
            input.acquireUiCapture()
            onPressChange?.invoke(true)
            try {
                tryAwaitRelease()
            } finally {
                onPressChange?.invoke(false)
                input.releaseUiCapture()
            }
        },
        onTap = onTap?.let { tap -> { tap() } },
    )
}
