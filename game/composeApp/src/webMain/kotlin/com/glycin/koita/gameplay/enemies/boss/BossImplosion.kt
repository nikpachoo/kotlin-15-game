package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

//TODO: Also not very fun, change it
class BossImplosion(
    private val origin: () -> Vec2,
    private val world: World,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val pullRadius: Float = 800f,
    private val gatherDuration: Float = 3f,
    private val holdDuration: Float = 1f,
) {
    var alive = true
        private set

    private var timer = 0f
    private var exploded = false

    private val tilePositionsX = IntArray(MAX_TILES)
    private val tilePositionsY = IntArray(MAX_TILES)
    private val tileThresholds = FloatArray(MAX_TILES)
    private val tileActive = BooleanArray(MAX_TILES)

    private val renderPositionsX = FloatArray(MAX_TILES)
    private val renderPositionsY = FloatArray(MAX_TILES)
    private var tileCount = 0
    private val totalDuration = gatherDuration + holdDuration

    init {
        prepareTiles()
    }

    fun update(deltaTime: Float) {
        if (!alive) return

        timer += deltaTime

        val center = origin()
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        val gatherProgress = (timer / gatherDuration).coerceAtMost(1f)

        for (i in 0..tileCount) {
            val threshold = tileThresholds[i]

            if (!tileActive[i]) {
                if (gatherProgress >= threshold) {
                    tileActive[i] = true
                    world[tilePositionsX[i], tilePositionsY[i]] = Tile.AIR
                }
            }

            if (tileActive[i]) {
                val tileLerp = ((gatherProgress - threshold) / (1f - threshold).coerceAtLeast(0.01f)).coerceIn(0f, 1f)
                val worldX = tilePositionsX[i] * tileSize
                val worldY = tilePositionsY[i] * tileSize
                renderPositionsX[i] = worldX + (center.x - worldX) * tileLerp
                renderPositionsY[i] = worldY + (center.y - worldY) * tileLerp
            }
        }

        if (timer >= totalDuration && !exploded) {
            exploded = true
            explode(center)
            alive = false
        }
    }

    private fun prepareTiles() {
        val center = origin()
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        val tiles = collisionDetector.getTilesInRadius(center, pullRadius)
        val distances = FloatArray(MAX_TILES)

        for ((tileX, tileY) in tiles) {
            if (tileCount >= MAX_TILES) break
            val tile = world[tileX, tileY]
            if (tile == Tile.AIR || tile.isIndestructible || tile == Tile.WATER) continue
            val dx = tileX * tileSize + tileSize / 2f - center.x
            val dy = tileY * tileSize + tileSize / 2f - center.y
            tilePositionsX[tileCount] = tileX
            tilePositionsY[tileCount] = tileY
            distances[tileCount] = dx * dx + dy * dy
            tileCount++
        }

        sortByDistance(distances)

        val maxDist = if (tileCount > 0) kotlin.math.sqrt(distances[tileCount - 1]) else 1f
        for (i in 0..tileCount) {
            tileThresholds[i] = if (maxDist > 0f) kotlin.math.sqrt(distances[i]) / maxDist else 0f
        }
    }

    private fun sortByDistance(distances: FloatArray) {
        for (i in 1 downTo tileCount) {
            val distKey = distances[i]
            val xKey = tilePositionsX[i]
            val yKey = tilePositionsY[i]
            var j = i - 1
            while (j >= 0 && distances[j] > distKey) {
                distances[j + 1] = distances[j]
                tilePositionsX[j + 1] = tilePositionsX[j]
                tilePositionsY[j + 1] = tilePositionsY[j]
                j--
            }
            distances[j + 1] = distKey
            tilePositionsX[j + 1] = xKey
            tilePositionsY[j + 1] = yKey
        }
    }

    private fun explode(center: Vec2) {
        for (i in 0..tileCount) {
            if (!tileActive[i]) continue
            val angle = Random.nextFloat() * TWO_PI
            val speed = 100f + Random.nextFloat() * 200f
            particleSystem.addParticle(
                position = center,
                velocity = Vec2(cos(angle) * speed, sin(angle) * speed),
                tile = Tile.LAVA,
            )
        }
    }

    fun forEachGatheredTile(action: (x: Float, y: Float) -> Unit) {
        if (!alive) return
        for (i in 0..tileCount) {
            if (!tileActive[i]) continue
            action(renderPositionsX[i], renderPositionsY[i])
        }
    }

    companion object {
        private const val MAX_TILES = 2048
    }
}
