package com.glycin.koita.ui_composables.atlas

import com.glycin.koita.core.SpriteSheet
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.upgrade_icons_sheet

object AtlasUpgradeObjects {
    private val ICONS = SpriteSheet(Res.drawable.upgrade_icons_sheet, 32, 32, 15)

    private fun entry(frame: Int, name: String, description: String) =
        AtlasEntry(ICONS, frame..frame, 0, name, description)

    val all: List<AtlasEntry> = listOf(
        entry(9, "Null Safety", "Slam down from the air to destroy terrain and damage enemies. Press S or push the left thumbstick down while airborne to activate."),
        entry(3, "Elvis Operator", "Unlocks a double jump. Press Space or push up on the left thumbstick up again while airborne."),
        entry(12, "Smart Casts", "Your missiles home in on the nearest enemy. Triggers automatically while firing missiles in attack mode."),
        entry(1, "Data Classes", "The blocks you place now explode when you shoot them. Place dynamite in build mode, then shoot it to detonate."),
        entry(0, "Coroutines", "Your weapon turns into a continuous laser instead. Switch to attack mode and hold fire to keep the beam going."),
        entry(13, "Suspend Functions", "Hold jump while falling to suspend your descent. Hold Space or up on the left thumbstick mid-air."),
        entry(4, "Extension Functions", "Unlocks a jetpack you can use to float briefly. Press Space or push up on the left thumbstick after your jumps are spent."),
        entry(11, "Sealed Classes", "The blocks you place become trampolines. Place bouncy blocks in build mode and jump on them for a triple-height bounce."),
        entry(2, "Delegation", "Unlocks a turret block that shoots enemies. Place turrets in build mode. They auto-fire at nearby enemies."),
        entry(7, "Inline Functions", "Press Shift to dash in any direction. Tap the Dash button or double-flick the left thumbstick on touch."),
        entry(5, "Higher-Order Functions", "Your weapon fires slow rockets that accelerate into a massive explosion. Switch to attack mode and fire to launch one."),
        entry(8, "Kotlin Multiplatform", "Your weapon shoots a stream of water. Switch to attack mode and hold fire to spray."),
        entry(6, "Immutability", "Press S or push down the left thumbstick on the ground to become immovable and invulnerable."),
        entry(10, "Scope Functions", "Hold to aim & release to fire a piercing shot. Switch to attack mode, hold fire to charge, release to shoot."),
        entry(14, "Companion Blocks", "Automatically use resources to create a shield around you. Always active. Uses minerals to rebuild."),
    )
}
