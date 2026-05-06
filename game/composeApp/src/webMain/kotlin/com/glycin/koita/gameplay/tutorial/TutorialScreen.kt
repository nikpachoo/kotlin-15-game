package com.glycin.koita.gameplay.tutorial

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.composables.GameLoop
import com.glycin.koita.composables.UiRenderer
import com.glycin.koita.composables.WorldRenderer
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Input
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.Drone
import com.glycin.koita.gameplay.FogOfWar
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.pickups.PickupManager
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.gameplay.ultimates.UltimateManager
import com.glycin.koita.gameplay.upgrades.ShrineManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollectibleSystem
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.ParallaxBackground
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import kotlinx.coroutines.delay

@Composable
fun TutorialScreen(appState: GameState) {
    val gameState = remember {
        GameState().apply {
            musicVolume = appState.musicVolume
            sfxVolume = appState.sfxVolume
        }
    }

    val focusRequester = remember { FocusRequester() }
    val spawnPosition = TutorialConstants.SPAWN_POSITION
    val camera = remember { Camera(spawnPosition) }
    val input = remember { Input() }
    val keysPressed = input.keyMap
    val mouse = input.mouse
    var frameCount by remember { mutableLongStateOf(0) }

    val world = remember {
        World(
            WorldConstants.WORLD_WIDTH_TILES,
            WorldConstants.WORLD_HEIGHT_TILES,
        ).apply {
            TutorialWorldBuilder.build(this)
        }
    }

    val collisionDetector = remember { CollisionDetector(world) }
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
            position = spawnPosition,
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
            position = spawnPosition,
            drone = drone,
            gameState = gameState,
            collisionDetector = collisionDetector,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
            world = world,
            input = input,
        )
    }

    remember(pickupManager, player) {
        pickupManager.onPlayerMaxHealthIncrease = {
            player.maxHealth++
            player.health++
        }
    }

    val upgradeRepository = remember { UpgradeRepository.getStandardRepository(gameState) }
    val shrineManager = remember { ShrineManager(player, upgradeRepository, particleSystem, collisionDetector, world, gameState) }
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

    remember(enemyManager, ultimateManager) {
        enemyManager.onEnemyKilled = { ultimateManager.notifyEnemyKilled() }
    }

    val parallaxBackground = remember { ParallaxBackground(camera = camera) }
    val fogOfWar = remember { FogOfWar(camera, player, gameState) }

    val tutorialState = remember { TutorialState(totalSteps = TutorialDirector.STEP_COUNT) }
    val tutorialDirector = remember {
        TutorialDirector(
            context = StepContext(
                player = player,
                gameState = gameState,
                world = world,
                input = input,
                collisionDetector = collisionDetector,
                particleSystem = particleSystem,
                enemyManager = enemyManager,
                pickupManager = pickupManager,
                shrineManager = shrineManager,
                upgradeRepository = upgradeRepository,
            ),
            state = tutorialState,
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .focusable()
        .onSizeChanged { newSize ->
            camera.updateViewport(newSize.width.toFloat(), newSize.height.toFloat())
        }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    val rawPos = event.changes.first().position
                    val virtualPos = camera.actualToVirtual(rawPos.x, rawPos.y)
                    mouse.updatePosition(virtualPos, camera.screenToWorld(virtualPos.x, virtualPos.y))
                    mouse.updateButtons(
                        leftPressed = event.buttons.isPrimaryPressed && !input.uiCapturing,
                        rightPressed = event.buttons.isSecondaryPressed && !input.uiCapturing,
                    )
                }
            }
        }
        .onKeyEvent { event ->
            when (event.type) {
                KeyEventType.KeyDown -> {
                    keysPressed[event.key] = true
                    when (event.key) {
                        Key.Escape -> {
                            appState.currentScreen = Screen.MAIN_MENU
                        }
                        Key.One -> player.equip(0)
                        Key.Two -> player.equip(1)
                        Key.Three -> player.equip(2)
                        Key.R -> gameState.ultimateTriggered = true
                        Key.E -> player.heal()
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
                shrineManager.update(floatDeltaTime)
                turretManager.update(floatDeltaTime)
                ultimateManager.update(floatDeltaTime, player)
                pickupManager.update(floatDeltaTime, player)

                if (gameState.ultimateTriggered) {
                    ultimateManager.activateOrReactivate(player)
                    gameState.ultimateTriggered = false
                }

                if (mouse.isLeftPressed || gameState.autoFireActive) {
                    player.useWeapon()
                }

                mouse.reset()

                tutorialDirector.update()

                frameCount++

                if (player.health == 0) {
                    SoundManager.playOneShot(Sounds.GAME_OVER)
                    appState.currentScreen = Screen.MAIN_MENU
                }
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
            null,
        )

        UiRenderer(gameState, player, camera, enemyManager, input, upgradeRepository)

        TutorialPrompt(tutorialState, input)

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            while (true) {
                delay(1000)
                if (!gameState.isPaused) {
                    gameState.elapsedTimeSeconds++
                }
            }
        }

        LaunchedEffect(gameState.isPaused) {
            if (!gameState.isPaused) {
                focusRequester.requestFocus()
                keysPressed.clear()
            }
        }
    }
}
