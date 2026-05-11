package com.glycin.koita.ui_composables.atlas

import com.glycin.koita.core.SpriteSheet

data class AtlasEntry(
    val sheet: SpriteSheet,
    val frames: IntRange,
    val frameDurationMs: Int,
    val name: String,
    val description: String,
)
