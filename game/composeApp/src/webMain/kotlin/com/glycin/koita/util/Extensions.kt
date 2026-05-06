package com.glycin.koita.util

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.isOutOfWorldBounds

fun Float.lerp(b: Float, t: Float) = this + t * (b - this)

fun <T> List<T>.nextAfter(current: T): T {
    val idx = indexOf(current).coerceAtLeast(0)
    return this[(idx + 1) % size]
}

fun <T> List<T>.prevBefore(current: T): T {
    val idx = indexOf(current).coerceAtLeast(0)
    return this[(idx - 1 + size) % size]
}

fun Vec2.isOutOfWorldBounds(): Boolean = isOutOfWorldBounds(x, y)

fun Tile.activate(tileX: Int, tileY: Int, sourcePosition: Vec2, particleSystem: ParticleSystem, impactRadius: Float) {
    val tileCenter = Vec2(
        tileX * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2f,
        tileY * WorldConstants.TILE_SIZE + WorldConstants.TILE_SIZE / 2f
    )

    val direction = (tileCenter - sourcePosition).normalized()
    val distance = Vec2.distance(sourcePosition, tileCenter)

    val forceFactor = 1f - (distance / impactRadius).coerceIn(0f, 1f)
    val force = 500f * forceFactor
    
    particleSystem.addParticle(
        position = Vec2(
            tileX * WorldConstants.TILE_SIZE.toFloat(),
            tileY * WorldConstants.TILE_SIZE.toFloat()
        ),
        velocity = direction * force,
        tile = this
    )
}

fun Player.overlapsWith(otherPos: Vec2, otherWidth: Int, otherHeight: Int): Boolean {
    return otherPos.x < position.x + width &&
            otherPos.x + otherWidth > position.x &&
            otherPos.y < position.y + height &&
            otherPos.y + otherHeight > position.y
}

fun explodeTerrain(
    affectedTiles: List<Pair<Int, Int>>,
    sourcePosition: Vec2,
    impactRadius: Float,
    world: World,
    particleSystem: ParticleSystem,
): Boolean {
    var hitIndestructible = false
    for (i in 0..<affectedTiles.size) {
        val pair = affectedTiles[i]
        val tileX = pair.first
        val tileY = pair.second
        val tile = world[tileX, tileY]
        when {
            tile.isIndestructible -> hitIndestructible = true
            tile.isFragile -> world[tileX, tileY] = Tile.AIR
            tile != Tile.AIR -> {
                tile.activate(tileX, tileY, sourcePosition, particleSystem, impactRadius)
                world[tileX, tileY] = Tile.AIR
            }
        }
    }
    return hitIndestructible
}