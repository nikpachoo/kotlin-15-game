package com.glycin.koita.gameplay.turrets

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector

class Turret(
    val tileX: Int,
    val tileY: Int,
    val position: Vec2,
    private val collisionDetector: CollisionDetector,
    private val enemyManager: EnemyManager,
    private val gameState: GameState,
) {

    private val missiles = mutableListOf<TurretMissile>()
    private var shootCooldown = SHOOT_INTERVAL

    fun update(deltaTime: Float) {
        for (i in 0..<missiles.size) {
            missiles[i].update(deltaTime)
        }
        missiles.removeAll { !it.isAlive }

        shootCooldown -= deltaTime
        if (shootCooldown <= 0f) {
            val targetCenter = enemyManager.findNearestTargetCenter(position, RANGE)
            if (targetCenter != null) {
                val direction = (targetCenter - position).normalized()
                missiles.add(
                    TurretMissile(
                        position = position.copy(),
                        direction = direction,
                        collisionDetector = collisionDetector,
                        enemyManager = enemyManager,
                        gameState = gameState,
                    )
                )
            }
            shootCooldown = SHOOT_INTERVAL
        }
    }

    fun forEachMissile(action: (TurretMissile) -> Unit) {
        for (i in 0..<missiles.size) {
            val m = missiles[i]
            if (m.isAlive) action(m)
        }
    }

    companion object {
        const val SHOOT_INTERVAL = 1.5f
        const val RANGE = 400f
        const val SPHERE_SIZE = 20f
    }
}
