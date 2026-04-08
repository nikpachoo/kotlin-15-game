package com.glycin.koita.gameplay.modes

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.weapon.Weapon
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.weapon.Laser
import com.glycin.koita.gameplay.weapon.MagicMissile
import com.glycin.koita.gameplay.weapon.Rocket
import com.glycin.koita.gameplay.weapon.Sniper
import com.glycin.koita.gameplay.weapon.SuperSoaker
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.angleTo
import com.glycin.koita.world.World

class AttackMode(
    position: Vec2,
    width: Float = 32f,
    height: Float = 64f,
    world: World,
    collisionDetector: CollisionDetector,
    particleSystem: ParticleSystem,
    mouse: Mouse,
    private val gameState: GameState,
    private val fluidSimulator: FluidSimulator,
    private val enemyManager: EnemyManager,
): Mode(position, width, height, world, collisionDetector, particleSystem, mouse) {

    override val coolDownMs: Long = 1000L

    var dronePosition: Vec2 = position

    private val missiles = mutableListOf<MagicMissile>()
    private val rockets = mutableListOf<Rocket>()
    private var laser: Laser? = null
    private var superSoaker: SuperSoaker? = null
    private var sniper: Sniper? = null

    fun getActiveWeapon(): List<Weapon> = when {
        gameState.sniperWeapon && sniper?.isAlive == true -> listOf(sniper!!)
        gameState.superSoaker && superSoaker?.isActive == true -> listOf(superSoaker!!)
        gameState.laserWeapon && laser?.isActive == true -> listOf(laser!!)
        gameState.rocketLauncher -> rockets
        else -> missiles
    }

    override fun use() {
        when {
            gameState.sniperWeapon -> useSniper()
            gameState.superSoaker -> useSuperSoaker()
            gameState.laserWeapon -> useLaser()
            gameState.rocketLauncher -> useRocket()
            else -> useMissile()
        }
    }

    override fun update(deltaTime: Float) {
        rotation = position.angleTo(mouse.worldPosition) + 90f

        missiles.forEach { it.update(deltaTime) }
        missiles.removeAll { !it.isAlive }

        rockets.forEach { it.update(deltaTime) }
        rockets.removeAll { !it.isAlive }

        laser?.let {
            if (it.isActive) it.update(deltaTime, dronePosition, mouse.worldPosition)
            it.isActive = false
        }

        superSoaker?.let {
            it.update(deltaTime, dronePosition, mouse.worldPosition)
            it.isActive = false
        }

        sniper?.let {
            it.update(deltaTime, dronePosition, mouse.worldPosition)
            it.isActive = false
        }
    }

    private fun useSuperSoaker() {
        val soaker = superSoaker ?: SuperSoaker(
            gameState = gameState,
            collisionDetector = collisionDetector,
            world = world,
            fluidSimulator = fluidSimulator,
            enemyManager = enemyManager,
        ).also { superSoaker = it }
        soaker.isActive = true
    }

    private fun useLaser() {
        val l = laser ?: Laser(
            gameState = gameState,
            collisionDetector = collisionDetector,
            world = world,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
        ).also { laser = it }
        l.isActive = true
    }

    private fun useSniper() {
        val s = sniper ?: Sniper(
            gameState = gameState,
            world = world,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
        ).also { sniper = it }
        s.isActive = true
    }

    private fun useRocket() {
        if (!canUse()) return

        val direction = (mouse.worldPosition - position).normalized()
        SoundManager.playOneShot(Sounds.SHOOT)

        rockets.add(Rocket(
            position = getActivationPoint(0.7f),
            direction = direction,
            gameState = gameState,
            collisionDetector = collisionDetector,
            world = world,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
            onEnd = {
                rockets.removeAll { !it.isAlive }
            },
        ))

        used()
    }

    private fun useMissile() {
        if(!canUse()) return

        val direction = (mouse.worldPosition - position).normalized()
        SoundManager.playOneShot(Sounds.SHOOT)

        missiles.add(MagicMissile(
            position = getActivationPoint(0.7f),
            direction = direction,
            gameState = gameState,
            collisionDetector = collisionDetector,
            world = world,
            particleSystem = particleSystem,
            enemyManager = enemyManager,
            onEnd = {
                missiles.removeAll { !it.isAlive }
            }
        ))

        used()
    }
}