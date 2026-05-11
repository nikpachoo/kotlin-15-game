package com.glycin.koita.gameplay.turrets

import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.util.isOutOfWorldBounds

class TurretMissile(
    position: Vec2,
    val direction: Vec2,
    private val collisionDetector: CollisionDetector,
    private val enemyManager: EnemyManager,
    private val gameState: GameState,
) {

    var position: Vec2 = position
        private set

    var isAlive = true
        private set

    fun update(deltaTime: Float) {
        if (!isAlive) return

        val newPos = position + direction * SPEED * deltaTime

        if (collisionDetector.isSolidAtPosition(newPos)) {
            isAlive = false
            return
        }

        if (newPos.isOutOfWorldBounds()) {
            isAlive = false
            return
        }

        position = newPos

        if (enemyManager.damageFirstColliding(position.x, position.y, SIZE, SIZE, DAMAGE * gameState.damageMultiplier)) {
            isAlive = false
        }
    }

    companion object {
        const val SPEED = 500f
        const val DAMAGE = 1f
        const val SIZE = 6f
    }
}
