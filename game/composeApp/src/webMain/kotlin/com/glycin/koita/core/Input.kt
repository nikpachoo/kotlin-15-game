package com.glycin.koita.core

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.input.key.Key

class Input(
    val keyMap: SnapshotStateMap<Key, Boolean> = mutableStateMapOf(),
    val mouse: Mouse = Mouse(),
) {
    private var captureCount: Int = 0
    val uiCapturing: Boolean
        get() = captureCount > 0

    fun acquireUiCapture() {
        captureCount++
    }

    fun releaseUiCapture() {
        if (captureCount > 0) captureCount--
    }
}
