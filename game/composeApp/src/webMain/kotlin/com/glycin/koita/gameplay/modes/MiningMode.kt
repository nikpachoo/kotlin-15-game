package com.glycin.koita.gameplay.modes

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.physics.CollectibleSystem
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.angleTo
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.math.PI
import kotlin.math.sin

class MiningMode(
    position: Vec2,
    width: Float = 32f,
    height: Float = 64f,
    world: World,
    collisionDetector: CollisionDetector,
    particleSystem: ParticleSystem,
    mouse: Mouse,
    private val collectibleSystem: CollectibleSystem,
    private val gameState: GameState,
): Mode(position, width, height, world, collisionDetector, particleSystem, mouse) {

    override val coolDownMs: Long = 150L
    private var swingProgress = 0f
    private val swingDuration = coolDownMs / 1000f
    private val swingAngle = 120f
    private val baseMiningRadius = 50f

    var onCollectHit: ((impactPoint: Vec2) -> Unit)? = null

    override fun use() {
        if(!canUse()) return
        swingProgress = 0f
        used()
    }

    //TODO: Remove the swing logic and replace with animation timings when i have the drone animations
    override fun update(deltaTime: Float) {
        val baseRotation = position.angleTo(mouse.worldPosition) + 90f
        val isMouseOnRight = mouse.worldPosition.x >= position.x
        val swingDirection = if (isMouseOnRight) 1f else -1f
        val previousProgress = swingProgress

        if (swingProgress < 1f) {
            swingProgress += deltaTime / swingDuration
            swingProgress = swingProgress.coerceAtMost(1f)

            if (previousProgress < 0.7f && swingProgress >= 0.7f) {
                onSwingImpact()
            }

            val easedProgress = sin(swingProgress * PI.toFloat() / 2f)
            val swingOffset = (easedProgress - 0.5f) * swingAngle * swingDirection
            rotation = baseRotation + swingOffset
        } else {
            rotation = baseRotation
        }
    }

    private fun onSwingImpact() {
        val impactPoint = getActivationPoint(0.25f)
        val miningRadius = baseMiningRadius * gameState.miningRadiusMultiplier
        val affectedTiles = collisionDetector.getTilesInRadius(impactPoint, miningRadius)
        var hitSolid = false

        affectedTiles.forEach { (tileX, tileY) ->
            val tile = world[tileX, tileY]
            if ((tile.isSolid || tile.isFragile) && !tile.isIndestructible) {
                val collectibleX = tileX * WorldConstants.TILE_SIZE.toFloat() + WorldConstants.TILE_SIZE / 2f
                val collectibleY = tileY * WorldConstants.TILE_SIZE.toFloat() + WorldConstants.TILE_SIZE / 2f
                collectibleSystem.spawn(collectibleX, collectibleY, tile)
                world[tileX, tileY] = Tile.AIR
                hitSolid = true
            }
        }

        if(hitSolid) {
            SoundManager.playOneShot(Sounds.DIG)
            onCollectHit?.invoke(impactPoint)
        }
    }
}