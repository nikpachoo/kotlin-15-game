package com.glycin.model

import kotlinx.serialization.Serializable

@Serializable
data class HighscoresResponse(
    val top: List<HighscoreEntry>,
    val totalEntries: Long,
    val userEntry: HighscoreEntry? = null,
)
