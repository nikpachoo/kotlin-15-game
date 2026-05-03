package com.glycin.koita.gameplay.turrets

import com.glycin.koita.core.SpriteSheet
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.weapons_sprite_sheet
import kotlin.math.sqrt

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
    val aimDirection: Vec2 = Vec2(1f, 0f)

    fun update(deltaTime: Float) {
        for (i in 0..<missiles.size) {
            missiles[i].update(deltaTime)
        }
        missiles.removeAll { !it.isAlive }

        val targetCenter = enemyManager.findNearestTargetCenter(position, RANGE)
        if (targetCenter != null) {
            val dx = targetCenter.x - position.x
            val dy = targetCenter.y - position.y
            val mag = sqrt(dx * dx + dy * dy)
            if (mag > 0f) {
                aimDirection.x = dx / mag
                aimDirection.y = dy / mag
            }
        }

        shootCooldown -= deltaTime
        if (shootCooldown <= 0f && targetCenter != null) {
            missiles.add(
                TurretMissile(
                    position = position.copy(),
                    direction = aimDirection.copy(),
                    collisionDetector = collisionDetector,
                    enemyManager = enemyManager,
                    gameState = gameState,
                )
            )
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
        const val SPRITE_SIZE = 24f
        const val SPRITE_OFFSET_X = (SPHERE_SIZE - SPRITE_SIZE) / 2f
        const val SPRITE_OFFSET_Y = (SPHERE_SIZE - SPRITE_SIZE) / 2f

        val SPRITE = SpriteSheet(
            sprite = Res.drawable.weapons_sprite_sheet,
            frameWidth = 32,
            frameHeight = 32,
            columns = 4,
        ).frame(0)
    }
}
