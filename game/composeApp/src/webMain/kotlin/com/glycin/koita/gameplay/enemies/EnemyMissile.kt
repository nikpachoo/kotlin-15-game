package com.glycin.koita.gameplay.enemies

import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.activate
import com.glycin.koita.util.isOutOfWorldBounds
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World

class EnemyMissile(
    var position: Vec2,
    val size: Float = 12f,
    private val speed: Float = 400f,
    private val impactRadius: Float = 50f,
    private val collisionDetector: CollisionDetector,
    private val world: World,
    private val particleSystem: ParticleSystem,
    direction: Vec2,
) {

    var isAlive = true
    private val direction = direction.normalized()
    val center get() = Vec2(position.x + size / 2f, position.y + size / 2f)

    fun update(deltaTime: Float) {
        if (!isAlive) return

        val newPos = position + (direction * speed * deltaTime)

        if (collisionDetector.isSolidOrShieldAtPosition(newPos)) {
            explode()
            isAlive = false
            return
        }

        if (newPos.isOutOfWorldBounds()) {
            isAlive = false
            return
        }

        position = newPos
    }

    private fun explode() {
        val affectedTiles = collisionDetector.getTilesInRadius(position, impactRadius)
        affectedTiles.forEach { (tileX, tileY) ->
            val tile = world[tileX, tileY]
            when {
                tile == Tile.SHIELD -> {
                    Tile.STONE.activate(tileX, tileY, position, particleSystem, impactRadius)
                    world[tileX, tileY] = Tile.AIR
                }
                tile != Tile.AIR && !tile.isIndestructible -> {
                    tile.activate(tileX, tileY, position, particleSystem, impactRadius)
                    world[tileX, tileY] = Tile.AIR
                }
            }
        }
    }

    fun checkPlayerCollision(playerPos: Vec2, playerWidth: Float, playerHeight: Float): Boolean {
        if (!isAlive) return false

        return center.x >= playerPos.x &&
               center.x <= playerPos.x + playerWidth &&
               center.y >= playerPos.y &&
               center.y <= playerPos.y + playerHeight
    }
}
