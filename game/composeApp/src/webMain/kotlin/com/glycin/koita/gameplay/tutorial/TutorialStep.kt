package com.glycin.koita.gameplay.tutorial

import androidx.compose.ui.input.key.Key
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.enemies.Slime
import com.glycin.koita.gameplay.modes.AttackMode
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
)

sealed class TutorialStep(val prompt: String) {
    open fun setup(ctx: StepContext) {}
    abstract fun isComplete(ctx: StepContext): Boolean
    open fun cleanup(ctx: StepContext) {}
}

abstract class ContinueOnEnterStep(prompt: String) : TutorialStep(prompt) {
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

class MoveStep : TutorialStep("Use WASD or the arrow keys to move") {
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

class EquipPickaxeStep : TutorialStep("Press 1 to equip the pickaxe") {
    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_HAMMER)
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.selectedHotkeyIndex == HOTKEY_PICKAXE
}

class MineBlockStep : TutorialStep("Hold left click on the block to mine it") {
    private var target: TileRect? = null
    private var snapshotSolidCount = 0

    override fun setup(ctx: StepContext) {
        val rect = TutorialWorldBuilder.placeMiningTarget(ctx.world, ctx.player)
        target = rect
        snapshotSolidCount = ctx.world.countSolidIn(rect)
    }

    override fun isComplete(ctx: StepContext): Boolean {
        val rect = target ?: return false
        return ctx.world.countSolidIn(rect) < snapshotSolidCount
    }
}

class ShowResourcesStep : ContinueOnEnterStep(
    "Look at the top-right. Your score and materials updated when you mined. \nPress ENTER to continue.",
)

class EquipWeaponStep : TutorialStep("Press 2 to equip your weapon") {
    override fun isComplete(ctx: StepContext): Boolean =
        ctx.gameState.selectedHotkeyIndex == HOTKEY_WEAPON
}

class ShootWeaponStep : TutorialStep("Left click to shoot your weapon") {
    private var fired = false

    override fun setup(ctx: StepContext) {
        fired = false
    }

    override fun isComplete(ctx: StepContext): Boolean {
        if (fired) return true
        val attackMode = ctx.player.currentWeapon as? AttackMode ?: return false
        if (attackMode.getActiveWeapon().isNotEmpty()) {
            fired = true
            return true
        }
        return false
    }
}

class EquipHammerStep : TutorialStep("Press 3 to equip the hammer") {
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

    override fun setup(ctx: StepContext) {
        TutorialWorldBuilder.placeLavaPool(ctx.world, ctx.player)
        snapshotHealth = ctx.player.health
    }

    override fun isComplete(ctx: StepContext): Boolean =
        ctx.player.health < snapshotHealth

    override fun cleanup(ctx: StepContext) {
        TutorialWorldBuilder.clearNonIndestructible(ctx.world)
    }
}

class ShowHealthStep : ContinueOnEnterStep(
    "You were hurt. On the top left you can see your health. Press ENTER to continue.",
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

class MineGoldStep : TutorialStep("Mine the gold ore") {
    private var target: TileRect? = null
    private var snapshotSolidCount = 0

    override fun setup(ctx: StepContext) {
        ctx.player.equip(HOTKEY_PICKAXE)
        val rect = TutorialWorldBuilder.placeGoldOreCluster(ctx.world, ctx.player)
        target = rect
        snapshotSolidCount = ctx.world.countSolidIn(rect)
    }

    override fun isComplete(ctx: StepContext): Boolean {
        val rect = target ?: return false
        return ctx.world.countSolidIn(rect) < snapshotSolidCount
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

class PlaceholderStep : TutorialStep("") {
    override fun isComplete(ctx: StepContext): Boolean = true
}
