package com.glycin.koita.world

import androidx.compose.ui.graphics.Color

enum class Tile(
    val color: Color,
    val isSolid: Boolean = true,
    val isIndestructible: Boolean = false,
    val isLiquid: Boolean = false,
    val isFragile: Boolean = false,
) {
    // Core
    AIR(Color.Transparent, false),

    // Deep Underground (bottom 20%)
    BEDROCK(Color(0xFF1a1a1a)),
    OBSIDIAN(Color(0xFF0f0820)),

    // Mid Underground (20-50%)
    DEEP_STONE(Color(0xFF3a3a3a)),
    COAL_ORE(Color(0xFF2b2b2b)),
    IRON_ORE(Color(0xFF8b7355)),

    // Upper Underground (50-70%)
    STONE(Color(0xFF808080)),
    DIRT(Color(0xFF654321)),
    CLAY(Color(0xFFa0826d)),
    GOLD_ORE(Color(0xFFffd700)),

    // Near Surface (70-90%)
    RICH_DIRT(Color(0xFF4a3728)),
    ROOTS(Color(0xFF5c4033)),
    SAND(Color(0xFFf4a460)),

    // Surface (90-100%)
    GRASS(Color(0xFF32cd32)),
    MOSS(Color(0xFF2d5016)),
    FLOWER(Color(0xFFff69b4)),
    WOOD(Color(0xFF8b4513)),
    LEAVES(Color(0xFF228b22)),

    // Liquids
    WATER(Color(0xFF4169e1), false, isLiquid = true),
    LAVA(Color(0xFFff4500), false, isLiquid = true),

    // Indestructible
    KOTLINIUM(Color(0xFF943BE5), true, true),

    // Special
    DYNAMITE(Color(0xFFFF1F1F), isFragile = true),
    SHIELD(Color(0xFFEFD500), false),
    WEB(Color(0xFFFFFFFF)),
    SLIME(Color(0xFF4CFF00), false, isFragile = true),
    PORTAL(Color(0xFF1a0a2e), false),
    BOUNCY(Color(0xFF00E5FF)),
}

fun Byte.toTile() = Tile.entries[this.toInt()]