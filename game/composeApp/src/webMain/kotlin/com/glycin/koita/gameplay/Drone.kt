package com.glycin.koita.gameplay

import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.modes.BuildMode
import com.glycin.koita.gameplay.modes.MiningMode
import com.glycin.koita.gameplay.modes.AttackMode
import com.glycin.koita.gameplay.modes.Mode
import com.glycin.koita.gameplay.turrets.TurretManager
import com.glycin.koita.physics.CollectibleSystem
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World

class Drone(
    val modes : MutableList<Mode>,
) {
    companion object {
        fun getStartingModes(
            position: Vec2,
            world: World,
            collisionDetector: CollisionDetector,
            particleSystem: ParticleSystem,
            collectibleSystem: CollectibleSystem,
            mouse: Mouse,
            gameState: GameState,
            fluidSimulator: FluidSimulator,
            enemyManager: EnemyManager,
            turretManager: TurretManager,
        ) : Drone {
            val standardWeapons = mutableListOf(
                MiningMode(
                    position = position,
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    mouse = mouse,
                    collectibleSystem = collectibleSystem,
                    gameState = gameState,
                ),
                AttackMode(
                    position = position,
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    mouse = mouse,
                    gameState = gameState,
                    fluidSimulator = fluidSimulator,
                    enemyManager = enemyManager,
                ),
                BuildMode(
                    position = position,
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    mouse = mouse,
                    gameState = gameState,
                    turretManager = turretManager,
                ),
            )

            return Drone(standardWeapons)
        }
    }

    fun getAttackMode(): AttackMode? = modes.filterIsInstance<AttackMode>().firstOrNull()

    fun getBuildMode(): BuildMode? = modes.filterIsInstance<BuildMode>().firstOrNull()

    fun getMiningMode(): MiningMode? = modes.filterIsInstance<MiningMode>().firstOrNull()
}