package com.glycin.koita.gameplay.modes

import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameSettings
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.angleTo
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.isValidTile
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
    private var waitForRelease = true

    private val swingDuration = 0.2f
    private val swingAngle = 90f
    private val tileSize = 5
    private val maxBuildDistance = 250f
    private val maxBuildDistanceSq = maxBuildDistance * maxBuildDistance

    var ghostTileX: Int? = null
        private set
    var ghostTileY: Int? = null
        private set
    var isGhostValid: Boolean = false
        private set

    fun onEquipped() {
        waitForRelease = true
    }

    override fun use() {
        if (waitForRelease) return
        if (!canUse()) return
        swingProgress = 0f
        used()
        placeBlock()
    }

    override fun update(deltaTime: Float) {
        if (waitForRelease && !mouse.isLeftPressed) {
            waitForRelease = false
        }

        val baseRotation = pivotPoint.angleTo(mouse.worldPosition) + 90f

        val isMouseOnRight = mouse.worldPosition.x >= pivotPoint.x
        val swingDirection = if (isMouseOnRight) 1f else -1f

        if (swingProgress < 1f) {
            swingProgress += deltaTime / swingDuration
            swingProgress = swingProgress.coerceAtMost(1f)

            val easedProgress = sin(swingProgress * PI.toFloat() / 2f)
            val swingOffset = (easedProgress - 0.5f) * swingAngle * swingDirection
            rotation = baseRotation + swingOffset
        } else {
            rotation = baseRotation
        }

        updateGhostTile()
    }

    private fun placeBlock() {
        val selected = gameState.selectedBlock
        if (!isGhostValid) {
            val cost = costFor(selected)
            if (gameState.collectedSimple < cost) {
                gameState.pickupNotification = "Not enough materials for ${selected.displayName} ($cost)"
            }
            return
        }
        val tileX = ghostTileX ?: return
        val tileY = ghostTileY ?: return

        writeTiles(selected.tile, tileX, tileY)
        if (selected == BuildBlock.TURRET) {
            turretManager.addTurret(tileX, tileY)
        }
        gameState.collectedSimple -= costFor(selected)
    }

    private fun writeTiles(tile: Tile, originTileX: Int, originTileY: Int) {
        for (dy in 0 until tileSize) {
            for (dx in 0 until tileSize) {
                if (isCornerTile(dx, dy)) continue
                val tileX = originTileX + dx
                val tileY = originTileY + dy
                if (!world[tileX, tileY].isIndestructible) {
                    world[tileX, tileY] = tile
                }
            }
        }
    }

    private fun isCornerTile(dx: Int, dy: Int): Boolean =
        (dx == 0 || dx == tileSize - 1) && (dy == 0 || dy == tileSize - 1)

    private fun updateGhostTile() {
        val cursorTileX = (mouse.worldPosition.x / WorldConstants.TILE_SIZE).toInt()
        val cursorTileY = (mouse.worldPosition.y / WorldConstants.TILE_SIZE).toInt()

        // Anchor the 5x5 block so the cursor sits on its middle tile.
        val tileX = cursorTileX - tileSize / 2
        val tileY = cursorTileY - tileSize / 2

        if (Vec2.fastDistance(mouse.worldPosition, pivotPoint) > maxBuildDistanceSq || !isValidTile(tileX, tileY)) {
            ghostTileX = null
            ghostTileY = null
            isGhostValid = false
            return
        }

        ghostTileX = tileX
        ghostTileY = tileY

        if (gameState.collectedSimple < costFor(gameState.selectedBlock)) {
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
                if (isCornerTile(dx, dy)) continue
                val checkX = tileX + dx
                val checkY = tileY + dy
                if (isValidTile(checkX, checkY) && world[checkX, checkY].isIndestructible) {
                    isGhostValid = false
                    return
                }
            }
        }

        isGhostValid = true
    }

    private fun costFor(block: BuildBlock): Int = when (block) {
        BuildBlock.TURRET -> GameSettings.TURRET_COST
        BuildBlock.BOUNCY -> GameSettings.BOUNCY_COST
        BuildBlock.DYNAMITE -> GameSettings.DYNAMITE_COST
        BuildBlock.STONE -> GameSettings.BLOCK_COST
    }
}
