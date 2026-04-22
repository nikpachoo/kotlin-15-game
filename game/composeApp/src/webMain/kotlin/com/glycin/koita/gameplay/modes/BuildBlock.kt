package com.glycin.koita.gameplay.modes

import com.glycin.koita.gameplay.GameState
import com.glycin.koita.world.Tile

enum class BuildBlock(
    val tile: Tile,
    val displayName: String,
    val requires: (GameState) -> Boolean,
) {
    STONE(Tile.STONE, "Stone", { true }),
    DYNAMITE(Tile.DYNAMITE, "Dynamite", { it.explosiveBlocks }),
    BOUNCY(Tile.BOUNCY, "Bouncy", { it.bouncyBlocks }),
    TURRET(Tile.KOTLINIUM, "Turret", { it.turretUnlocked });

    companion object {
        fun availableFor(gameState: GameState): List<BuildBlock> =
            entries.filter { it.requires(gameState) }
    }
}
