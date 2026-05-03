package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.isValidTile
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class BossPolarityFlip(
    private val world: World,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    private val bossCenter: Vec2,
) {
    var alive = true
        private set

    private enum class Phase { FLIPPED, PULLING, IMPLODING, EXPLODING }

    private var phase = Phase.FLIPPED
    private var arrivedCount = 0

    private val flipDuration = 4f
    private val implodeDuration = 5f
    private val explodeMaxDuration = 4f

    private val playerSafeRadius = 2
    private val bossSafeRadius = 6
    private val waterRevertChance = 0.15f
    private val grassRevertChance = 0.33f

    private val pullBaseSpeed = 250f
    private val pullAccelerationFactor = 3f
    private val pullCollectDistance = 8f
    private val pullCollectDistSq = pullCollectDistance * pullCollectDistance
    private val pullSpeedAccel = pullBaseSpeed * pullAccelerationFactor

    private val explodeMinSpeed = 200f
    private val explodeMaxSpeed = 450f

    private val tileSize = WorldConstants.TILE_SIZE.toFloat()
    private val tileValues = Tile.entries.toTypedArray()

    private val materialPool = arrayOf(
        Tile.STONE,
        Tile.DIRT,
        Tile.SAND,
        Tile.CLAY,
        Tile.RICH_DIRT,
        Tile.COAL_ORE,
        Tile.IRON_ORE,
        Tile.GOLD_ORE,
        Tile.OBSIDIAN,
        Tile.DEEP_STONE,
    )

    private val maxFlipped = WorldConstants.WORLD_WIDTH_TILES * WorldConstants.SURFACE_Y
    private val flippedTiles = IntArray(maxFlipped)
    private var flippedCount = 0

    private val maxPulled = WorldConstants.WORLD_WIDTH_TILES * WorldConstants.SURFACE_Y
    private val pulledX = FloatArray(maxPulled)
    private val pulledY = FloatArray(maxPulled)
    private val pulledVx = FloatArray(maxPulled)
    private val pulledVy = FloatArray(maxPulled)
    private val pulledTile = IntArray(maxPulled)
    private val pulledActive = BooleanArray(maxPulled)
    private val pulledArrived = BooleanArray(maxPulled)
    private var pulledTotalCount = 0

    private var phaseTimer = 0f

    private val bossTx = (bossCenter.x / WorldConstants.TILE_SIZE).toInt()
    private val bossTy = (bossCenter.y / WorldConstants.TILE_SIZE).toInt()

    init {
        flipWorld()
    }

    fun update(deltaTime: Float) {
        if (!alive) return
        when (phase) {
            Phase.FLIPPED -> updateFlipped(deltaTime)
            Phase.PULLING -> updatePulling(deltaTime)
            Phase.IMPLODING -> updateImploding(deltaTime)
            Phase.EXPLODING -> updateExploding(deltaTime)
        }
    }

    private fun updateFlipped(deltaTime: Float) {
        phaseTimer += deltaTime
        if (phaseTimer >= flipDuration) {
            revertWorld()
            startPull()
        }
    }

    private fun updatePulling(deltaTime: Float) {
        val targetX = bossCenter.x
        val targetY = bossCenter.y
        for (i in 0 until pulledTotalCount) {
            if (!pulledActive[i] || pulledArrived[i]) continue
            val dx = targetX - pulledX[i]
            val dy = targetY - pulledY[i]
            val distSq = dx * dx + dy * dy
            if (distSq < pullCollectDistSq) {
                pulledArrived[i] = true
                pulledX[i] = targetX
                pulledY[i] = targetY
                arrivedCount++
                continue
            }
            val dist = sqrt(distSq)
            val dirX = dx / dist
            val dirY = dy / dist
            val speed = pullBaseSpeed + (1f / (dist / 100f + 1f)) * pullSpeedAccel
            pulledX[i] += dirX * speed * deltaTime
            pulledY[i] += dirY * speed * deltaTime
        }
        if (arrivedCount >= pulledTotalCount) {
            phase = Phase.IMPLODING
            phaseTimer = 0f
        }
    }

    private fun updateImploding(deltaTime: Float) {
        phaseTimer += deltaTime
        if (phaseTimer >= implodeDuration) {
            startExplode()
        }
    }

    private fun updateExploding(deltaTime: Float) {
        phaseTimer += deltaTime
        var anyAlive = false
        for (i in 0 until pulledTotalCount) {
            if (!pulledActive[i]) continue
            val prevX = pulledX[i]
            val prevY = pulledY[i]
            pulledX[i] = prevX + pulledVx[i] * deltaTime
            pulledY[i] = prevY + pulledVy[i] * deltaTime
            val tx = (pulledX[i] / tileSize).toInt()
            val ty = (pulledY[i] / tileSize).toInt()
            if (!isValidTile(tx, ty)) {
                pulledActive[i] = false
                continue
            }
            val tile = world[tx, ty]
            if (tile.isSolid && !tile.isFragile) {
                landAt(i, (prevX / tileSize).toInt(), (prevY / tileSize).toInt())
                continue
            }
            anyAlive = true
        }
        if (!anyAlive || phaseTimer >= explodeMaxDuration) {
            for (i in 0 until pulledTotalCount) {
                if (!pulledActive[i]) continue
                landAt(i, (pulledX[i] / tileSize).toInt(), (pulledY[i] / tileSize).toInt())
            }
            alive = false
        }
    }

    private fun landAt(i: Int, tx: Int, ty: Int) {
        placeSlime(tx, ty)
        pulledActive[i] = false
    }

    private fun placeSlime(tx: Int, ty: Int) {
        if (!isValidTile(tx, ty)) return
        val existing = world[tx, ty]
        if (existing != Tile.AIR && !existing.isLiquid) return
        world[tx, ty] = Tile.SLIME
    }

    private fun startPull() {
        for (ty in 0 until WorldConstants.SURFACE_Y) {
            for (tx in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                if (pulledTotalCount >= maxPulled) break
                val tile = world[tx, ty]
                if (!tile.isSolid) continue
                if (tile.isIndestructible) continue
                world[tx, ty] = Tile.AIR
                val idx = pulledTotalCount++
                pulledX[idx] = tx * tileSize
                pulledY[idx] = ty * tileSize
                pulledTile[idx] = tile.ordinal
                pulledActive[idx] = true
                pulledArrived[idx] = false
            }
        }
        phase = Phase.PULLING
        phaseTimer = 0f
        if (pulledTotalCount == 0) {
            alive = false
        }
    }

    private fun startExplode() {
        for (i in 0 until pulledTotalCount) {
            if (!pulledActive[i]) continue
            val angle = Random.nextFloat() * TWO_PI
            val speed = explodeMinSpeed + Random.nextFloat() * (explodeMaxSpeed - explodeMinSpeed)
            pulledVx[i] = cos(angle) * speed
            pulledVy[i] = sin(angle) * speed
        }
        phase = Phase.EXPLODING
        phaseTimer = 0f
    }

    fun forEachPulledTile(action: (x: Float, y: Float, tile: Tile) -> Unit) {
        if (phase == Phase.FLIPPED) return
        val displayAsSlime = phase == Phase.IMPLODING || phase == Phase.EXPLODING
        for (i in 0 until pulledTotalCount) {
            if (!pulledActive[i]) continue
            val tile = if (displayAsSlime) Tile.SLIME else tileValues[pulledTile[i]]
            action(pulledX[i], pulledY[i], tile)
        }
    }

    private fun flipWorld() {
        val pc = player.center
        val playerTx = (pc.x / WorldConstants.TILE_SIZE).toInt()
        val playerTy = (pc.y / WorldConstants.TILE_SIZE).toInt()

        for (ty in 0 until WorldConstants.SURFACE_Y) {
            for (tx in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                if (inSafeZone(tx, ty, playerTx, playerTy, playerSafeRadius)) continue
                if (inSafeZone(tx, ty, bossTx, bossTy, bossSafeRadius)) continue
                val tile = world[tx, ty]
                when {
                    tile.isIndestructible -> continue
                    tile == Tile.AIR -> {
                        world[tx, ty] = randomMaterial()
                        recordFlip(tx, ty)
                    }
                    else -> {
                        world[tx, ty] = Tile.AIR
                    }
                }
            }
        }
    }

    private fun revertWorld() {
        for (i in 0 until flippedCount) {
            val packed = flippedTiles[i]
            val tx = (packed ushr 16) and 0xFFFF
            val ty = packed and 0xFFFF
            val current = world[tx, ty]
            if (current == Tile.AIR) continue
            if (current.isIndestructible) continue
            val roll = Random.nextFloat()
            when {
                roll < waterRevertChance -> {
                    world[tx, ty] = Tile.WATER
                    fluidSimulator.registerFluid(tx, ty)
                }
                roll < waterRevertChance + grassRevertChance -> {
                    world[tx, ty] = Tile.GRASS
                }
                else -> {
                    world[tx, ty] = Tile.AIR
                }
            }
        }
    }

    private fun randomMaterial(): Tile = materialPool[Random.nextInt(materialPool.size)]

    private fun recordFlip(tx: Int, ty: Int) {
        if (flippedCount >= maxFlipped) return
        flippedTiles[flippedCount++] = ((tx and 0xFFFF) shl 16) or (ty and 0xFFFF)
    }

    private fun inSafeZone(tx: Int, ty: Int, cx: Int, cy: Int, r: Int): Boolean {
        return tx in cx - r..cx + r && ty in cy - r..cy + r
    }
}
