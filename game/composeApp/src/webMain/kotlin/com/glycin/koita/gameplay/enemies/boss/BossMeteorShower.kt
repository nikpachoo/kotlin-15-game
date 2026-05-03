package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.isValidTile
import kotlin.math.sqrt
import kotlin.random.Random

class BossMeteorShower(
    private val origin: () -> Vec2,
    private val world: World,
    private val player: Player,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
) {
    var alive = true
        private set

    private val meteorCount = 30
    private val spawnDuration = 5f
    private val horizontalRange = 400f
    private val spawnHeightAbove = 600f
    private val minVy = 100f
    private val maxVy = 160f
    private val maxVxAbs = 30f
    private val explosionRadius = 60f
    private val damage = 2
    val meteorRadius = 6f
    private val lavaRadiusFrac = 0.6f

    private val tileSize = WorldConstants.TILE_SIZE.toFloat()

    private val meteorX = FloatArray(meteorCount)
    private val meteorY = FloatArray(meteorCount)
    private val meteorVx = FloatArray(meteorCount)
    private val meteorVy = FloatArray(meteorCount)
    private val meteorActive = BooleanArray(meteorCount)
    private var spawnedCount = 0
    private var aliveCount = 0
    private var spawnTimer = 0f

    private val explodePos = Vec2(0f, 0f)

    fun update(deltaTime: Float) {
        if (!alive) return
        spawnTimer += deltaTime
        spawnDue()

        val pc = player.center
        val playerHitRadius = meteorRadius + player.width / 2f
        val playerHitRadiusSq = playerHitRadius * playerHitRadius
        for (i in 0 until meteorCount) {
            if (!meteorActive[i]) continue
            meteorX[i] += meteorVx[i] * deltaTime
            meteorY[i] += meteorVy[i] * deltaTime
            if (Vec2.fastDistance(meteorX[i], meteorY[i], pc.x, pc.y) <= playerHitRadiusSq) {
                detonate(i)
                continue
            }
            val tx = (meteorX[i] / tileSize).toInt()
            val ty = (meteorY[i] / tileSize).toInt()
            if (!isValidTile(tx, ty)) {
                meteorActive[i] = false
                aliveCount--
                continue
            }
            val tile = world[tx, ty]
            if (tile.isSolid && !tile.isFragile) {
                detonate(i)
            }
        }

        if (spawnedCount >= meteorCount && aliveCount == 0) {
            alive = false
        }
    }

    private fun spawnDue() {
        if (spawnedCount >= meteorCount) return
        val expected = ((spawnTimer / spawnDuration) * meteorCount).toInt().coerceAtMost(meteorCount)
        while (spawnedCount < expected) {
            val idx = spawnedCount
            val o = origin()
            meteorX[idx] = o.x + (Random.nextFloat() - 0.5f) * 2f * horizontalRange
            meteorY[idx] = o.y - spawnHeightAbove
            meteorVx[idx] = (Random.nextFloat() - 0.5f) * 2f * maxVxAbs
            meteorVy[idx] = minVy + Random.nextFloat() * (maxVy - minVy)
            meteorActive[idx] = true
            spawnedCount++
            aliveCount++
        }
    }

    private fun detonate(i: Int) {
        meteorActive[i] = false
        aliveCount--
        explodePos.x = meteorX[i]
        explodePos.y = meteorY[i]
        val tiles = collisionDetector.getTilesInRadius(explodePos, explosionRadius)
        explodeTerrain(tiles, explodePos, explosionRadius, world, particleSystem)
        spawnMeteorMaterials(tiles)
        SoundManager.playOneShot(Sounds.EXPLODE)

        val pc = player.center
        val effectiveRadius = explosionRadius + player.width / 2f
        if (Vec2.fastDistance(pc.x, pc.y, explodePos.x, explodePos.y) <= effectiveRadius * effectiveRadius) {
            player.takeDamage(damage)
        }
    }

    private fun spawnMeteorMaterials(tiles: List<Pair<Int, Int>>) {
        val cx = explodePos.x
        val cy = explodePos.y
        val lavaRadius = explosionRadius * lavaRadiusFrac
        val lavaRadiusSq = lavaRadius * lavaRadius
        for (i in 0..<tiles.size) {
            val pair = tiles[i]
            val tx = pair.first
            val ty = pair.second
            if (!isValidTile(tx, ty)) continue
            if (world[tx, ty] != Tile.AIR) continue
            val centerX = (tx + 0.5f) * tileSize
            val centerY = (ty + 0.5f) * tileSize
            val dx = centerX - cx
            val dy = centerY - cy
            val distSq = dx * dx + dy * dy
            val tile = if (distSq <= lavaRadiusSq) Tile.LAVA else Tile.OBSIDIAN
            val mag = sqrt(distSq)
            val speed = 80f + Random.nextFloat() * 80f
            val vx = if (mag > 0.001f) dx / mag * speed else 0f
            val vy = if (mag > 0.001f) dy / mag * speed - 60f else -speed
            particleSystem.addParticle(
                position = Vec2(tx * tileSize, ty * tileSize),
                velocity = Vec2(vx, vy),
                tile = tile,
            )
        }
    }

    fun forEachMeteor(action: (x: Float, y: Float) -> Unit) {
        for (i in 0 until meteorCount) {
            if (!meteorActive[i]) continue
            action(meteorX[i], meteorY[i])
        }
    }
}
