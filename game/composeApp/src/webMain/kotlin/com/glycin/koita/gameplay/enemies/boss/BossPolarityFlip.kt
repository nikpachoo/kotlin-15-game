package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlin.random.Random

class BossPolarityFlip(
    private val world: World,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    bossCenter: Vec2,
) {
    var alive = true
        private set

    private val flipDuration = 4f
    private val playerSafeRadius = 2
    private val bossSafeRadius = 6
    private val waterRevertChance = 0.15f
    private val grassRevertChance = 0.33f

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

    private var timer = 0f

    private val bossTx = (bossCenter.x / WorldConstants.TILE_SIZE).toInt()
    private val bossTy = (bossCenter.y / WorldConstants.TILE_SIZE).toInt()

    init {
        flipWorld()
    }

    fun update(deltaTime: Float) {
        if (!alive) return
        timer += deltaTime
        if (timer >= flipDuration) {
            revertWorld()
            alive = false
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
