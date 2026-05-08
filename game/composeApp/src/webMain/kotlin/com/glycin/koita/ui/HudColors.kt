package com.glycin.koita.ui

import androidx.compose.ui.graphics.Color

internal object HudColors {
    val BUTTON_IDLE = Color(0xFF2A2A3E)
    val BUTTON_ACTIVE = Color(0xFFFF5530)
    val BUTTON_BORDER = Color(0xFF6B30F9)
    val BUTTON_BORDER_HOVER = Color(0xFFFF5530)
    val BUTTON_BORDER_ACTIVE = Color(0xFFFF8A5C)

    val SCORE_PINK = Color(0xFFEB492D)

    val LIVES_PANEL_BACKGROUND = Color(0x33DB4161)
    val LIVES_TEXT_COLOR = Color(0xFFDB4161)

    val PANEL_BACKGROUND = Color(0xCC0A0A1F)
    val PANEL_BORDER = Color(0x66FFFFFF)
    val PANEL_ACCENT = Color(0xFFFFD977)

    val BANNER_GOLD_TOP = Color(0xFFFFE066)
    val BANNER_GOLD_MID = Color(0xFFFFA624)
    val BANNER_GOLD_BOTTOM = Color(0xFFCC5500)
    val BANNER_STRIPE = Color(0x44FF2200)
    val BANNER_OUTLINE = Color(0xFF1A0A00)
    val BANNER_GLOW = Color(0x66FFE066)
    val BANNER_TEXT_SHADOW = Color(0xCC1A0A00)
    val BANNER_LABEL = Color(0xFFFFEE44)

    val ULTIMATE_BAR_BACKGROUND = Color(0xFF1A0033)

    val MATERIALS_COLOR = Color(0xFF5D564E)
    val MINERALS_COLOR = Color(0xFF7B4D41)
    val ORE_COLOR = Color(0xFFFFD977)

    /**
     * Picks the chip border color for the standard idle / hover / active triple.
     * Hover only brightens when [enabled] is true; active always wins over hover.
     */
    fun chipBorder(active: Boolean, hovered: Boolean, enabled: Boolean = true): Color = when {
        active -> BUTTON_BORDER_ACTIVE
        hovered && enabled -> BUTTON_BORDER_HOVER
        else -> BUTTON_BORDER
    }
}
