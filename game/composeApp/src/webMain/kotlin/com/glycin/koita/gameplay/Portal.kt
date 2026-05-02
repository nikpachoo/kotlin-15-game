package com.glycin.koita.gameplay

import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.boss.Boss
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

class Portal(
    private val gameState: GameState,
    private val world: World,
    private val camera: Camera,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
) {
    var boss: Boss? = null
        private set

    private var bossSpawnTimer = 0f
    private var portalCleared = false

    fun update(deltaTime: Float) {
        if (!gameState.passedPortal) return

        if (!portalCleared) {
            portalCleared = true
            enemyManager.clearAll()
            val visibleChunks = world.getVisibleChunks(camera, camera.canvasWidth, camera.canvasHeight)
            visibleChunks.forEach { it.fillWith(Tile.AIR) }
        }

        if (!gameState.bossSpawned) {
            bossSpawnTimer += deltaTime
            if (bossSpawnTimer >= 3f) {
                val bossX = WorldConstants.WORLD_WIDTH_PIXELS / 2f - 64f / 2f
                val bossY = player.position.y - 50f * WorldConstants.TILE_SIZE
                boss = Boss(
                    position = Vec2(bossX, bossY),
                    world = world,
                    player = player,
                    fluidSimulator = fluidSimulator,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    gameState = gameState,
                ).also { enemyManager.setBoss(it) }
                gameState.bossSpawned = true
            }
        }

        boss?.update(deltaTime)
    }
}
