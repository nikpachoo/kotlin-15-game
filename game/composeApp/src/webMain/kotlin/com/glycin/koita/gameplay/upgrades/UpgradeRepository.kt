package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.core.SpriteSheet
import com.glycin.koita.gameplay.GameState
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.upgrade_icons_sheet

class UpgradeRepository(
    private val unlocks: List<Unlock>,
    private val gameState: GameState,
) {
    var onUpgradeCallback: (() -> Unit)? = null
    private val unlocked = mutableSetOf<UnlockId>()

    fun getAll(): List<Unlock> = unlocks

    fun getUnlocked(): List<Unlock> = unlocks.filter { it.id in unlocked }

    fun unlockedCount(): Int = unlocked.size

    fun isUnlocked(id: UnlockId): Boolean = id in unlocked

    fun getById(id: UnlockId): Unlock? = unlocks.firstOrNull { it.id == id }

    fun upgrade(upgradeId: UnlockId) {
        val upgrade = getById(upgradeId) ?: return
        unlocked.add(upgrade.id)
        upgrade.onUnlock()
        onUpgradeCallback?.invoke()
    }

    fun getRandomAvailable(count: Int): List<Unlock> {
        return unlocks.filter { it.id !in unlocked }.shuffled().take(count)
    }

    companion object {
        private val UPGRADE_ICONS = SpriteSheet(
            sprite = Res.drawable.upgrade_icons_sheet,
            frameWidth = 32,
            frameHeight = 32,
            columns = 15,
        )

        fun getStandardRepository(gameState: GameState): UpgradeRepository {
            return UpgradeRepository(
                listOf(
                    Unlock(
                        id = UnlockId.GROUND_POUND,
                        group = UnlockGroup.MOVEMENT,
                        name = "Null Safety",
                        description = "Slam down from the air to destroy terrain and damage enemies.",
                        icon = UPGRADE_ICONS.frame(9),
                        onUnlock = { gameState.canGroundPound = true }
                    ),
                    Unlock(
                        id = UnlockId.DOUBLE_JUMP,
                        group = UnlockGroup.MOVEMENT,
                        name = "Elvis Operator",
                        description = "Unlocks a double jump.",
                        icon = UPGRADE_ICONS.frame(3),
                        onUnlock = { gameState.canDoubleJump = true }
                    ),
                    Unlock(
                        id = UnlockId.HOMING_MISSILES,
                        group = UnlockGroup.WEAPON,
                        name = "Smart Casts",
                        description = "Your missiles home in on the nearest enemy.",
                        icon = UPGRADE_ICONS.frame(12),
                        onUnlock = { gameState.homingMissilesUnlocked = true }
                    ),
                    Unlock(
                        id = UnlockId.EXPLODING_BLOCKS,
                        group = UnlockGroup.BUILD,
                        name = "Data Classes",
                        description = "The blocks you place now explode when you shoot them.",
                        icon = UPGRADE_ICONS.frame(1),
                        onUnlock = { gameState.explosiveBlocks = true }
                    ),
                    Unlock(
                        id = UnlockId.LASER,
                        group = UnlockGroup.WEAPON,
                        name = "Coroutines",
                        description = "Your weapon turns into a continuous laser instead.",
                        icon = UPGRADE_ICONS.frame(0),
                        onUnlock = { gameState.laserWeapon = true }
                    ),
                    Unlock(
                        id = UnlockId.SLOW_FALL,
                        group = UnlockGroup.MOVEMENT,
                        name = "Suspend Functions",
                        description = "Hold jump while falling to suspend your descent.",
                        icon = UPGRADE_ICONS.frame(13),
                        onUnlock = { gameState.canHover = true }
                    ),
                    Unlock(
                        id = UnlockId.JETPACK,
                        group = UnlockGroup.MOVEMENT,
                        name = "Extension Functions",
                        description = "Unlocks a jetpack you can use to float briefly.",
                        icon = UPGRADE_ICONS.frame(4),
                        onUnlock = { gameState.canJetpack = true }
                    ),
                    Unlock(
                        id = UnlockId.BOUNCY_BLOCKS,
                        group = UnlockGroup.BUILD,
                        name = "Sealed Classes",
                        description = "The blocks you place become trampolines.",
                        icon = UPGRADE_ICONS.frame(11),
                        onUnlock = { gameState.bouncyBlocks = true }
                    ),
                    Unlock(
                        id = UnlockId.TURRET,
                        group = UnlockGroup.BUILD,
                        name = "Delegation",
                        description = "Unlocks a turret block that shoots enemies.",
                        icon = UPGRADE_ICONS.frame(2),
                        onUnlock = { gameState.turretUnlocked = true }
                    ),
                    Unlock(
                        id = UnlockId.DASH,
                        group = UnlockGroup.MOVEMENT,
                        name = "Inline Functions",
                        description = "Press Shift to dash in any direction.",
                        icon = UPGRADE_ICONS.frame(7),
                        onUnlock = { gameState.canDash = true }
                    ),
                    Unlock(
                        id = UnlockId.ROCKET_LAUNCHER,
                        group = UnlockGroup.WEAPON,
                        name = "Higher-Order Functions",
                        description = "Your weapon fires slow rockets that accelerate into a massive explosion.",
                        icon = UPGRADE_ICONS.frame(5),
                        onUnlock = { gameState.rocketLauncher = true }
                    ),
                    Unlock(
                        id = UnlockId.SUPER_SOAKER,
                        group = UnlockGroup.WEAPON,
                        name = "Kotlin Multiplatform",
                        description = "Your weapon shoots a stream of water.",
                        icon = UPGRADE_ICONS.frame(8),
                        onUnlock = { gameState.superSoaker = true }
                    ),
                    Unlock(
                        id = UnlockId.INVULNERABLE,
                        group = UnlockGroup.BUILD,
                        name = "Immutability",
                        description = "Press S on the ground to become immovable and invulnerable.",
                        icon = UPGRADE_ICONS.frame(6),
                        onUnlock = { gameState.canAnchor = true }
                    ),
                    Unlock(
                        id = UnlockId.SNIPER,
                        group = UnlockGroup.WEAPON,
                        name = "Scope Functions",
                        description = "Hold to aim, release to fire a piercing shot.",
                        icon = UPGRADE_ICONS.frame(10),
                        onUnlock = { gameState.sniperWeapon = true }
                    ),
                    Unlock(
                        id = UnlockId.RESOURCE_SHIELD,
                        group = UnlockGroup.BUILD,
                        name = "Companion Blocks",
                        description = "Automatically use resources to create a shield around you.",
                        icon = UPGRADE_ICONS.frame(14),
                        onUnlock = { gameState.resourceShield = true }
                    ),
                ),
                gameState
            )
        }
    }
}
