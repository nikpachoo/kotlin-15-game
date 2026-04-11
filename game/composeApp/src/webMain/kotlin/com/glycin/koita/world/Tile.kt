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
    BEDROCK(Color(0xFF202022)),
    OBSIDIAN(Color(0xFF2D203C)),

    // Mid Underground (20-50%)
    DEEP_STONE(Color(0xFF2F363B)),
    COAL_ORE(Color(0xFF191F22)),
    IRON_ORE(Color(0xFF7B4D41)),

    // Upper Underground (50-70%)
    STONE(Color(0xFF5D564E)),
    DIRT(Color(0xFF383830)),
    CLAY(Color(0xFF735B42)),
    GOLD_ORE(Color(0xFFEEBF2F)),

    // Near Surface (70-90%)
    RICH_DIRT(Color(0xFF362815)),
    ROOTS(Color(0xFF503B28)),
    SAND(Color(0xFFD3BE79)),

    // Surface (90-100%)
    GRASS(Color(0xFF8AA137)),
    MOSS(Color(0xFF476658)),
    FLOWER(Color(0xFFF0C8DF)),
    WOOD(Color(0xFF6E531E)),
    LEAVES(Color(0xFF579727)),

    // Liquids
    WATER(Color(0xFF7CD9E3), false, isLiquid = true),
    LAVA(Color(0xFFDE5A28), false, isLiquid = true),

    // Indestructible
    KOTLINIUM(Color(0xFF6B30F9), true, true),

    // Special
    DYNAMITE(Color(0xFFFF1F1F), isFragile = true),
    SHIELD(Color(0xFFEFD500), false),
    WEB(Color(0xFFFFFFFF)),
    SLIME(Color(0xFF4CFF00), false, isFragile = true),
    PORTAL(Color(0xFF1a0a2e), false),
    BOUNCY(Color(0xFF00E5FF)),
}

fun Byte.toTile() = Tile.entries[this.toInt()]