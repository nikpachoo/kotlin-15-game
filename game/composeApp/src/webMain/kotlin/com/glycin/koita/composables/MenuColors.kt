package com.glycin.koita.composables

import androidx.compose.ui.graphics.Color
import com.glycin.koita.gameplay.upgrades.UnlockGroup

internal object MenuColors {
    val BACKGROUND = Color(0xFF1A1A2E)
    val PAUSE_OVERLAY = Color(0x99000000)
    val SECTION_TITLE = Color(0xFFCCCCFF)
    val ERROR_TEXT = Color(0xFFFF6666)
    val SUCCESS_TEXT = Color(0xFF66FF99)
    val GAME_OVER_TITLE = Color(0xFFFF4D4D)
    val VICTORY_TITLE = Color(0xFFFFD700)
    val INPUT_BACKGROUND = Color(0xFF2A2A44)
    val RANK_GOLD = Color(0xFFFFD700)
    val RANK_SILVER = Color(0xFFC0C0C0)
    val RANK_BRONZE = Color(0xFFCD7F32)

    val SIDEBAR = Color(0xFFFF5530)
    val MAIN_BACKGROUND_DARK = Color(0xFF1A0A2E)
    val MAIN_BACKGROUND_MID = Color(0xFF4B0AC9)
    val MAIN_BACKGROUND_LIGHT = Color(0xFF6B30F9)

    val CARD_MOVEMENT_BORDER = Color(0xFF8C83FF)
    val CARD_BUILD_BORDER = Color(0xFF8CFF83)
    val CARD_WEAPON_BORDER = Color(0xFFFF8C63)

    fun cardBorder(group: UnlockGroup): Color = when (group) {
        UnlockGroup.MOVEMENT -> CARD_MOVEMENT_BORDER
        UnlockGroup.BUILD -> CARD_BUILD_BORDER
        UnlockGroup.WEAPON -> CARD_WEAPON_BORDER
    }
}
