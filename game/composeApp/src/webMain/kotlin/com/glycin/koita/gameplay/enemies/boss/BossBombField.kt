package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.isValidTile
import kotlin.math.sin
import kotlin.random.Random

class BossBombField(
    private val world: World,
    private val player: Player,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
) {
    var alive = true
        private set

    private val bombCount = 100
    private val blockHalfTiles = 2
    private val minFuseDuration = 10f
    private val maxFuseDuration = 15f
    private val explosionRadius = 150f
    private val playerDamage = 2
    private val placementAttempts = 16
    private val warnFuseDuration = 3f
    private val basePulse = 2f
    private val maxPulse = 16f
    private val defuseCheckInterval = 0.15f

    private val bombTilesX = IntArray(bombCount)
    private val bombTilesY = IntArray(bombCount)
    private val fuseTimers = FloatArray(bombCount)
    private val pulsePhases = FloatArray(bombCount)
    private val bombActive = BooleanArray(bombCount)
    private var activeCount = 0
    private var defuseCheckTimer = 0f

    private val arenaMin = Vec2(
        blockHalfTiles.toFloat(),
        blockHalfTiles.toFloat(),
    )
    private val arenaMax = Vec2(
        (WorldConstants.WORLD_WIDTH_TILES - blockHalfTiles).toFloat(),
        (WorldConstants.SURFACE_Y - blockHalfTiles).toFloat(),
    )

    private val explodePos = Vec2(0f, 0f)

    init {
        spawnBombs()
    }

    fun update(deltaTime: Float) {
        if (!alive) return
        defuseCheckTimer -= deltaTime
        val checkDefuse = defuseCheckTimer <= 0f
        if (checkDefuse) defuseCheckTimer = defuseCheckInterval

        for (i in 0 until bombCount) {
            if (!bombActive[i]) continue
            if (checkDefuse && clusterDefused(i)) {
                bombActive[i] = false
                activeCount--
                continue
            }
            fuseTimers[i] -= deltaTime
            val urgency = urgencyOf(i)
            if (urgency > 0f) {
                pulsePhases[i] += (basePulse + urgency * (maxPulse - basePulse)) * deltaTime
            }
            if (fuseTimers[i] <= 0f) detonate(i)
        }
        if (activeCount == 0) alive = false
    }

    private inline fun forEach5x5(centerTx: Int, centerTy: Int, action: (tx: Int, ty: Int) -> Unit) {
        for (dy in -blockHalfTiles..blockHalfTiles) {
            for (dx in -blockHalfTiles..blockHalfTiles) {
                action(centerTx + dx, centerTy + dy)
            }
        }
    }

    private fun clusterDefused(i: Int): Boolean {
        forEach5x5(bombTilesX[i], bombTilesY[i]) { tx, ty ->
            if (isValidTile(tx, ty) && world[tx, ty] == Tile.DYNAMITE) return false
        }
        return true
    }

    fun forEachBomb(action: (topLeftX: Float, topLeftY: Float, sizePx: Float, flashAlpha: Float) -> Unit) {
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        val sizePx = (blockHalfTiles * 2 + 1) * tileSize
        for (i in 0 until bombCount) {
            if (!bombActive[i]) continue
            val urgency = urgencyOf(i)
            if (urgency <= 0f) continue
            val pulse = sin(pulsePhases[i] * TWO_PI) * 0.5f + 0.5f
            val flashAlpha = urgency * (0.3f + 0.7f * pulse)
            val tlx = (bombTilesX[i] - blockHalfTiles) * tileSize
            val tly = (bombTilesY[i] - blockHalfTiles) * tileSize
            action(tlx, tly, sizePx, flashAlpha)
        }
    }

    private fun urgencyOf(i: Int): Float {
        val fuse = fuseTimers[i]
        if (fuse >= warnFuseDuration) return 0f
        if (fuse <= 0f) return 1f
        return 1f - fuse / warnFuseDuration
    }

    private fun spawnBombs() {
        for (i in 0 until bombCount) {
            for (attempt in 0 until placementAttempts) {
                val tx = Random.nextInt(arenaMin.x.toInt(), arenaMax.x.toInt())
                val ty = Random.nextInt(arenaMin.y.toInt(), arenaMax.y.toInt())
                if (!is5x5Clear(tx, ty)) continue
                place5x5(tx, ty)
                bombTilesX[i] = tx
                bombTilesY[i] = ty
                fuseTimers[i] = minFuseDuration + Random.nextFloat() * (maxFuseDuration - minFuseDuration)
                pulsePhases[i] = Random.nextFloat()
                bombActive[i] = true
                activeCount++
                break
            }
        }
        if (activeCount == 0) alive = false
    }

    private fun is5x5Clear(centerTx: Int, centerTy: Int): Boolean {
        forEach5x5(centerTx, centerTy) { tx, ty ->
            if (!isValidTile(tx, ty) || world[tx, ty] != Tile.AIR) return false
        }
        return true
    }

    private fun place5x5(centerTx: Int, centerTy: Int) {
        forEach5x5(centerTx, centerTy) { tx, ty ->
            world[tx, ty] = Tile.DYNAMITE
        }
    }

    private fun detonate(i: Int) {
        bombActive[i] = false
        activeCount--

        val centerTx = bombTilesX[i]
        val centerTy = bombTilesY[i]

        var anyDynamite = false
        forEach5x5(centerTx, centerTy) { tx, ty ->
            if (isValidTile(tx, ty) && world[tx, ty] == Tile.DYNAMITE) {
                world[tx, ty] = Tile.STONE
                anyDynamite = true
            }
        }
        if (!anyDynamite) return

        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        explodePos.x = (centerTx + 0.5f) * tileSize
        explodePos.y = (centerTy + 0.5f) * tileSize

        val affected = collisionDetector.getTilesInRadius(explodePos, explosionRadius)
        explodeTerrain(affected, explodePos, explosionRadius, world, particleSystem)
        SoundManager.playOneShot(Sounds.EXPLODE)

        val pc = player.center
        val pdx = pc.x - explodePos.x
        val pdy = pc.y - explodePos.y
        val effectiveRadius = explosionRadius + player.width / 2f
        if (pdx * pdx + pdy * pdy <= effectiveRadius * effectiveRadius) {
            player.takeDamage(playerDamage)
        }
    }
}
