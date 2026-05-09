package com.glycin.koita.gameplay.ultimates

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.gameplay.upgrades.UpgradeRepository
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.World

class UltimateManager(
    private val gameState: GameState,
    private val upgradeRepository: UpgradeRepository,
    private val ultimates: List<UltimateAttack>,
) {
    var activeUltimate: UltimateAttack? = null
        private set
    var availableUltimate: UltimateAttack? = null
        private set
    private val announcedUltimateIds = mutableSetOf<UltimateId>()
    private var lastActivated: UltimateAttack? = null

    fun checkCombinations() {
        if (availableUltimate != null || activeUltimate != null) return
        if (gameState.ultimateCooldownRemaining > 0f) return

        val newCombo = ultimates.firstOrNull { ultimate ->
            ultimate.id !in announcedUltimateIds &&
                ultimate.requiredUnlockIds.all(upgradeRepository::isUnlocked)
        }
        val ready = newCombo ?: lastActivated ?: return

        markAvailable(ready)

        if (newCombo != null) {
            announcedUltimateIds.add(newCombo.id)
            gameState.ultimateBannerName = newCombo.name
            SoundManager.playOneShot(Sounds.ULTIMATE_UNLOCK)
        }
    }

    fun notifyEnemyKilled() {
        if (announcedUltimateIds.isEmpty()) return
        reduceCooldown(KILL_COOLDOWN_REDUCTION)
    }

    fun activateOrReactivate(player: Player) {
        val active = activeUltimate
        if (active != null) {
            active.onReactivate()
            return
        }

        val ultimate = availableUltimate ?: return
        activeUltimate = ultimate
        lastActivated = ultimate
        ultimate.activate(player)
        gameState.ultimateActive = true
        if (ultimate.usesBoostAnimation) player.enterBoostState()
        SoundManager.playOneShot(Sounds.ULTIMATE_USE)
    }

    fun update(deltaTime: Float, player: Player) {
        reduceCooldown(deltaTime)

        val active = activeUltimate ?: return
        active.update(deltaTime, player)

        if (active.isFinished()) {
            activeUltimate = null
            availableUltimate = null
            gameState.ultimateActive = false
            gameState.ultimateAvailable = null
            gameState.ultimateCooldownRemaining = ULTIMATE_RECHARGE_DURATION
        }
    }

    fun devUnlock(id: UltimateId) {
        val ultimate = ultimates.firstOrNull { it.id == id } ?: return
        announcedUltimateIds.add(ultimate.id)
        gameState.ultimateCooldownRemaining = 0f
        gameState.ultimateBannerName = ultimate.name
        markAvailable(ultimate)
    }

    private fun markAvailable(ultimate: UltimateAttack) {
        availableUltimate = ultimate
        gameState.ultimateAvailable = ultimate.name
    }

    private fun reduceCooldown(amount: Float) {
        if (gameState.ultimateCooldownRemaining <= 0f) return
        val next = (gameState.ultimateCooldownRemaining - amount).coerceAtLeast(0f)
        gameState.ultimateCooldownRemaining = next
        if (next <= 0f) {
            checkCombinations()
        }
    }

    companion object {
        const val ULTIMATE_RECHARGE_DURATION = 60f
        private const val KILL_COOLDOWN_REDUCTION = 1f

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
