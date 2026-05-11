package com.glycin.koita.gameplay

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ModifierConfiguration {
    const val WEIGHT_NO_HEAL          = 2.50f
    const val WEIGHT_NO_SHRINES       = 3.00f
    const val WEIGHT_NO_PICKUPS       = 1.50f
    const val WEIGHT_NO_MINING_BOOST  = 1.00f
    const val WEIGHT_DOUBLE_ENEMIES   = 2.00f

    var noHeal by mutableStateOf(false)
    var noShrines by mutableStateOf(false)
    var noPickups by mutableStateOf(false)
    var noMiningBoost by mutableStateOf(false)
    var doubleEnemies by mutableStateOf(false)

    val scoreMultiplier: Float by derivedStateOf {
        var bonus = 0f
        if (noHeal)         bonus += WEIGHT_NO_HEAL
        if (noShrines)      bonus += WEIGHT_NO_SHRINES
        if (noPickups)      bonus += WEIGHT_NO_PICKUPS
        if (noMiningBoost)  bonus += WEIGHT_NO_MINING_BOOST
        if (doubleEnemies)  bonus += WEIGHT_DOUBLE_ENEMIES
        1f + bonus
    }
}
