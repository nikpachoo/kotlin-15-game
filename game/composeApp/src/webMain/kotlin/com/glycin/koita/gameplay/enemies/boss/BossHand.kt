package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

//TODO: This sux, come up with something different for it
class BossHand(
    private var tileX: Int,
    private var tileY: Int,
    private val world: World,
) {
    var alive = true
        private set

    private var timer = 0f
    private var smashing = false
    private val hoverDuration = 5f
    private val smashSpeed = 800f
    private var hasHitPlayer = false

    private val tileOffsetsX: IntArray
    private val tileOffsetsY: IntArray
    private val tileTypes: Array<Tile>
    private var remainingTiles = 0
    private var subPixelY = 0f

    init {
        val shape = arrayOf(
            "..................",
            "..................",
            ".........XXXXXXX..",
            "........XOOOOOOX..",
            "....XXXXOOOOOXX...",
            ".XXOOOOOOOOOOOOOX.",
            ".XOOOOOOOXXXXXXX..",
            ".XOOOOOOOOOOOOOOX.",
            ".XOOOOOOOXXXXXXX..",
            ".XOOOOOOOOOOOOO...",
            ".XOOOOOOOXXXXXX...",
            ".XOOOOOOOOOOOO....",
            "..XXOOOOOXXXXX....",
            "....XXXXXXXXX.....",
            "..................",
            "..................",
        )
        val xs = mutableListOf<Int>()
        val ys = mutableListOf<Int>()
        val types = mutableListOf<Tile>()
        for (dy in shape.indices) {
            for (dx in shape[dy].indices) {
                when (shape[dy][dx]) {
                    'X' -> { xs.add(dx); ys.add(dy); types.add(Tile.STONE) }
                    'O' -> { xs.add(dx); ys.add(dy); types.add(Tile.CLAY) }
                }
            }
        }
        tileOffsetsX = xs.toIntArray()
        tileOffsetsY = ys.toIntArray()
        tileTypes = types.toTypedArray()
        placeTiles()
    }

    fun update(deltaTime: Float, player: Player, world: World) {
        if (!alive) return

        timer += deltaTime

        if (remainingTiles <= 0) {
            alive = false
            return
        }

        if (!smashing) {
            val tileSize = WorldConstants.TILE_SIZE
            val targetTX = ((player.center.x - Boss.HAND_WIDTH * tileSize / 2f) / tileSize).toInt()

            if (targetTX != tileX) {
                clearTiles()
                tileX = targetTX
                placeTiles()
            }

            if (timer >= hoverDuration) {
                smashing = true
                subPixelY = 0f
            }
        } else {
            val tileSize = WorldConstants.TILE_SIZE
            subPixelY += smashSpeed * deltaTime

            val tilesToDrop = (subPixelY / tileSize).toInt()
            if (tilesToDrop > 0) {
                subPixelY -= tilesToDrop * tileSize
                clearTiles()
                tileY += tilesToDrop
                placeTiles()
            }

            if (!hasHitPlayer) {
                val handPixelX = (tileX * tileSize).toFloat()
                val handPixelY = (tileY * tileSize).toFloat()
                val handPixelW = (Boss.HAND_WIDTH * tileSize).toFloat()
                val handPixelH = (Boss.HAND_HEIGHT * tileSize).toFloat()

                if (handPixelX < player.position.x + player.width &&
                    handPixelX + handPixelW > player.position.x &&
                    handPixelY < player.position.y + player.height &&
                    handPixelY + handPixelH > player.position.y
                ) {
                    player.takeDamage(3)
                    hasHitPlayer = true
                }
            }

            val bottomTY = tileY + Boss.HAND_HEIGHT
            var hitGround = false
            for (dx in 0 until Boss.HAND_WIDTH) {
                val tx = tileX + dx
                if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                    bottomTY in 0 until WorldConstants.WORLD_HEIGHT_TILES
                ) {
                    if (world[tx, bottomTY].isSolid) {
                        hitGround = true
                        break
                    }
                }
            }

            val tooFar = (tileY * tileSize).toFloat() > player.position.y + 400f
            if (hitGround || tooFar) {
                alive = false
            }
        }
    }

    private fun placeTiles() {
        remainingTiles = 0
        for (i in tileOffsetsX.indices) {
            val tx = tileX + tileOffsetsX[i]
            val ty = tileY + tileOffsetsY[i]
            if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
            ) {
                if (!world[tx, ty].isSolid && !world[tx, ty].isIndestructible) {
                    world[tx, ty] = tileTypes[i]
                    remainingTiles++
                }
            }
        }
    }

    private fun clearTiles() {
        remainingTiles = 0
        for (i in tileOffsetsX.indices) {
            val tx = tileX + tileOffsetsX[i]
            val ty = tileY + tileOffsetsY[i]
            if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
            ) {
                if (world[tx, ty] == tileTypes[i]) {
                    world[tx, ty] = Tile.AIR
                }
            }
        }
    }
}
