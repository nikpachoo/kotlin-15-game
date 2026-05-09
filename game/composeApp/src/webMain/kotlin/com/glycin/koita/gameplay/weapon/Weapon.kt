package com.glycin.koita.gameplay.weapon

sealed interface Weapon {
    val isAlive: Boolean
    val bossShieldDamage: Int
}
