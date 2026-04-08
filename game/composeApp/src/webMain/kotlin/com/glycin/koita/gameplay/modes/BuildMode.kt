package com.glycin.koita.gameplay.modes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.angleTo
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.math.PI
import kotlin.math.sin

class BuildMode(
    position: Vec2,
    width: Float = 32f,
    height: Float = 64f,
    world: World,
    collisionDetector: CollisionDetector,
    particleSystem: ParticleSystem,
    mouse: Mouse,
    private val gameState: GameState,
    private val turretManager: TurretManager,
): Mode(position, width, height, world, collisionDetector, particleSystem, mouse) {

    override val coolDownMs: Long = 150L

    private var swingProgress = 0f
    private var dynamiteCooldownCount = 0 // Amount of blocks placed
    private var holdTimer = 0f
    private var waitForRelease = true

    private val swingDuration = 0.2f
    private val swingAngle = 90f
    private val tileSize = 5
    private val maxBuildDistance = 250f

    var ghostTileX: Int? = null
        private set
    var ghostTileY: Int? = null
        private set
    var isGhostValid: Boolean = false
        private set
    var holdProgress by mutableStateOf(0f)
        private set

    fun onEquipped() {
        waitForRelease = true
    }

    override fun use() {
        if (waitForRelease) return
        if (!canUse()) return
        swingProgress = 0f
        used()
    }

    //TODO: Remove the swing logic and replace with animation timings when i have the drone animations
    override fun update(deltaTime: Float) {
        if (waitForRelease && !mouse.isLeftPressed) {
            waitForRelease = false
        }

        if (gameState.turretUnlocked && mouse.isLeftPressed) {
            holdTimer += deltaTime
            holdProgress = (holdTimer / TURRET_HOLD_TIME).coerceAtMost(1f)
        } else {
            resetTurretHold()
        }

        val baseRotation = pivotPoint.angleTo(mouse.worldPosition) + 90f

        val isMouseOnRight = mouse.worldPosition.x >= pivotPoint.x
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

        updateGhostTile()
    }

    private fun onSwingImpact() {
        if (!isGhostValid || ghostTileX == null || ghostTileY == null) {
            return
        }

        val isTurret = holdTimer >= TURRET_HOLD_TIME && gameState.turretUnlocked
        val selectedTile = when {
            isTurret -> Tile.KOTLINIUM
            gameState.explosiveBlocks && dynamiteCooldownCount == 0 -> Tile.DYNAMITE
            gameState.bouncyBlocks -> Tile.BOUNCY
            else -> Tile.STONE
        }
        val centerTileX = ghostTileX!!
        val centerTileY = ghostTileY!!

        for (dy in 0 until tileSize) {
            for (dx in 0 until tileSize) {
                val tileX = centerTileX + dx
                val tileY = centerTileY + dy
                if (!world[tileX, tileY].isIndestructible) {
                    world[tileX, tileY] = selectedTile
                }
            }
        }

        if (isTurret) {
            turretManager.addTurret(centerTileX, centerTileY)
            resetTurretHold()
        } else if (gameState.explosiveBlocks) {
            dynamiteCooldownCount = (dynamiteCooldownCount + 1) % 6
        }

        gameState.collectedStones -= 25
    }

    private fun updateGhostTile() {
        val tileX = (mouse.worldPosition.x / WorldConstants.TILE_SIZE).toInt()
        val tileY = (mouse.worldPosition.y / WorldConstants.TILE_SIZE).toInt()

        val tileWorldX = tileX * WorldConstants.TILE_SIZE.toFloat() + WorldConstants.TILE_SIZE / 2f
        val tileWorldY = tileY * WorldConstants.TILE_SIZE.toFloat() + WorldConstants.TILE_SIZE / 2f
        val distanceToPlayer = Vec2.distance(Vec2(tileWorldX, tileWorldY), pivotPoint)

        if (distanceToPlayer > maxBuildDistance ||
            tileX !in 0 until WorldConstants.WORLD_WIDTH_TILES ||
            tileY !in 0 until WorldConstants.WORLD_HEIGHT_TILES
        ) {
            ghostTileX = null
            ghostTileY = null
            isGhostValid = false
            return
        }

        ghostTileX = tileX
        ghostTileY = tileY

        if (gameState.collectedStones < 25) {
            isGhostValid = false
            return
        }

        val playerSize = 32f
        val playerPos = Vec2(pivotPoint.x - playerSize / 2f, pivotPoint.y - playerSize / 2f)
        val placePos = Vec2(tileX * WorldConstants.TILE_SIZE.toFloat(), tileY * WorldConstants.TILE_SIZE.toFloat())
        val placeSize = tileSize * WorldConstants.TILE_SIZE.toFloat()

        if (collisionDetector.checkAABBOverlap(playerPos, playerSize, playerSize, placePos, placeSize, placeSize)) {
            isGhostValid = false
            return
        }

        for (dy in 0 until tileSize) {
            for (dx in 0 until tileSize) {
                val checkX = tileX + dx
                val checkY = tileY + dy
                if (checkX in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                    checkY in 0 until WorldConstants.WORLD_HEIGHT_TILES &&
                    world[checkX, checkY].isIndestructible
                ) {
                    isGhostValid = false
                    return
                }
            }
        }

        isGhostValid = true
    }

    private fun resetTurretHold() {
        holdTimer = 0f
        holdProgress = 0f
    }

    companion object {
        private const val TURRET_HOLD_TIME = 1.0f
    }
}