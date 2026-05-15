package com.glycin.koita.composables

import androidx.compose.ui.graphics.Color
import com.glycin.koita.gameplay.upgrades.UnlockGroup
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.world.Tile

internal object WorldRendererColors {
    val CONFUSER_TILE = Color(0xFFAA44FF)
    val ENEMY_MISSILE = Color(0xFFFFC118)

    val ORB_MOVEMENT_OUTER = Color(0x446C63FF)
    val ORB_MOVEMENT_INNER = MenuColors.CARD_MOVEMENT_BORDER
    val ORB_BUILD_OUTER = Color(0x446CFF63)
    val ORB_BUILD_INNER = MenuColors.CARD_BUILD_BORDER
    val ORB_WEAPON_OUTER = Color(0x44FF6C43)
    val ORB_WEAPON_INNER = MenuColors.CARD_WEAPON_BORDER

    fun orbOuter(group: UnlockGroup): Color = when (group) {
        UnlockGroup.MOVEMENT -> ORB_MOVEMENT_OUTER
        UnlockGroup.BUILD -> ORB_BUILD_OUTER
        UnlockGroup.WEAPON -> ORB_WEAPON_OUTER
    }

    fun orbInner(group: UnlockGroup): Color = when (group) {
        UnlockGroup.MOVEMENT -> ORB_MOVEMENT_INNER
        UnlockGroup.BUILD -> ORB_BUILD_INNER
        UnlockGroup.WEAPON -> ORB_WEAPON_INNER
    }

    val ROCKET = Color(0xFFFF6600)
    val ROCKET_CORE = HudColors.BANNER_GOLD_TOP
    val ROCKET_FIN = Color(0xFFCC3300)
    val ROCKET_FLAME_OUTER = Color(0x88FFAA22)
    val ROCKET_FLAME_INNER = Color(0xCCFFEE88)

    val MISSILE = Color(0xFFFF4400)
    val MISSILE_CORE = Color(0xFFFFCC00)
    val MISSILE_TAIL = Color(0x66FF6600)

    val SOAKER_DROPLET = Tile.BOUNCY.color
    val SOAKER_DROPLET_CORE = Color(0xFFCCFFFF)
    val MISSILE_AURA_OUTER = Color(0x33FF4444)
    val MISSILE_AURA_INNER = Color(0xAAFF6600)

    val SNIPER_GUIDE = Color(0x66FFDD44)
    val SNIPER_BULLET_OUTER = Color(0x33FFDD44)
    val SNIPER_BULLET_MIDDLE = Color(0xAAFFEE66)
    val SNIPER_BULLET_CORE = Color(0xFFFFFFCC)

    val BOSS_EYE_PUPIL = Color(0xFF6600AA)

    val LASER_OUTER = MISSILE_AURA_OUTER
    val LASER_MIDDLE = Color(0xAAFF2222)
    val LASER_CORE = Color(0xFFFFAAAA)
    val LASER_IMPACT = Color(0xCCFF4444)

    val BOSS_LASER_OUTER = Color(0x33CC44FF)
    val BOSS_LASER_MIDDLE = Color(0xAA9933DD)
    val BOSS_LASER_CORE = Color(0xFFEEBBFF)

    val BOSS_EYE_BEAM_CHARGE = Color(0x88FF44FF)

    val BOSS_BOMB_FLASH = Color(0xFFFFEE66)

    val BOSS_METEOR_OUTER = MISSILE_TAIL
    val BOSS_METEOR_MIDDLE = Color(0xCCFF8822)
    val BOSS_METEOR_CORE = Color(0xFFFFEE99)
}
