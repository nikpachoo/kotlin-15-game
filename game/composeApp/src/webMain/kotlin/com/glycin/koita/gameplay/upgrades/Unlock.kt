package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.core.SpriteFrame

class Unlock(
    val id: String,
    val name: String,
    val description: String,
    val icon: SpriteFrame,
    val onUnlock: () -> Unit,
)
