package com.glycin.koita.gameplay.modes

import com.glycin.koita.gameplay.GameState

enum class AttackWeapon(
    val displayName: String,
    val requires: (GameState) -> Boolean,
) {
    MISSILE("Missile", { true }),
    LASER("Laser", { it.laserWeapon }),
    ROCKET("Rocket", { it.rocketLauncher }),
    SOAKER("Soaker", { it.superSoaker }),
    SNIPER("Sniper", { it.sniperWeapon });

    companion object {
        fun availableFor(gameState: GameState): List<AttackWeapon> =
            entries.filter { it.requires(gameState) }
    }
}
