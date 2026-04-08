package com.glycin.koita.composables

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.glycin.koita.BuildConfig
import com.glycin.koita.audio.Music
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.FogOfWar
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Drone
import com.glycin.koita.gameplay.Portal
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.gameplay.ultimates.UltimateManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollectibleSystem
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.ParallaxBackground
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.world.WorldGenerator
import kotlinx.coroutines.delay

@Composable
fun GameScreen(gameState: GameState) {
    val focusRequester = remember { FocusRequester() }
    val startWorldPosX = (WorldConstants.WORLD_WIDTH_TILES * WorldConstants.TILE_SIZE) / 2f
    val startWorldPosY = (WorldConstants.WORLD_HEIGHT_TILES * WorldConstants.TILE_SIZE) - (WorldConstants.CHUNK_PIXEL_HEIGHT * 4f)
    val camera = remember { Camera(Vec2(startWorldPosX, startWorldPosY)) }
    val keysPressed = remember { mutableStateMapOf<Key, Boolean>() }
    val mouse = remember { Mouse() }
    var frameCount by remember { mutableLongStateOf(0) }

    val wg = remember { WorldGenerator() }
    val world = remember {
        World(
            WorldConstants.WORLD_WIDTH_TILES,
            WorldConstants.WORLD_HEIGHT_TILES
        ).apply {
            wg.generateWorld(this, Vec2(startWorldPosX, startWorldPosY))
        }
    }

    val collisionDetector = remember {
        CollisionDetector(world).apply {
            val tileSize = WorldConstants.TILE_SIZE.toFloat()
            val surfaceY = WorldConstants.SURFACE_Y
            val portalTileY = surfaceY + WorldConstants.PORTAL_OFFSET_Y

            portalX = (WorldConstants.WORLD_WIDTH_TILES / 2 - WorldConstants.PORTAL_WIDTH / 2) * tileSize
            portalY = portalTileY * tileSize
            portalWidth = WorldConstants.PORTAL_WIDTH * tileSize
            portalHeight = WorldConstants.PORTAL_HEIGHT * tileSize
            portalDestY = (surfaceY - 10) * tileSize
        }
    }
    val fluidSimulator = remember { FluidSimulator(world, camera) }
    val particleSystem = remember { ParticleSystem(collisionDetector, world, fluidSimulator) }
    val collectibleSystem = remember { CollectibleSystem(gameState) }

    val pickupManager = remember { PickupManager(gameState, collisionDetector) }
    val enemyManager = remember {
        EnemyManager(collisionDetector, world, particleSystem, pickupManager)
    }
    val turretManager = remember { TurretManager(enemyManager, collisionDetector, gameState) }

    val drone = remember {
        Drone.getStartingModes(
            position = Vec2(startWorldPosX, startWorldPosY),
            world = world,
            collisionDetector = collisionDetector,
            particleSystem = particleSystem,
            collectibleSystem = collectibleSystem,
            gameState = gameState,
            mouse = mouse,
            fluidSimulator = fluidSimulator,
            enemyManager = enemyManager,
            turretManager = turretManager,
        )
    }

    val player = remember {
        Player(
            position = Vec2(startWorldPosX, startWorldPosY),
            drone = drone,
            gameState = gameState,
            collisionDetector = collisionDetector,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
            world = world,
            keyMap = keysPressed,
            mouse = mouse,
        )
    }

    remember(pickupManager, player) {
        pickupManager.onPlayerMaxHealthIncrease = { player.maxHealth++ }
    }

    val upgradeRepository = remember { UpgradeRepository.getStandardRepository(gameState) }
    val shrineManager = remember { ShrineManager(player, upgradeRepository, particleSystem, collisionDetector, world) }
    val ultimateManager = remember {
        UltimateManager.createStandard(
            gameState = gameState,
            upgradeRepository = upgradeRepository,
            world = world,
            collisionDetector = collisionDetector,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
            mouse = mouse,
            camera = camera,
        )
    }

    remember(upgradeRepository, ultimateManager) {
        upgradeRepository.onUpgradeCallback = { ultimateManager.checkCombinations() }
    }

    val spawnPosition = Vec2(startWorldPosX, startWorldPosY)

    remember {
        wg.spawnEnemies(
            world = world,
            enemyManager = enemyManager,
            collisionDetector = collisionDetector,
            particleSystem = particleSystem,
            spawnPosition = spawnPosition,
            player = player,
            fluidSimulator = fluidSimulator,
        )

        wg.spawnShrines(
            world = world,
            shrineManager = shrineManager,
            upgradeRepository = upgradeRepository,
            spawnPosition = spawnPosition,
        )

        wg.spawnPickups(
            world = world,
            pickupManager = pickupManager,
            spawnPosition = spawnPosition,
        )

        shrineManager.spawnFirstOrbs(spawnPosition)
    }

    val parallaxBackground = remember { ParallaxBackground( camera = camera) }
    val fogOfWar = remember { FogOfWar(camera, player, gameState) }

    val portal = remember {
        Portal(gameState, world, camera, player, fluidSimulator, collisionDetector, particleSystem, enemyManager)
    }
    var musicStarted by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .focusable()
        .onSizeChanged { newSize ->
            camera.canvasWidth = newSize.width.toFloat()
            camera.canvasHeight = newSize.height.toFloat()
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val mousePos = event.changes.first().position
                    mouse.updatePosition(mousePos, camera.screenToWorld(mousePos.x, mousePos.y))
                    mouse.updateButtons(
                        leftPressed = event.buttons.isPrimaryPressed,
                        rightPressed = event.buttons.isSecondaryPressed
                    )
                }
            }
        }
        .onKeyEvent{ event ->
            when(event.type) {
                KeyEventType.KeyDown -> {
                    keysPressed[event.key] = true
                    if (!musicStarted) {
                        musicStarted = true
                        SoundManager.playLoop(Music.BACKGROUND)
                    }
                    when(event.key) {
                        Key.Escape -> {
                            gameState.isPaused = !gameState.isPaused
                        }
                        Key.One -> {
                            player.equip(0)
                            gameState.selectedHotkeyIndex = 0
                        }
                        Key.Two -> {
                            player.equip(1)
                            gameState.selectedHotkeyIndex = 1
                        }
                        Key.Three ->  {
                            player.equip(2)
                            gameState.selectedHotkeyIndex = 2
                        }
                        Key.R -> {
                            ultimateManager.activateOrReactivate(player)
                        }
                        Key.B -> {
                            if (BuildConfig.isDev) {
                                player.position = Vec2(player.position.x, collisionDetector.portalY + collisionDetector.portalHeight + 16f)
                            }
                        }
                    }
                }
                KeyEventType.KeyUp -> keysPressed[event.key] = false
            }
            true
        }
    ) {
        GameLoop { deltaTime ->
            if (!gameState.isPaused) {
                val floatDeltaTime = deltaTime.toFloat()
                player.update(floatDeltaTime)
                camera.followPlayer(player, floatDeltaTime)
                particleSystem.update(floatDeltaTime)
                collectibleSystem.update(floatDeltaTime, player.center)
                fluidSimulator.update()
                enemyManager.update(floatDeltaTime, player)
                portal.update(floatDeltaTime)
                shrineManager.update(floatDeltaTime)
                turretManager.update(floatDeltaTime)
                ultimateManager.update(floatDeltaTime, player)
                pickupManager.update(floatDeltaTime, player)

                if (mouse.isLeftPressed) {
                    player.useWeapon()
                }

                mouse.reset()

                frameCount++
            }
        }

        WorldRenderer(
            gameState,
            world,
            camera,
            player,
            particleSystem,
            collectibleSystem,
            enemyManager,
            shrineManager,
            pickupManager,
            turretManager,
            mouse,
            parallaxBackground,
            fogOfWar,
            ultimateManager,
            frameCount,
            portal.boss,
        )

        UiRenderer(gameState, player, camera, enemyManager, mouse)

        if (BuildConfig.isDev) {
            FpsCounter()
            DevUltimateButtons(ultimateManager, focusRequester)
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            while (true) {
                delay(1000)
                if (!gameState.isPaused) {
                    gameState.elapsedTimeSeconds++
                }
            }
        }

        // TODO: Maybe add a launchedEffect that cleans up collectible and particle system arrays?
    }
}
