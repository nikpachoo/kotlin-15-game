package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.weapon.Laser
import com.glycin.koita.gameplay.weapon.MagicMissile
import com.glycin.koita.gameplay.weapon.Rocket
import com.glycin.koita.gameplay.weapon.Sniper
import com.glycin.koita.gameplay.weapon.SuperSoaker
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.FluidSimulator
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants
import com.glycin.koita.util.TWO_PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

fun interface BossCommand {
    fun update(deltaTime: Float): Boolean
}

class Boss(
    val position: Vec2,
    private val world: World,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val gameState: GameState,
    private val maxHealth: Float = 100f,
) {
    val width = SIZE
    val height = SIZE
    val drawWidth = SIZE
    val drawHeight = SIZE

    var health = maxHealth
        private set
    var isAlive = true
        private set
    var renderOffset: Vec2 = Vec2.zero
        private set

    val center get() = Vec2(position.x + width / 2f, position.y + height / 2f)

    private var bodyBob = 0f

    private val maxShieldTiles = 128
    private val shieldPositions = FloatArray(maxShieldTiles * 2)
    private val shieldActive = BooleanArray(maxShieldTiles)
    private var shieldCount = 0
    private val shieldSpawnInterval = 0.1f
    private val orbitRadius = 60f
    private val orbitSpeed = 3f
    private var orbitAngle = 0f

    private val lavaSpawnInterval = 0.01f
    private val lavaStreamDuration = 3f

    private val hands = mutableListOf<BossHand>()
    private var activeLaser: BossLaser? = null
    private var activeImplosion: BossImplosion? = null

    private val commandQueue = ArrayDeque<BossCommand>()
    private var currentCommand: BossCommand? = null
    private var idleTimer = 0f
    private var nextIdleInterval = Random.nextFloat() * 3f + 3f
    private var lastAttack = -1

    init {
        commandQueue.addLast(shieldCommand())
        commandQueue.addLast(lavaStreamCommand())
    }

    fun update(deltaTime: Float) {
        if (!isAlive) return

        bodyBob += deltaTime * 2f
        renderOffset = Vec2(0f, sin(bodyBob) * 4f)

        val hp = health / maxHealth
        if (hp != gameState.bossHealthPercent) {
            gameState.bossHealthPercent = hp
        }

        updateShieldOrbit(deltaTime)

        if (currentCommand == null) {
            if (commandQueue.isNotEmpty()) {
                currentCommand = commandQueue.removeFirst()
            } else {
                idleTimer += deltaTime
                if (idleTimer >= nextIdleInterval) {
                    idleTimer = 0f
                    currentCommand = pickRandomAttack()
                }
            }
        }

        val cmd = currentCommand
        if (cmd != null) {
            val done = cmd.update(deltaTime)
            if (done) {
                currentCommand = null
                nextIdleInterval = Random.nextFloat() * 3f + 3f
            }
        }

        val laser = activeLaser
        if (laser != null) {
            laser.update(deltaTime)
            if (!laser.alive) activeLaser = null
        }

        val implosion = activeImplosion
        if (implosion != null) {
            implosion.update(deltaTime)
            if (!implosion.alive) activeImplosion = null
        }

        hands.forEach { it.update(deltaTime, player, world) }
        hands.removeAll { !it.alive }

        checkWeaponCollisions()
    }

    private fun updateShieldOrbit(deltaTime: Float) {
        orbitAngle += orbitSpeed * deltaTime
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            val i2 = i * 2
            val angleOffset = (i.toFloat() / maxShieldTiles) * TWO_PI
            val radius = orbitRadius + (i % 4) * 8f
            val angle = orbitAngle + angleOffset
            val targetX = center.x + cos(angle) * radius
            val targetY = center.y + sin(angle) * radius
            shieldPositions[i2] += (targetX - shieldPositions[i2]) * 8f * deltaTime
            shieldPositions[i2 + 1] += (targetY - shieldPositions[i2 + 1]) * 8f * deltaTime
        }
    }

    private fun pickRandomAttack(): BossCommand {
        val options = (0 until 4).filter { it != lastAttack }
        val pick = options[Random.nextInt(options.size)]
        lastAttack = pick
        return when (pick) {
            0 -> handAttackCommand()
            1 -> laserAttackCommand()
            2 -> implosionCommand()
            else -> lavaStreamCommand()
        }
    }

    private fun shieldCommand(): BossCommand {
        var spawnTimer = 0f
        return BossCommand { deltaTime ->
            spawnTimer += deltaTime
            if (spawnTimer >= shieldSpawnInterval && shieldCount < maxShieldTiles) {
                spawnTimer -= shieldSpawnInterval
                val index = findFreeSlot()
                if (index != null) {
                    val i2 = index * 2
                    shieldPositions[i2] = center.x
                    shieldPositions[i2 + 1] = center.y
                    shieldActive[index] = true
                    shieldCount++
                }
            }
            shieldCount >= maxShieldTiles
        }
    }

    private fun lavaStreamCommand(): BossCommand {
        var streamTimer = 0f
        var spawnTimer = 0f
        return BossCommand { deltaTime ->
            streamTimer += deltaTime
            spawnTimer += deltaTime
            while (spawnTimer >= lavaSpawnInterval) {
                spawnTimer -= lavaSpawnInterval
                spawnLavaTile()
            }
            streamTimer >= lavaStreamDuration
        }
    }

    private fun handAttackCommand(): BossCommand {
        spawnHandAttack()
        return BossCommand { true }
    }

    private fun implosionCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activeImplosion = BossImplosion(
                    origin = { center },
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                )
            }
            activeImplosion?.alive != true
        }
    }

    private fun laserAttackCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activeLaser = BossLaser(
                    origin = { center },
                    player = player,
                    world = world,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                )
            }
            activeLaser?.alive != true
        }
    }

    private fun checkWeaponCollisions() {
        player.weapons.forEach { weapon ->
            when (weapon) {
                is MagicMissile -> {
                    if (weapon.isAlive && overlaps(weapon.position, weapon.baseSize, weapon.baseSize)) {
                        takeDamage(2f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Laser -> {
                    if (weapon.isActive && overlaps(weapon.end, 8f, 8f)) {
                        takeDamage(0.25f * gameState.damageMultiplier)
                    }
                }
                is SuperSoaker -> {
                    weapon.droplets.forEach { droplet ->
                        if (droplet.alive && overlaps(droplet.position, 4f, 4f)) {
                            takeDamage(0.15f * gameState.damageMultiplier)
                        }
                    }
                }

                is Rocket -> {
                    if (weapon.isAlive && overlaps(weapon.position, Rocket.BASE_SIZE, Rocket.BASE_SIZE)) {
                        takeDamage(4f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Sniper -> {
                    if (weapon.bulletActive && lineOverlaps(
                            weapon.bulletStart, weapon.bulletEnd,
                            position, width, height,
                        )
                    ) {
                        takeDamage(weapon.bulletDamage * gameState.damageMultiplier)
                        weapon.bulletActive = false
                    }
                }
            }
        }
    }

    private fun lineOverlaps(start: Vec2, end: Vec2, boxPos: Vec2, boxW: Float, boxH: Float): Boolean {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return overlaps(start, 1f, 1f)
        val steps = (len / (boxW.coerceAtMost(boxH) * 0.5f)).toInt().coerceAtLeast(20)
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val px = start.x + dx * t
            val py = start.y + dy * t
            if (px < boxPos.x + boxW && px + 4f > boxPos.x &&
                py < boxPos.y + boxH && py + 4f > boxPos.y) {
                return true
            }
        }
        return false
    }

    private fun overlaps(pos: Vec2, w: Float, h: Float): Boolean {
        return pos.x < position.x + width &&
                pos.x + w > position.x &&
                pos.y < position.y + height &&
                pos.y + h > position.y
    }

    fun takeDamage(amount: Float) {
        if (!isAlive) return
        health -= amount
        if (health <= 0f) {
            health = 0f
            isAlive = false
            gameState.bossHealthPercent = 0f
        }
    }

    fun forEachLaser(action: (startX: Float, startY: Float, endX: Float, endY: Float) -> Unit) {
        activeLaser?.forEachLaser(action)
    }

    fun forEachGatheredTile(action: (x: Float, y: Float) -> Unit) {
        activeImplosion?.forEachGatheredTile(action)
    }

    fun forEachShieldTile(action: (x: Float, y: Float) -> Unit) {
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            action(shieldPositions[i * 2], shieldPositions[i * 2 + 1])
        }
    }

    private fun spawnHandAttack() {
        val tileSize = WorldConstants.TILE_SIZE
        val startTX = ((player.center.x - HAND_WIDTH * tileSize / 2f) / tileSize).toInt()
        val startTY = ((player.center.y - 200f) / tileSize).toInt()
        hands.add(BossHand(startTX, startTY, world))
    }

    private fun spawnLavaTile() {
        val tileSize = WorldConstants.TILE_SIZE
        val bossTX = (center.x / tileSize).toInt()
        val bossTY = (center.y / tileSize).toInt()
        val tx = bossTX + Random.nextInt(-1, 2)
        val ty = bossTY + 2
        if (tx in 0 until WorldConstants.WORLD_WIDTH_TILES &&
            ty in 0 until WorldConstants.WORLD_HEIGHT_TILES &&
            world[tx, ty] == Tile.AIR
        ) {
            world[tx, ty] = Tile.LAVA
            fluidSimulator.registerFluid(tx, ty)
        }
    }

    private fun findFreeSlot(): Int? {
        for (i in shieldActive.indices) {
            if (!shieldActive[i]) return i
        }
        return null
    }

    companion object {
        const val SIZE = 32f
        const val HAND_WIDTH = 18
        const val HAND_HEIGHT = 16
    }
}
