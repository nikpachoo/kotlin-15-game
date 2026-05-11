package com.glycin.koita.gameplay.tutorial

import com.glycin.koita.core.Player
import com.glycin.koita.core.PlayerFacing
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.enemies.Slime
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.upgrades.Shrine
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.gameplay.upgrades.UnlockId
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

private const val MINING_TARGET_GAP_TILES = 2
private const val MINING_TARGET_WIDTH_TILES = 30
private const val MINING_TARGET_HEIGHT_TILES = 20

private const val LAVA_POOL_GAP_TILES = 3
private const val LAVA_POOL_WIDTH_TILES = 10
private const val LAVA_POOL_DEPTH_TILES = 6
private const val LAVA_POOL_WALL_LIP_TILES = 1

private const val SLIME_CAVE_GAP_TILES = 4
private const val SLIME_CAVE_INTERIOR_WIDTH_TILES = 12
private const val SLIME_CAVE_INTERIOR_HEIGHT_TILES = 10
private const val SLIME_CAVE_OPENING_HEIGHT_TILES = 2
private const val SLIME_CAVE_OPENING_BOTTOM_OFFSET_TILES = 5
private const val SLIME_HITBOX_WIDTH_TILES = 8
private const val SLIME_HITBOX_HEIGHT_TILES = 8

private const val GOLD_CLUSTER_GAP_TILES = 3
private const val GOLD_CLUSTER_WIDTH_TILES = 4
private const val GOLD_CLUSTER_HEIGHT_TILES = 4

private const val HEART_CHAMBER_GAP_TILES = 3
private const val HEART_CHAMBER_SIZE_TILES = 12
private const val HEART_CHAMBER_CAVITY_RADIUS_TILES = 4

private const val SHRINE_GAP_TILES = 4
private const val SHRINE_WIDTH_TILES = 16
private const val SHRINE_HEIGHT_TILES = 32

private const val DIG_UP_POCKET_PADDING_TILES = 1

data class TileRect(val left: Int, val right: Int, val top: Int, val bottom: Int)

data class LavaPoolLayout(val leftWallX: Int, val rightWallX: Int, val wallTop: Int, val poolBottom: Int)

object TutorialWorldBuilder {

    fun build(world: World) {
        paintWorldBounds(world)
    }

    fun clearNonIndestructible(world: World) {
        for (cy in 0 until world.chunksHigh) {
            for (cx in 0 until world.chunksWide) {
                world.getChunk(cx, cy)?.fillWith(Tile.AIR)
            }
        }
    }

    fun placeMiningTarget(world: World, player: Player): TileRect {
        val centerTileY = (player.center.y / WorldConstants.TILE_SIZE).toInt()
        val top = centerTileY - MINING_TARGET_HEIGHT_TILES / 2
        val bottom = top + MINING_TARGET_HEIGHT_TILES - 1
        val left = structureLeftTile(player, MINING_TARGET_GAP_TILES, MINING_TARGET_WIDTH_TILES)
        val right = left + MINING_TARGET_WIDTH_TILES - 1

        for (y in top..bottom) {
            for (x in left..right) {
                setIfDestructible(world, x, y, if (y == bottom) Tile.BEDROCK else Tile.STONE)
            }
        }

        return TileRect(left, right, top, bottom)
    }

    fun placeLavaPool(world: World, player: Player): LavaPoolLayout {
        val structureWidth = LAVA_POOL_WIDTH_TILES + 2
        val structureLeft = structureLeftTile(player, LAVA_POOL_GAP_TILES, structureWidth)
        val lavaLeft = structureLeft + 1
        val lavaRight = lavaLeft + LAVA_POOL_WIDTH_TILES - 1
        val rightWallX = lavaRight + 1
        val poolBottom = WorldConstants.WORLD_HEIGHT_TILES - 2
        val lavaTop = poolBottom - LAVA_POOL_DEPTH_TILES + 1
        val wallTop = lavaTop - LAVA_POOL_WALL_LIP_TILES

        for (y in wallTop..poolBottom) {
            setIfDestructible(world, structureLeft, y, Tile.KOTLINIUM)
            setIfDestructible(world, rightWallX, y, Tile.KOTLINIUM)
        }
        for (y in lavaTop..poolBottom) {
            for (x in lavaLeft..lavaRight) {
                setIfDestructible(world, x, y, Tile.LAVA)
            }
        }

        return LavaPoolLayout(structureLeft, rightWallX, wallTop, poolBottom)
    }

    fun fillWorldSolid(world: World, player: Player) {
        for (cy in 0 until world.chunksHigh) {
            for (cx in 0 until world.chunksWide) {
                world.getChunk(cx, cy)?.fillWith(Tile.STONE)
            }
        }

        val tileSize = WorldConstants.TILE_SIZE
        val playerLeftTile = (player.position.x / tileSize).toInt() - DIG_UP_POCKET_PADDING_TILES
        val playerRightTile = ((player.position.x + player.width) / tileSize).toInt() + DIG_UP_POCKET_PADDING_TILES
        val playerTopTile = (player.position.y / tileSize).toInt()
        val playerBottomTile = ((player.position.y + player.height) / tileSize).toInt()

        for (y in playerTopTile..playerBottomTile) {
            for (x in playerLeftTile..playerRightTile) {
                setIfDestructible(world, x, y, Tile.AIR)
            }
        }

        val digUpTiles = (TutorialConstants.DIG_UP_THRESHOLD_PX / tileSize).toInt()
        val surfaceTileY = (playerTopTile - digUpTiles).coerceAtLeast(0)
        for (cy in 0 until world.chunksHigh) {
            val chunkTopTile = cy * WorldConstants.CHUNK_SIZE
            val chunkBottomTile = chunkTopTile + WorldConstants.CHUNK_SIZE - 1
            when {
                chunkBottomTile < surfaceTileY -> {
                    for (cx in 0 until world.chunksWide) {
                        world.getChunk(cx, cy)?.fillWith(Tile.AIR)
                    }
                }
                chunkTopTile < surfaceTileY -> {
                    for (y in chunkTopTile until surfaceTileY) {
                        for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                            setIfDestructible(world, x, y, Tile.AIR)
                        }
                    }
                }
            }
        }
    }

    fun clearLavaPool(world: World, layout: LavaPoolLayout) {
        for (y in layout.wallTop..layout.poolBottom) {
            world[layout.leftWallX, y] = Tile.AIR
            world[layout.rightWallX, y] = Tile.AIR
        }
    }

    fun spawnSlimeInCave(
        world: World,
        player: Player,
        collisionDetector: CollisionDetector,
        particleSystem: ParticleSystem,
        enemyManager: EnemyManager,
    ): Slime {
        val tileSize = WorldConstants.TILE_SIZE
        val structureWidth = SLIME_CAVE_INTERIOR_WIDTH_TILES + 2
        val structureLeft = structureLeftTile(player, SLIME_CAVE_GAP_TILES, structureWidth)
        val leftWallX = structureLeft
        val interiorLeft = structureLeft + 1
        val interiorRight = interiorLeft + SLIME_CAVE_INTERIOR_WIDTH_TILES - 1
        val rightWallX = interiorRight + 1
        val cavityBottom = WorldConstants.WORLD_HEIGHT_TILES - 2
        val cavityTop = cavityBottom - SLIME_CAVE_INTERIOR_HEIGHT_TILES + 1
        val ceilingY = cavityTop - 1

        val openingBottom = cavityBottom - SLIME_CAVE_OPENING_BOTTOM_OFFSET_TILES
        val openingTop = openingBottom - SLIME_CAVE_OPENING_HEIGHT_TILES + 1
        val openingOnLeft = player.facing == PlayerFacing.RIGHT
        val openingRange = openingTop..openingBottom

        for (y in cavityTop..cavityBottom) {
            if (!(openingOnLeft && y in openingRange)) {
                setIfDestructible(world, leftWallX, y, Tile.STONE)
            }
            if (!(!openingOnLeft && y in openingRange)) {
                setIfDestructible(world, rightWallX, y, Tile.STONE)
            }
        }
        for (x in leftWallX..rightWallX) {
            setIfDestructible(world, x, ceilingY, Tile.STONE)
        }

        val slimeTileX = (interiorLeft + interiorRight) / 2 - SLIME_HITBOX_WIDTH_TILES / 2
        val slimeTileY = cavityBottom - SLIME_HITBOX_HEIGHT_TILES + 1
        val slime = Slime(
            position = Vec2((slimeTileX * tileSize).toFloat(), (slimeTileY * tileSize).toFloat()),
            collisionDetector = collisionDetector,
            world = world,
            particleSystem = particleSystem,
        )
        enemyManager.add(slime)
        return slime
    }

    fun placeGoldOreCluster(world: World, player: Player): TileRect {
        val centerTileY = (player.center.y / WorldConstants.TILE_SIZE).toInt()
        val top = centerTileY - GOLD_CLUSTER_HEIGHT_TILES / 2
        val bottom = top + GOLD_CLUSTER_HEIGHT_TILES - 1
        val left = structureLeftTile(player, GOLD_CLUSTER_GAP_TILES, GOLD_CLUSTER_WIDTH_TILES)
        val right = left + GOLD_CLUSTER_WIDTH_TILES - 1

        for (y in top..bottom) {
            for (x in left..right) {
                setIfDestructible(world, x, y, Tile.GOLD_ORE)
            }
        }

        return TileRect(left, right, top, bottom)
    }

    fun placeHeartChamber(world: World, pickupManager: PickupManager, player: Player) {
        val tileSize = WorldConstants.TILE_SIZE
        val left = structureLeftTile(player, HEART_CHAMBER_GAP_TILES, HEART_CHAMBER_SIZE_TILES)
        val right = left + HEART_CHAMBER_SIZE_TILES - 1
        val bottom = WorldConstants.WORLD_HEIGHT_TILES - 2
        val top = bottom - HEART_CHAMBER_SIZE_TILES + 1
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2
        val radiusSq = HEART_CHAMBER_CAVITY_RADIUS_TILES * HEART_CHAMBER_CAVITY_RADIUS_TILES

        for (y in top..bottom) {
            for (x in left..right) {
                val dx = x - centerX
                val dy = y - centerY
                val tile = if (dx * dx + dy * dy <= radiusSq) Tile.AIR else Tile.STONE
                setIfDestructible(world, x, y, tile)
            }
        }

        val heartPosition = Vec2(
            (centerX * tileSize).toFloat() - 16f,
            (centerY * tileSize).toFloat() - 16f,
        )
        pickupManager.spawnHealth(heartPosition)
    }

    fun spawnTutorialShrine(
        shrineManager: ShrineManager,
        upgradeRepository: UpgradeRepository,
        player: Player,
    ): Shrine {
        val tileSize = WorldConstants.TILE_SIZE
        val left = structureLeftTile(player, SHRINE_GAP_TILES, SHRINE_WIDTH_TILES)
        val bottom = WorldConstants.WORLD_HEIGHT_TILES - 1
        val top = bottom - SHRINE_HEIGHT_TILES
        val choices = listOfNotNull(upgradeRepository.getById(UnlockId.LASER))
        val shrine = Shrine(
            position = Vec2((left * tileSize).toFloat(), (top * tileSize).toFloat()),
            choices = choices,
        )
        shrineManager.add(shrine)
        return shrine
    }

    private fun structureLeftTile(player: Player, gapTiles: Int, structureWidthTiles: Int): Int {
        val tileSize = WorldConstants.TILE_SIZE
        val raw = if (player.facing == PlayerFacing.RIGHT) {
            ((player.position.x + player.width) / tileSize).toInt() + gapTiles
        } else {
            (player.position.x / tileSize).toInt() - gapTiles - structureWidthTiles
        }
        val maxLeft = WorldConstants.WORLD_WIDTH_TILES - 1 - structureWidthTiles
        return raw.coerceIn(1, maxLeft)
    }

    private fun setIfDestructible(world: World, x: Int, y: Int, tile: Tile) {
        if (world[x, y].isIndestructible) return
        world[x, y] = tile
    }

    private fun paintWorldBounds(world: World) {
        for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
            world[x, 0] = Tile.KOTLINIUM
            world[x, WorldConstants.WORLD_HEIGHT_TILES - 1] = Tile.KOTLINIUM
        }
        for (y in 0 until WorldConstants.WORLD_HEIGHT_TILES) {
            world[0, y] = Tile.KOTLINIUM
            world[WorldConstants.WORLD_WIDTH_TILES - 1, y] = Tile.KOTLINIUM
        }
    }
}
