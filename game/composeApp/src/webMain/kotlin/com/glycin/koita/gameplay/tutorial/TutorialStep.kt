package com.glycin.koita.gameplay.tutorial

import androidx.compose.ui.input.key.Key
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.enemies.Slime
import com.glycin.koita.gameplay.modes.AttackMode
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.upgrades.Shrine
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World
import kotlin.math.abs

private const val BUILD_RESOURCE_GRANT = 200
private const val HEAL_RESOURCE_GRANT = 200

private const val HOTKEY_PICKAXE = 0
private const val HOTKEY_WEAPON = 1
private const val HOTKEY_HAMMER = 2

data class StepContext(
    val player: Player,
    val gameState: GameState,
    val world: World,
    val input: Input,
    val collisionDetector: CollisionDetector,
    val particleSystem: ParticleSystem,
    val enemyManager: EnemyManager,
    val pickupManager: PickupManager,
    val shrineManager: ShrineManager,
    val upgradeRepository: UpgradeRepository,
)

sealed class TutorialStep(val prompt: String) {
    open val awaitsContinue: Boolean = false
    open fun setup(ctx: StepContext) {}
    abstract fun isComplete(ctx: StepContext): Boolean
    open fun cleanup(ctx: StepContext) {}
}

abstract class ContinueOnEnterStep(prompt: String) : TutorialStep(prompt) {
    final override val awaitsContinue: Boolean = true
    private var enterWasDown = false

    override fun setup(ctx: StepContext) {
        enterWasDown = ctx.input.keyMap[Key.Enter] == true
    }

    override fun isComplete(ctx: StepContext): Boolean {
        val isDown = ctx.input.keyMap[Key.Enter] == true
        if (!isDown) {
            enterWasDown = false
            return false
        }
        return !enterWasDown
    }
}

private fun World.countSolidIn(rect: TileRect): Int {
    var count = 0
    for (y in rect.top..rect.bottom) {
        for (x in rect.left..rect.right) {
            if (this[x, y].isSolid) count++
        }
    }
    return count
}

private fun World.hasMinedSince(rect: TileRect, initialNonSolid: Int): Boolean {
    var nonSolid = 0
    for (y in rect.top..rect.bottom) {
        for (x in rect.left..rect.right) {
            if (!this[x, y].isSolid) {
                nonSolid++
                if (nonSolid > initialNonSolid) return true
            }
        }
    }
    return false
}

private val TileRect.area: Int
    get() = (bottom - top + 1) * (right - left + 1)

abstract class MineRectStep(prompt: String) : TutorialStep(prompt) {
    private var target: TileRect? = null
    private var initialNonSolid = 0

    protected abstract fun placeTarget(ctx: StepContext): TileRect

    override fun setup(ctx: StepContext) {
        val rect = placeTarget(ctx)
        target = rect
        initialNonSolid = rect.area - ctx.world.countSolidIn(rect)
    }

    override fun isComplete(ctx: StepContext): Boolean {
        val rect = target ?: return false
        return ctx.world.hasMinedSince(rect, initialNonSolid)
    }
}

class MoveStep : TutorialStep("Use WASD to move") {
    private var startX = 0f

    override fun setup(ctx: StepContext) {
        startX = ctx.player.position.x
    }

    override fun isComplete(ctx: StepContext): Boolean =
        abs(ctx.player.position.x - startX) >= TutorialConstants.MOVE_THRESHOLD_PX
}

class JumpStep : TutorialStep("Press SPACE to jump") {
    private var startY = 0f

    override fun setup(ctx: StepContext) {
        startY = ctx.player.position.y
    }

    override fun isComplete(ctx: StepContext): Boolean =
        startY - ctx.player.position.y >= TutorialConstants.JUMP_THRESHOLD_PX
}

class DroneIntroStep : ContinueOnEnterStep(
    "The little drone floating beside you is your companion. It carries all of Kodee's tools and switches between them as you equip them.",
)

class EquipPickaxeStep : TutorialStep("Press 1 to switch your drone to mining mode") {
    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_HAMMER)
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.selectedHotkeyIndex == HOTKEY_PICKAXE
}

class MineBlockStep : MineRectStep("Hold left click on the block to mine it") {
    override fun placeTarget(ctx: StepContext): TileRect =
        TutorialWorldBuilder.placeMiningTarget(ctx.world, ctx.player)
}

class DigUpStep : TutorialStep("Dig up to reach the surface. That is your goal in the game") {
    private var startY = 0f

    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_PICKAXE)
        TutorialWorldBuilder.fillWorldSolid(ctx.world, ctx.player)
        startY = ctx.player.position.y
    }

    override fun isComplete(ctx: StepContext): Boolean =
        startY - ctx.player.position.y >= TutorialConstants.DIG_UP_THRESHOLD_PX

    override fun cleanup(ctx: StepContext) {
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
    }
}

class ShowResourcesStep : ContinueOnEnterStep(
    "Look at the top-right. Your score and materials updated when you mined.",
)

class EquipWeaponStep : TutorialStep("Press 2 to switch your drone to attack mode") {
    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.selectedHotkeyIndex == HOTKEY_WEAPON
}

class ShootWeaponStep : TutorialStep("Left click to shoot your weapon") {
    override fun isComplete(ctx: StepContext): Boolean {
        val attackMode = ctx.player.currentWeapon as? AttackMode ?: return false
        return attackMode.getActiveWeapon().isNotEmpty()
    }
}

class EquipHammerStep : TutorialStep("Press 3 to switch your drone to build mode") {
    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.selectedHotkeyIndex == HOTKEY_HAMMER
}

class PlaceBlockStep : TutorialStep("Left click to place a block when the indicator is green. This costs some of your mined materials.") {
    private var snapshotCollectedSimple = 0

    override fun setup(ctx: StepContext) {
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
        if (ctx.gameState.collectedSimple < BUILD_RESOURCE_GRANT) {
            ctx.gameState.collectedSimple = BUILD_RESOURCE_GRANT
        }
        snapshotCollectedSimple = ctx.gameState.collectedSimple
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.collectedSimple < snapshotCollectedSimple
}

class LavaJumpStep : TutorialStep("Jump in the lava until it damages you") {
    private var snapshotHealth = 0
    private var lavaPoolLayout: LavaPoolLayout? = null

    override fun setup(ctx: StepContext) {
        lavaPoolLayout = TutorialWorldBuilder.placeLavaPool(ctx.world, ctx.player)
        snapshotHealth = ctx.player.health
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.player.health < snapshotHealth

    override fun cleanup(ctx: StepContext) {
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
        lavaPoolLayout?.let { TutorialWorldBuilder.clearLavaPool(ctx.world, it) }
    }
}

class ShowHealthStep : ContinueOnEnterStep(
    "You were hurt. On the top-left you can see your health.",
)

class KillSlimeStep : TutorialStep("Defeat the slime") {
    private var slime: Slime? = null

    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_WEAPON)
        slime = TutorialWorldBuilder.spawnSlimeInCave(
            world = ctx.world,
            player = ctx.player,
            collisionDetector = ctx.collisionDetector,
            particleSystem = ctx.particleSystem,
            enemyManager = ctx.enemyManager,
        )
    }

    override fun isComplete(ctx: StepContext): Boolean = slime?.isAlive == false
}

class MineGoldStep : MineRectStep("Mine the gold ore") {
    override fun placeTarget(ctx: StepContext): TileRect {
        ctx.player.equip(HOTKEY_PICKAXE)
        return TutorialWorldBuilder.placeGoldOreCluster(ctx.world, ctx.player)
    }
}

class HealStep : TutorialStep("Press E to heal. This costs 100 ore, and is doubled every time you heal.") {
    private var snapshotHealth = 0

    override fun setup(ctx: StepContext) {
        if (ctx.player.health == ctx.player.maxHealth) {
            ctx.player.takeDamage(1)
        }
        if (ctx.gameState.collectedRich < ctx.gameState.nextHealCost) {
            ctx.gameState.collectedRich = ctx.gameState.nextHealCost + HEAL_RESOURCE_GRANT
        }
        snapshotHealth = ctx.player.health
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.player.health > snapshotHealth
}

class PickupHeartStep : TutorialStep("Mine through the stone block to reach the heart pickup") {
    private var snapshotMaxHealth = 0

    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_PICKAXE)
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
        TutorialWorldBuilder.placeHeartChamber(ctx.world, ctx.pickupManager, ctx.player)
        snapshotMaxHealth = ctx.player.maxHealth
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.player.maxHealth > snapshotMaxHealth
}

class PickupInfoStep : ContinueOnEnterStep(
    "That pickup increased your max health! Different pickups have different effects.",
)

class ChargeShrineStep : TutorialStep("Stand on the shrine for 3 seconds to charge it") {
    private var shrine: Shrine? = null

    override fun setup(ctx: StepContext) {
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
        shrine = TutorialWorldBuilder.spawnTutorialShrine(ctx.shrineManager, ctx.upgradeRepository, ctx.player)
    }

    override fun isComplete(ctx: StepContext): Boolean = shrine?.isActivated == true
}

class PickUpgradeStep : TutorialStep("Now you can see an upgrade. Walk into the orb to unlock it") {
    private var snapshotUnlockedCount = 0

    override fun setup(ctx: StepContext) {
        snapshotUnlockedCount = ctx.upgradeRepository.unlockedCount()
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.upgradeRepository.unlockedCount() > snapshotUnlockedCount
}

class UpgradeInfoStep : ContinueOnEnterStep(
    "You unlocked your first upgrade! Red upgrades unlock new weapons, blue upgrades unlock movement options, and green upgrades unlock build options.",
)

class WeaponSwitchInfoStep : ContinueOnEnterStep(
    "You can change your weapon by clicking the button on the right side of the screen under WEAPON. The same works for new BLOCKS once you unlock them.",
)

class UpgradeCombinationInfoStep : ContinueOnEnterStep(
    "Combining different kinds of upgrades can have unexpected and fun results!",
)

class PortalGoalStep : ContinueOnEnterStep(
    "Your goal is to help Kodee reach the top of the world and pass through the portal there.",
)

class CompleteTutorialStep : TutorialStep("Press ESC to complete the tutorial") {
    override fun isComplete(ctx: StepContext): Boolean = false
}
