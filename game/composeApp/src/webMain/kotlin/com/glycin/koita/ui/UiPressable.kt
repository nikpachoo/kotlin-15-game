package com.glycin.koita.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import com.glycin.koita.core.Input

fun Modifier.uiPressable(
    input: Input,
    key: Any? = Unit,
    onPressChange: ((Boolean) -> Unit)? = null,
    onTap: (() -> Unit)? = null,
): Modifier = composed {
    val latestPressChange by rememberUpdatedState(onPressChange)
    val latestTap by rememberUpdatedState(onTap)
    pointerInput(key) {
        detectTapGestures(
            onPress = {
                input.acquireUiCapture()
                latestPressChange?.invoke(true)
                try {
                    tryAwaitRelease()
                } finally {
                    latestPressChange?.invoke(false)
                    input.releaseUiCapture()
                }
            },
            onTap = { latestTap?.invoke() },
        )
    }
}
