package com.glycin.koita.world

import androidx.compose.ui.graphics.Color

enum class TileCategory {
    NONE,
    MINERALS,
    SIMPLE,
    RICH,
}

enum class Tile(
    val color: Color,
    val category: TileCategory,
    val isSolid: Boolean = true,
    val isIndestructible: Boolean = false,
    val isLiquid: Boolean = false,
    val isFragile: Boolean = false,
) {
    // Core
    AIR(Color.Transparent, TileCategory.NONE, false),

    // Deep Underground (bottom 20%)
    BEDROCK(Color(0xFF202022), TileCategory.SIMPLE),
    OBSIDIAN(Color(0xFF2D203C), TileCategory.MINERALS),

    // Mid Underground (20-50%)
    DEEP_STONE(Color(0xFF2F363B), TileCategory.SIMPLE),
    COAL_ORE(Color(0xFF191F22), TileCategory.MINERALS),
    IRON_ORE(Color(0xFF7B4D41), TileCategory.MINERALS),

    // Upper Underground (50-70%)
    STONE(Color(0xFF5D564E), TileCategory.SIMPLE),
    DIRT(Color(0xFF383830), TileCategory.SIMPLE),
    CLAY(Color(0xFF735B42), TileCategory.RICH),
    GOLD_ORE(Color(0xFFEEBF2F), TileCategory.RICH),

    // Near Surface (70-90%)
    RICH_DIRT(Color(0xFF362815), TileCategory.RICH),
    ROOTS(Color(0xFF503B28), TileCategory.RICH),
    SAND(Color(0xFFD3BE79), TileCategory.SIMPLE),

    // Surface (90-100%)
    GRASS(Color(0xFF8AA137), TileCategory.SIMPLE),
    MOSS(Color(0xFF476658), TileCategory.RICH),
    FLOWER(Color(0xFFF0C8DF), TileCategory.RICH),
    WOOD(Color(0xFF6E531E), TileCategory.SIMPLE),
    LEAVES(Color(0xFF579727), TileCategory.SIMPLE),

    // Liquids
    WATER(Color(0xFF7CD9E3), TileCategory.NONE, false, isLiquid = true),
    LAVA(Color(0xFFDE5A28), TileCategory.NONE, false, isLiquid = true),

    // Indestructible
    KOTLINIUM(Color(0xFF6B30F9), TileCategory.MINERALS, true, true),

    // Special
    DYNAMITE(Color(0xFFFF1F1F), TileCategory.NONE, isFragile = true),
    SHIELD(Color(0xFFEFD500), TileCategory.NONE, false),
    WEB(Color(0xFFFFFFFF), TileCategory.NONE),
    SLIME(Color(0xFF4CFF00), TileCategory.NONE, false, isFragile = true),
    PORTAL(Color(0xFF1a0a2e), TileCategory.NONE, false),
    BOUNCY(Color(0xFF00E5FF), TileCategory.NONE),
}

fun Byte.toTile() = Tile.entries[this.toInt()]
