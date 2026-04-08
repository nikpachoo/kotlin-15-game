package com.glycin.koita.gameplay.turrets

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.world.WorldConstants

class TurretManager(
    private val enemyManager: EnemyManager,
    private val collisionDetector: CollisionDetector,
    private val gameState: GameState,
) {

    private val turrets = mutableListOf<Turret>()

    fun addTurret(tileX: Int, tileY: Int) {
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        val baseSize = 5
        val centerX = (tileX + baseSize / 2f) * tileSize
        val topY = tileY * tileSize - Turret.SPHERE_SIZE

        turrets.add(
            Turret(
                tileX = tileX,
                tileY = tileY,
                position = Vec2(centerX - Turret.SPHERE_SIZE / 2f, topY),
                collisionDetector = collisionDetector,
                enemyManager = enemyManager,
                gameState = gameState,
            )
        )
    }

    fun update(deltaTime: Float) {
        for (i in 0..<turrets.size) {
            turrets[i].update(deltaTime)
        }
    }

    fun forEachTurret(action: (Turret) -> Unit) {
        for (i in 0..<turrets.size) {
            action(turrets[i])
        }
    }

    fun forEachMissile(action: (TurretMissile) -> Unit) {
        for (i in 0..<turrets.size) {
            turrets[i].forEachMissile(action)
        }
    }
}
