package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.ModifierConfiguration
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.SpriteSet
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.abs

class ShrineManager(
    private val player: Player,
    private val upgradeRepository: UpgradeRepository,
    private val particleSystem: ParticleSystem,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val gameState: GameState,
) {
    private val shrines = mutableListOf<Shrine>()
    private val orbs = mutableListOf<UnlockOrb>()
    private val shrineSprites = SpriteSet()
    private val orbSprites = SpriteSet()
    private var nextGroupId = 0

    fun add(shrine: Shrine) {
        shrines.add(shrine)
        shrineSprites.add(shrine.spriteAnimator.sprite)
    }

    fun getShrinesInRange(position: Vec2, range: Float): List<Shrine> {
        val rangeSq = range * range
        return shrines.filter { shrine ->
            Vec2.fastDistance(shrine.center, position) <= rangeSq
        }
    }

    fun getOrbs() = orbs

    fun getDistinctSprites(): Set<DrawableResource> = shrineSprites.distinct

    fun getDistinctOrbIconSprites(): Set<DrawableResource> = orbSprites.distinct

    fun update(deltaTime: Float) {
        shrines.firstOrNull { abs(it.position.y - player.position.y) <= it.height + 25 }?.let { shrineInRange ->
            if(shrineInRange.overlapsPlayer(player)) {
                shrineInRange.startActivation()
            } else {
                shrineInRange.cancelActivation()
            }

            shrineInRange.update(deltaTime)

            if (shrineInRange.isActivated) {
                explode(shrineInRange)
                spawnOrbs(shrineInRange)
                shrineSprites.remove(shrineInRange.spriteAnimator.sprite)
                shrines.remove(shrineInRange)
            }
        }

        orbs.forEach { it.update(deltaTime) }

        val pickedOrb = orbs.firstOrNull { !it.isFlying && it.overlapsPlayer(player) }
        if (pickedOrb != null) {
            upgradeRepository.upgrade(pickedOrb.unlock.id)
            gameState.pickupNotification = "Unlocked ${pickedOrb.unlock.name}!"
            val groupId = pickedOrb.groupId
            for (i in orbs.size - 1 downTo 0) {
                val orb = orbs[i]
                if (orb.groupId == groupId) {
                    orbSprites.remove(orb.unlock.icon.sheet.sprite)
                    orbs.removeAt(i)
                }
            }
            SoundManager.playOneShot(Sounds.UPGRADE_UNLOCK)
        }
    }

    private fun explode(shrine: Shrine) {
        val position = shrine.center
        val impactRadius = 120f
        val affectedTiles = collisionDetector.getTilesInRadius(position, impactRadius)
        SoundManager.playOneShot(Sounds.EXPLODE)
        explodeTerrain(affectedTiles, position, impactRadius, world, particleSystem)
        player.applyShrineLift()
    }

    fun spawnFirstOrbs(origin: Vec2) {
        if (ModifierConfiguration.noShrines) return

        val choices = upgradeRepository.getRandomAvailable(3)
        if (choices.isEmpty()) return

        val groupId = nextGroupId++
        val horizontalSpacing = 220f
        val totalWidth = (choices.size - 1) * horizontalSpacing
        val startX = origin.x - totalWidth / 2f
        val baseY = origin.y - 25f
        val arcHeight = 40f

        choices.forEachIndexed { index, upgrade ->
            val targetX = startX + index * horizontalSpacing
            val t = if (choices.size > 1) (2f * index / (choices.size - 1) - 1f) else 0f
            val targetY = baseY - arcHeight * (1f - t * t)

            val orb = UnlockOrb(
                unlock = upgrade,
                startPosition = Vec2(origin.x, origin.y),
                targetPosition = Vec2(targetX, targetY),
                groupId = groupId,
            )
            orbs.add(orb)
            orbSprites.add(orb.unlock.icon.sheet.sprite)
        }
    }

    private fun spawnOrbs(shrine: Shrine) {
        val choices = shrine.choices ?: upgradeRepository.getRandomAvailable(3)
        if (choices.isEmpty()) return

        val groupId = nextGroupId++
        val shrineCenter = shrine.center
        val worldMidX = WorldConstants.WORLD_WIDTH_PIXELS / 2f

        val directionX = if (shrineCenter.x < worldMidX) 1f else -1f

        val spreadDistance = 300f
        val verticalSpacing = 120f
        val verticalOffset = (choices.size - 1) * verticalSpacing / 2f

        choices.forEachIndexed { index, upgrade ->
            val targetX = shrineCenter.x + directionX * spreadDistance
            val targetY = shrineCenter.y - verticalOffset + index * verticalSpacing

            val orb = UnlockOrb(
                unlock = upgrade,
                startPosition = Vec2(shrineCenter.x, shrineCenter.y),
                targetPosition = Vec2(targetX, targetY),
                groupId = groupId,
            )
            orbs.add(orb)
            orbSprites.add(orb.unlock.icon.sheet.sprite)
        }
    }
}
