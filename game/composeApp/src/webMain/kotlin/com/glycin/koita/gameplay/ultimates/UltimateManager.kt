package com.glycin.koita.gameplay.ultimates

import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World

//TODO: Ultimate attacks should be enums instead of strings
class UltimateManager(
    private val gameState: GameState,
    private val upgradeRepository: UpgradeRepository,
    private val ultimates: List<UltimateAttack>,
) {
    var activeUltimate: UltimateAttack? = null
        private set
    var availableUltimate: UltimateAttack? = null
        private set

    fun checkCombinations() {
        if (availableUltimate != null || activeUltimate != null) return

        for (ultimate in ultimates) {
            if (ultimate.requiredUnlockIds.all { upgradeRepository.isUnlocked(it) }) {
                availableUltimate = ultimate
                gameState.ultimateAvailable = ultimate.name
                return
            }
        }
    }

    fun activateOrReactivate(player: Player) {
        val active = activeUltimate
        if (active != null) {
            active.onReactivate()
            return
        }

        val ultimate = availableUltimate ?: return
        activeUltimate = ultimate
        ultimate.activate(player)
        gameState.ultimateActive = true
    }

    fun update(deltaTime: Float, player: Player) {
        val active = activeUltimate ?: return
        active.update(deltaTime, player)

        if (active.isFinished()) {
            activeUltimate = null
            availableUltimate = null
            gameState.ultimateActive = false
            gameState.ultimateAvailable = null
        }
    }

    fun devUnlock(id: String) {
        val ultimate = ultimates.firstOrNull { it.id == id } ?: return
        availableUltimate = ultimate
        gameState.ultimateAvailable = ultimate.name
    }

    companion object {
        fun createStandard(
            gameState: GameState,
            upgradeRepository: UpgradeRepository,
            world: World,
            collisionDetector: CollisionDetector,
            particleSystem: ParticleSystem,
            enemyManager: EnemyManager,
            mouse: Mouse,
            camera: Camera,
        ): UltimateManager = UltimateManager(
            gameState = gameState,
            upgradeRepository = upgradeRepository,
            ultimates = listOf(
                SuperSaiyanDash(
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    enemyManager = enemyManager,
                ),
                SwarmBarrage(
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    enemyManager = enemyManager,
                ),
                Kamehameha(
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    enemyManager = enemyManager,
                    mouse = mouse,
                    camera = camera,
                ),
                GiantForm(
                    gameState = gameState,
                    enemyManager = enemyManager,
                ),
                RocketRide(
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                    enemyManager = enemyManager,
                    mouse = mouse,
                ),
            ),
        )
    }
}
