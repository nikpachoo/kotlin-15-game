package com.glycin.model

import kotlinx.serialization.Serializable

@Serializable
data class HighscoreEntry(
    val name: String,
    val score: Int,
)