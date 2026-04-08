package com.glycin.koita.gameplay.upgrades

class Unlock(
    val id: String,
    val name: String,
    val description: String,
    val onUnlock: () -> Unit,
)
