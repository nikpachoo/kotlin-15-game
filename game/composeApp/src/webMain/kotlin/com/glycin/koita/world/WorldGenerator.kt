package com.glycin.koita.world

import com.glycin.koita.algorithms.SimplexNoise
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.Hydra
import com.glycin.koita.gameplay.enemies.Confuser
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.enemies.Wraith
import com.glycin.koita.gameplay.enemies.Slime
import com.glycin.koita.gameplay.enemies.Spider
import com.glycin.koita.gameplay.enemies.DashingPhantom
import com.glycin.koita.gameplay.enemies.StoneGolem
import com.glycin.koita.gameplay.ModifierConfiguration
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.upgrades.Shrine
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.gameplay.SpawnSettings
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

//Optimized with AI
class WorldGenerator(
    seed: Long = Random.nextLong()
) {
    private val noise = SimplexNoise(seed)
    private val oreNoise = SimplexNoise(seed + 1)
    private val caveNoise = SimplexNoise(seed + 2)
    private val biomeNoise = SimplexNoise(seed + 3)

    private val surfaceY = WorldConstants.SURFACE_Y
    private val minSpawnTileY = surfaceY * 3

    fun generateWorld(world: World, spawnPosition: Vec2) {
        for (chunkY in 0 until world.chunksHigh) {
            for (chunkX in 0 until world.chunksWide) {
                generateChunk(world, chunkX, chunkY)
            }
        }

        generateOreVeins(world)
        generateLavaPools(world)
        generateWaterPools(world)
        generateFlatSurface(world)
        generateOuterBounds(world)
        generateSurfaceVegetation(world)
        carveSpawnCave(world, spawnPosition)
    }


    fun spawnEnemies(
        world: World,
        enemyManager: EnemyManager,
        collisionDetector: CollisionDetector,
        particleSystem: ParticleSystem,
        minDistanceFromSpawn: Float = 500f,
        minDistanceBetweenEnemies: Float = 100f,
        spawnPosition: Vec2,
        player: Player,
        fluidSimulator: FluidSimulator,
    ) {
        val allLocations = mutableListOf<Vec2>()
        val airCheck: (Tile) -> Boolean = { !it.isSolid }
        val solidCheck: (Tile) -> Boolean = { it.isSolid && !it.isIndestructible }
        val countMultiplier = if (ModifierConfiguration.doubleEnemies) 2 else 1

        for (zone in SpawnSettings.ALL_ZONES) {
            fun findAir(count: Int, width: Float, height: Float) = findSpawnLocations(
                world, count, width, height, minDistanceFromSpawn, minDistanceBetweenEnemies,
                spawnPosition, allLocations, zone.minTileY, zone.maxTileY, airCheck,
            )

            fun findSolid(count: Int, width: Float, height: Float) = findSpawnLocations(
                world, count, width, height, minDistanceFromSpawn, minDistanceBetweenEnemies,
                spawnPosition, allLocations, zone.minTileY, zone.maxTileY, solidCheck,
            )

            fun spawn(locations: List<Vec2>, factory: (Vec2) -> Unit) {
                locations.forEach { factory(it.copy()) }
                allLocations.addAll(locations)
            }

            spawn(findAir(zone.wraith.random() * countMultiplier, 64f, 64f)) { pos ->
                enemyManager.add(Wraith(pos, collisionDetector, world, particleSystem))
            }

            spawn(findAir(zone.hydra.random() * countMultiplier, 32f, 32f)) { pos ->
                val target = findPatrolTarget(pos, collisionDetector, 500f, 24f, 24f) ?: pos
                enemyManager.add(Hydra(pos, collisionDetector, world, particleSystem, targetPosition = target))
            }

            spawn(findAir(zone.slime.random() * countMultiplier, 32f, 32f)) { pos ->
                enemyManager.add(Slime(pos, collisionDetector, world, particleSystem = particleSystem))
            }

            spawn(findSolid(zone.stoneGolem.random() * countMultiplier, 24f, 24f)) { pos ->
                enemyManager.add(StoneGolem(pos, collisionDetector, world, player = player))
            }

            spawn(findSolid(zone.spider.random() * countMultiplier, 24f, 24f)) { pos ->
                enemyManager.add(Spider(pos, collisionDetector, world))
            }

            spawn(findAir(zone.phantom.random() * countMultiplier, 32f, 32f)) { pos ->
                enemyManager.add(DashingPhantom(pos, collisionDetector, world, player, particleSystem))
            }

            spawn(findAir(zone.confuser.random() * countMultiplier, 48f, 48f)) { pos ->
                enemyManager.add(Confuser(pos, collisionDetector, world, player, fluidSimulator))
            }
        }
    }

    private inline fun forEachEllipseTile(
        cx: Int,
        cy: Int,
        rx: Int,
        ry: Int,
        margin: Int = 0,
        action: (tx: Int, ty: Int, dx: Int, dy: Int, inside: Boolean) -> Unit,
    ) {
        val rxF = rx.toFloat()
        val ryF = ry.toFloat()
        for (dy in -ry - margin..ry + margin) {
            val ny = dy / ryF
            val nySq = ny * ny
            for (dx in -rx - margin..rx + margin) {
                val tx = cx + dx
                val ty = cy + dy
                if (!isValidTile(tx, ty)) continue
                val nx = dx / rxF
                val inside = nx * nx + nySq <= 1f
                action(tx, ty, dx, dy, inside)
            }
        }
    }

    private fun depthPercent(tileY: Int): Float =
        1f - (tileY.toFloat() / WorldConstants.WORLD_HEIGHT_TILES)

    fun spawnShrines(
        world: World,
        shrineManager: ShrineManager,
        upgradeRepository: UpgradeRepository,
        spawnPosition: Vec2,
        minDistanceFromSpawn: Float = 500f,
        minDistanceBetweenShrines: Float = 800f,
    ) {
        if (ModifierConfiguration.noShrines) return

        val shrineWidth = 32f
        val shrineHeight = 48f
        val carvePadding = 4

        val shrineWidthTiles = (shrineWidth / WorldConstants.TILE_SIZE).toInt()
        val shrineHeightTiles = (shrineHeight / WorldConstants.TILE_SIZE).toInt()

        val minDistanceFromSpawnSq = minDistanceFromSpawn * minDistanceFromSpawn
        val minDistanceBetweenShrinesSq = minDistanceBetweenShrines * minDistanceBetweenShrines

        val placedPositions = mutableListOf<Vec2>()
        var shrineBudget = upgradeRepository.getAll().size

        for (zone in SpawnSettings.ALL_ZONES) {
            if (shrineBudget <= 0) break

            val count = zone.shrines.random()
            if (count <= 0) continue

            val minTileY = maxOf(zone.minTileY, minSpawnTileY)

            val searchStep = 16
            val searchPositions = mutableListOf<Pair<Int, Int>>()
            for (x in carvePadding until WorldConstants.WORLD_WIDTH_TILES - carvePadding step searchStep) {
                for (y in minTileY until zone.maxTileY - carvePadding step searchStep) {
                    searchPositions.add(x to y)
                }
            }

            val shuffledPositions = searchPositions.shuffled()
            var placed = 0

            for ((x, y) in shuffledPositions) {
                if (placed >= count || shrineBudget <= 0) break

                if (x + shrineWidthTiles + carvePadding >= WorldConstants.WORLD_WIDTH_TILES) continue
                if (y + shrineHeightTiles + carvePadding >= WorldConstants.WORLD_HEIGHT_TILES) continue

                val worldPos = Vec2(
                    x = x * WorldConstants.TILE_SIZE.toFloat(),
                    y = y * WorldConstants.TILE_SIZE.toFloat(),
                )

                if (Vec2.fastDistance(worldPos, spawnPosition) < minDistanceFromSpawnSq) continue
                if (placedPositions.any { Vec2.fastDistance(worldPos, it) < minDistanceBetweenShrinesSq }) continue

                val centerX = x + shrineWidthTiles / 2
                val centerY = y + shrineHeightTiles / 2
                val radiusX = shrineWidthTiles / 2 + carvePadding
                val radiusY = shrineHeightTiles / 2 + carvePadding
                val shrineBottomY = y + shrineHeightTiles

                forEachEllipseTile(centerX, centerY, radiusX, radiusY) { tx, ty, _, _, inside ->
                    if (!inside) return@forEachEllipseTile
                    if (world[tx, ty].isIndestructible) return@forEachEllipseTile
                    world[tx, ty] = if (ty < shrineBottomY) {
                        Tile.AIR
                    } else {
                        getTileForDepth(depthPercent(ty), tx, ty)
                    }
                }

                shrineManager.add(
                    Shrine(
                        position = worldPos,
                        width = shrineWidth,
                        height = shrineHeight,
                    )
                )
                placedPositions.add(worldPos)
                placed++
                shrineBudget--
            }
        }
    }

    fun spawnPickups(
        world: World,
        pickupManager: PickupManager,
        spawnPosition: Vec2,
        minDistanceFromSpawn: Float = 300f,
        minDistanceBetweenPickups: Float = 200f,
    ) {
        if (ModifierConfiguration.noPickups) return

        val pickupTiles = 32 / WorldConstants.TILE_SIZE
        val border = 1
        val minDistanceFromSpawnSq = minDistanceFromSpawn * minDistanceFromSpawn
        val minDistanceBetweenPickupsSq = minDistanceBetweenPickups * minDistanceBetweenPickups
        val placedPositions = mutableListOf<Vec2>()

        for (zone in SpawnSettings.ALL_ZONES) {
            val count = zone.pickups.random()
            if (count <= 0) continue

            val minTileY = maxOf(zone.minTileY, minSpawnTileY)

            val searchStep = 12
            val searchPositions = mutableListOf<Pair<Int, Int>>()
            for (x in border until WorldConstants.WORLD_WIDTH_TILES - pickupTiles - border step searchStep) {
                for (y in minTileY until zone.maxTileY - pickupTiles - border step searchStep) {
                    searchPositions.add(x to y)
                }
            }

            val shuffledPositions = searchPositions.shuffled()
            var placed = 0

            for ((x, y) in shuffledPositions) {
                if (placed >= count) break

                var allSolid = true
                for (dy in -border until pickupTiles + border) {
                    for (dx in -border until pickupTiles + border) {
                        val tx = x + dx
                        val ty = y + dy
                        if (!isValidTile(tx, ty) || !world[tx, ty].isSolid) {
                            allSolid = false
                            break
                        }
                    }
                    if (!allSolid) break
                }
                if (!allSolid) continue

                val worldPos = Vec2(
                    x = x * WorldConstants.TILE_SIZE.toFloat(),
                    y = y * WorldConstants.TILE_SIZE.toFloat(),
                )

                if (Vec2.fastDistance(worldPos, spawnPosition) < minDistanceFromSpawnSq) continue
                if (placedPositions.any { Vec2.fastDistance(worldPos, it) < minDistanceBetweenPickupsSq }) continue

                val centerX = x + pickupTiles / 2
                val centerY = y + pickupTiles / 2

                forEachEllipseTile(centerX, centerY, pickupTiles, pickupTiles) { tx, ty, _, _, inside ->
                    if (!inside) return@forEachEllipseTile
                    if (world[tx, ty].isIndestructible) return@forEachEllipseTile
                    world[tx, ty] = Tile.AIR
                }

                pickupManager.spawn(worldPos)
                placedPositions.add(worldPos)
                placed++
            }
        }
    }

    private fun generateChunk(world: World, chunkX: Int, chunkY: Int) {
        val chunk = world.getChunk(chunkX, chunkY) ?: return

        for (localY in 0 until WorldConstants.CHUNK_SIZE) {
            for (localX in 0 until WorldConstants.CHUNK_SIZE) {
                val worldY = chunkY * WorldConstants.CHUNK_SIZE + localY
                val worldX = chunkX * WorldConstants.CHUNK_SIZE + localX

                val depthPercent = 1.0f - (worldY.toFloat() / WorldConstants.WORLD_HEIGHT_TILES)

                val largeCaves = caveNoise.noise(worldX / 150f, worldY / 150f)
                val mediumCaves = caveNoise.noise(worldX / 60f, worldY / 60f) * 0.3f
                val smallDetails = caveNoise.noise(worldX / 20f, worldY / 20f) * 0.1f
                val combinedNoise = largeCaves + mediumCaves + smallDetails

                val threshold = when {
                    depthPercent > 0.80f -> 0.65f
                    depthPercent > 0.50f -> 0.70f
                    depthPercent > 0.20f -> 0.75f
                    else -> 0.80f
                }

                if (combinedNoise > threshold) {
                    chunk.setTileAt(localX, localY, Tile.AIR)
                    continue
                }

                val tile = getTileForDepth(depthPercent, worldX, worldY)
                chunk.setTileAt(localX, localY, tile)
            }
        }
    }

    private fun getTileForDepth(depthPercent: Float, worldX: Int, worldY: Int): Tile {
        return when {
            // Deep Underground (0-20%) - Desolate, hot
            depthPercent < 0.05f -> Tile.BEDROCK
            depthPercent < 0.20f -> {
                val biome = biomeNoise.noise(worldX / 50f, worldY / 50f)
                when {
                    biome > 0.6f -> Tile.OBSIDIAN
                    biome < 0.15f -> Tile.CLAY
                    else -> Tile.DEEP_STONE
                }
            }

            // Mid Underground (20-50%) - Dark caves
            depthPercent < 0.50f -> {
                val biome = biomeNoise.noise(worldX / 40f, worldY / 40f)
                if (biome < 0.15f) Tile.CLAY else Tile.STONE
            }

            // Upper Underground (50-70%) - Transition zone
            depthPercent < 0.70f -> {
                val biome = biomeNoise.noise(worldX / 30f, worldY / 30f)
                when {
                    biome > 0.6f -> Tile.STONE
                    biome > 0.15f -> Tile.DIRT
                    else -> Tile.CLAY
                }
            }

            // Near Surface (70-90%) - Rich soil
            depthPercent < 0.90f -> {
                val biome = biomeNoise.noise(worldX / 25f, worldY / 25f)
                when {
                    biome > 0.6f -> Tile.DIRT
                    biome > 0.3f -> Tile.RICH_DIRT
                    else -> Tile.SAND
                }
            }

            // Surface (90-100%) - Lush and green
            else -> Tile.GRASS
        }
    }

    private fun generateOreVeins(world: World) {
        for (y in 0 until WorldConstants.WORLD_HEIGHT_TILES) {
            for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                if (world[x, y].isSolid) {
                    val depthPercent = 1.0f - (y.toFloat() / WorldConstants.WORLD_HEIGHT_TILES)  // FIXED
                    val oreValue = oreNoise.noise(x / 20f, y / 20f)

                    val ore = when {
                        depthPercent < 0.30f && oreValue > 0.75f -> Tile.COAL_ORE  // Deep
                        depthPercent < 0.50f && oreValue > 0.80f -> Tile.IRON_ORE  // Mid-deep
                        depthPercent < 0.70f && oreValue > 0.85f -> Tile.GOLD_ORE  // Mid
                        else -> null
                    }

                    ore?.let { world[x, y] = it }
                }
            }
        }
    }

    private fun generateWaterPools(world: World) {
        val minDepth = (WorldConstants.WORLD_HEIGHT_TILES * 0.4f).toInt()

        for (y in 0 until minDepth) {
            for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                val waterNoise = noise.noise(x / 25f, y / 25f)

                if (world[x, y].isSolid && waterNoise > 0.75f) {
                    world[x, y] = Tile.WATER
                }

                if (world[x, y] == Tile.AIR && y < WorldConstants.WORLD_HEIGHT_TILES - 1) {
                    val below = world[x, y + 1]

                    if (below.isSolid && waterNoise > 0.80f) {
                        world[x, y] = Tile.WATER

                        if (x > 0 && world[x - 1, y] == Tile.AIR &&
                            world[x - 1, y + 1].isSolid && waterNoise > 0.78f) {
                            world[x - 1, y] = Tile.WATER
                        }
                        if (x < WorldConstants.WORLD_WIDTH_TILES - 1 &&
                            world[x + 1, y] == Tile.AIR &&
                            world[x + 1, y + 1].isSolid && waterNoise > 0.78f) {
                            world[x + 1, y] = Tile.WATER
                        }
                    }
                }
            }
        }
    }

    private fun generateLavaPools(world: World) {
        val deepMin = (WorldConstants.WORLD_HEIGHT_TILES * 0.75f).toInt()
        fillLavaInRange(world, deepMin, WorldConstants.WORLD_HEIGHT_TILES, solidThreshold = 0.85f, pourThreshold = 0.86f)

        val nearSurfaceMin = (WorldConstants.WORLD_HEIGHT_TILES * 0.10f).toInt()
        val nearSurfaceMax = (WorldConstants.WORLD_HEIGHT_TILES * 0.30f).toInt()
        fillLavaInRange(world, nearSurfaceMin, nearSurfaceMax, solidThreshold = 0.89f, pourThreshold = 0.90f)
    }

    private fun fillLavaInRange(
        world: World,
        minY: Int,
        maxY: Int,
        solidThreshold: Float,
        pourThreshold: Float,
    ) {
        for (y in minY until maxY) {
            for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
                val lavaNoise = biomeNoise.noise(x / 30f, y / 30f)

                if (world[x, y].isSolid && lavaNoise > solidThreshold) {
                    world[x, y] = Tile.LAVA
                }

                if (world[x, y] == Tile.AIR && y < WorldConstants.WORLD_HEIGHT_TILES - 1) {
                    val below = world[x, y + 1]

                    if (below.isSolid && lavaNoise > pourThreshold) {
                        world[x, y] = Tile.LAVA

                        if (x > 0 && world[x - 1, y] == Tile.AIR &&
                            world[x - 1, y + 1].isSolid && lavaNoise > pourThreshold) {
                            world[x - 1, y] = Tile.LAVA
                        }
                        if (x < WorldConstants.WORLD_WIDTH_TILES - 1 &&
                            world[x + 1, y] == Tile.AIR &&
                            world[x + 1, y + 1].isSolid && lavaNoise > pourThreshold) {
                            world[x + 1, y] = Tile.LAVA
                        }
                    }
                }
            }
        }
    }

    private fun generateSurfaceVegetation(world: World) {
        val surfaceThreshold = (WorldConstants.WORLD_HEIGHT_TILES * 0.15f).toInt()

        for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
            for (y in 0 until surfaceThreshold) {
                if (world[x, y] == Tile.GRASS && y < WorldConstants.WORLD_HEIGHT_TILES - 1 && world[x, y + 1] == Tile.AIR) {
                    val vegNoise = noise.noise(x / 10f, y / 10f)

                    if (vegNoise > 0.7f) {
                        world[x, y + 1] = Tile.FLOWER
                    }
                    else if (vegNoise < 0.2f && y <= WorldConstants.WORLD_HEIGHT_TILES - 4) {
                        for (treeY in 1..3) {
                            if (world[x, y + treeY] == Tile.AIR) {
                                world[x, y + treeY] = Tile.WOOD
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateFlatSurface(world: World) {
        for (x in 0 until WorldConstants.WORLD_WIDTH_TILES) {
            for(y in surfaceY until surfaceY + 5) {
                world[x, y] = Tile.GRASS
            }

            world[x, surfaceY] = Tile.KOTLINIUM

            for (y in 0 until surfaceY) {
                world[x, y] = Tile.AIR
            }
        }

        // Place portal just below surfaceY, centered horizontally
        val portalCenterX = WorldConstants.WORLD_WIDTH_TILES / 2
        val portalWidth = WorldConstants.PORTAL_WIDTH
        val portalHeight = WorldConstants.PORTAL_HEIGHT
        val portalY = surfaceY + WorldConstants.PORTAL_OFFSET_Y
        val carvePadding = 4

        // Carve space around the portal
        for (dy in -carvePadding until portalHeight + carvePadding) {
            for (dx in -portalWidth / 2 - carvePadding until portalWidth / 2 + carvePadding) {
                val tx = portalCenterX + dx
                val ty = portalY + dy
                if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                    ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
                ) {
                    val tile = world[tx, ty]
                    if (!tile.isIndestructible) {
                        world[tx, ty] = Tile.AIR
                    }
                }
            }
        }

        // Place portal tiles
        for (dy in 0 until portalHeight) {
            for (dx in -portalWidth / 2 until portalWidth / 2) {
                val tx = portalCenterX + dx
                val ty = portalY + dy
                if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                    ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
                ) {
                    world[tx, ty] = Tile.PORTAL
                }
            }
        }

        // Kotlinium floor under the portal
        for (dx in -portalWidth / 2 - 2 until portalWidth / 2 + 2) {
            val tx = portalCenterX + dx
            val ty = portalY + portalHeight
            if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
                ty in 0 until WorldConstants.WORLD_HEIGHT_TILES
            ) {
                world[tx, ty] = Tile.KOTLINIUM
            }
        }
    }

    private fun carveSpawnCave(world: World, spawnPosition: Vec2) {
        val centerTileX = (spawnPosition.x / WorldConstants.TILE_SIZE).toInt()
        val centerTileY = (spawnPosition.y / WorldConstants.TILE_SIZE).toInt()

        val caveRadius = 60
        val caveHeight = 70
        val halfHeight = caveHeight / 2
        val shellThickness = 2
        val innerRadius = (caveRadius - shellThickness).toFloat()
        val innerHalfHeight = (halfHeight - shellThickness).toFloat()
        val fluidMargin = 3

        forEachEllipseTile(centerTileX, centerTileY, caveRadius, halfHeight, fluidMargin) { tx, ty, dx, dy, inOuter ->
            val tile = world[tx, ty]
            if (tile.isIndestructible) return@forEachEllipseTile
            if (!inOuter) {
                if (tile == Tile.WATER || tile == Tile.LAVA) world[tx, ty] = Tile.STONE
                return@forEachEllipseTile
            }
            val nxInner = dx / innerRadius
            val nyInner = dy / innerHalfHeight
            val inInner = nxInner * nxInner + nyInner * nyInner <= 1f
            world[tx, ty] = if (inInner) Tile.AIR else getTileForDepth(depthPercent(ty), tx, ty)
        }
    }

    private fun findSpawnLocations(
        world: World,
        count: Int,
        enemyWidth: Float,
        enemyHeight: Float,
        minDistanceFromSpawn: Float,
        minDistanceBetweenEnemies: Float,
        spawnPosition: Vec2,
        existingLocations: List<Vec2>,
        minTileY: Int = minSpawnTileY,
        maxTileY: Int = WorldConstants.WORLD_HEIGHT_TILES,
        tileCheck: (Tile) -> Boolean,
    ): List<Vec2> {
        val newLocations = mutableListOf<Vec2>()

        val widthInTiles = (enemyWidth / WorldConstants.TILE_SIZE).toInt() + 1
        val heightInTiles = (enemyHeight / WorldConstants.TILE_SIZE).toInt() + 1

        val minDistanceFromSpawnSq = minDistanceFromSpawn * minDistanceFromSpawn
        val minDistanceBetweenEnemiesSq = minDistanceBetweenEnemies * minDistanceBetweenEnemies

        val clampedMinY = maxOf(minTileY, minSpawnTileY)
        val searchStep = 8
        val searchPositions = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until WorldConstants.WORLD_WIDTH_TILES step searchStep) {
            for (y in clampedMinY until maxTileY step searchStep) {
                searchPositions.add(x to y)
            }
        }

        val shuffledPositions = searchPositions.shuffled()

        for ((x, y) in shuffledPositions) {
            if (newLocations.size >= count) return newLocations

            var valid = true
            for (dx in 0 until widthInTiles) {
                for (dy in 0 until heightInTiles) {
                    val checkX = x + dx
                    val checkY = y + dy

                    if (checkX >= WorldConstants.WORLD_WIDTH_TILES ||
                        checkY >= WorldConstants.WORLD_HEIGHT_TILES) {
                        valid = false
                        break
                    }

                    if (!tileCheck(world[checkX, checkY])) {
                        valid = false
                        break
                    }
                }
                if (!valid) break
            }

            if (!valid) continue

            val worldPos = Vec2(
                x = x * WorldConstants.TILE_SIZE.toFloat(),
                y = y * WorldConstants.TILE_SIZE.toFloat()
            )

            if (Vec2.fastDistance(worldPos, spawnPosition) < minDistanceFromSpawnSq) continue

            var tooClose = false
            for (existingLocation in existingLocations) {
                if (Vec2.fastDistance(worldPos, existingLocation) < minDistanceBetweenEnemiesSq) {
                    tooClose = true
                    break
                }
            }
            if (tooClose) continue

            for (newLocation in newLocations) {
                if (Vec2.fastDistance(worldPos, newLocation) < minDistanceBetweenEnemiesSq) {
                    tooClose = true
                    break
                }
            }
            if (tooClose) continue

            newLocations.add(worldPos)
        }

        return newLocations
    }

    private fun findPatrolTarget(
        startPos: Vec2,
        collisionDetector: CollisionDetector,
        radius: Float,
        enemyWidth: Float,
        enemyHeight: Float
    ): Vec2? {
        // Try to find a valid target position (max 20 attempts)
        repeat(20) {
            // Random angle and distance
            val angle = Random.nextDouble() * 2 * PI
            val distance = Random.nextDouble(radius / 2.0, radius * 2.0)

            val targetPos = Vec2(
                x = startPos.x + (cos(angle) * distance).toFloat(),
                y = startPos.y + (sin(angle) * distance).toFloat()
            )

            // Check if target position has space for the enemy
            if (!collisionDetector.checkAABB(targetPos, enemyWidth, enemyHeight)) {
                return targetPos
            }
        }

        return null // No valid position found
    }

    private fun generateOuterBounds(world: World) {
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