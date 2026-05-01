package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
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
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.boss_sheet
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class BossAnimState { IDLE, ATTACK_1, ATTACK_2 }

fun interface BossCommand {
    fun update(deltaTime: Float): Boolean
}

class Boss(
    val position: Vec2,
    val width: Float = 64f,
    val height: Float = 64f,
    val drawWidth: Float = 128f,
    val drawHeight: Float = 128f,
    private val world: World,
    private val player: Player,
    private val fluidSimulator: FluidSimulator,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val gameState: GameState,
    private val maxHealth: Float = 100f,
) {

    val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.boss_sheet,
        frameWidth = 64,
        frameHeight = 64,
        columns = 16,
        totalSprites = 48,
        frameDuration = 0.1f,
    )

    val eye = BossEye()

    private val idleFrames = 0..9
    private val attack1Frames = 16..31
    private val attack2Frames = 32..47
    private var animState = BossAnimState.IDLE

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
    private val shieldRegenQueue = FloatArray(maxShieldTiles)
    private var shieldCount = 0
    private var regenPending = 0
    private val shieldSpawnInterval = 0.1f / 3f
    private val shieldRegenDelay = 5f
    private val orbitRadius = 60f
    private val orbitSpeed = 3f
    private var orbitAngle = 0f

    private val lavaSpawnInterval = 0.01f
    private val lavaStreamDuration = 3f

    private var activeLaser: BossLaser? = null
    private var activeEyeBeam: BossEyeBeam? = null
    private var activeBombField: BossBombField? = null

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
        updateShieldRegen(deltaTime)

        if (currentCommand == null) {
            if (commandQueue.isNotEmpty()) {
                currentCommand = commandQueue.removeFirst()
            } else {
                animState = BossAnimState.IDLE
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

        val eyeBeam = activeEyeBeam
        if (eyeBeam != null) {
            eyeBeam.update(deltaTime)
            if (!eyeBeam.alive) activeEyeBeam = null
        }

        val bombField = activeBombField
        if (bombField != null) {
            bombField.update(deltaTime)
            if (!bombField.alive) activeBombField = null
        }

        eye.update(center, player.center)
        updateAnimation(deltaTime)
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
        val pick = options.random()
        lastAttack = pick
        return when (pick) {
            0 -> { animState = BossAnimState.ATTACK_2; laserAttackCommand() }
            1 -> { animState = BossAnimState.ATTACK_2; eyeBeamCommand() }
            2 -> { animState = BossAnimState.ATTACK_1; lavaStreamCommand() }
            else -> { animState = BossAnimState.ATTACK_1; bombFieldCommand() }
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

    private fun eyeBeamCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activeEyeBeam = BossEyeBeam(
                    origin = { Vec2(center.x + eye.irisOffset.x, center.y + eye.irisOffset.y) },
                    player = player,
                    collisionDetector = collisionDetector,
                    world = world,
                    particleSystem = particleSystem,
                )
            }
            activeEyeBeam?.alive != true
        }
    }

    private fun bombFieldCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activeBombField = BossBombField(
                    world = world,
                    player = player,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                )
            }
            activeBombField?.alive != true
        }
    }

    private fun checkWeaponCollisions() {
        player.weapons.forEach { weapon ->
            when (weapon) {
                is MagicMissile -> {
                    if (!weapon.isAlive) return@forEach
                    if (hitShield(weapon.position, weapon.baseSize, weapon.baseSize)) {
                        weapon.isAlive = false
                    } else if (overlaps(weapon.position, weapon.baseSize, weapon.baseSize)) {
                        takeDamage(2f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Laser -> {
                    if (!weapon.isActive) return@forEach
                    if (lineHitsShield(weapon.start, weapon.end)) return@forEach
                    if (overlaps(weapon.end, 8f, 8f)) {
                        takeDamage(0.25f * gameState.damageMultiplier)
                    }
                }
                is SuperSoaker -> {
                    weapon.droplets.forEach { droplet ->
                        if (!droplet.alive) return@forEach
                        if (hitShield(droplet.position, 4f, 4f)) {
                            droplet.alive = false
                        } else if (overlaps(droplet.position, 4f, 4f)) {
                            takeDamage(0.15f * gameState.damageMultiplier)
                        }
                    }
                }

                is Rocket -> {
                    if (!weapon.isAlive) return@forEach
                    if (hitShield(weapon.position, Rocket.BASE_SIZE, Rocket.BASE_SIZE)) {
                        weapon.isAlive = false
                    } else if (overlaps(weapon.position, Rocket.BASE_SIZE, Rocket.BASE_SIZE)) {
                        takeDamage(4f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Sniper -> {
                    if (!weapon.bulletActive) return@forEach
                    if (lineHitsShield(weapon.bulletStart, weapon.bulletEnd)) {
                        weapon.bulletActive = false
                    } else if (lineOverlaps(
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

    private fun hitShield(pos: Vec2, w: Float, h: Float): Boolean {
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            val sx = shieldPositions[i * 2]
            val sy = shieldPositions[i * 2 + 1]
            if (pos.x < sx + tileSize && pos.x + w > sx &&
                pos.y < sy + tileSize && pos.y + h > sy) {
                destroyShield(i)
                return true
            }
        }
        return false
    }

    private fun lineHitsShield(start: Vec2, end: Vec2): Boolean {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return hitShield(start, 1f, 1f)
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        val steps = (len / (tileSize * 0.5f)).toInt().coerceAtLeast(20)
        for (s in 0..steps) {
            val t = s.toFloat() / steps
            val px = start.x + dx * t
            val py = start.y + dy * t
            for (i in 0 until maxShieldTiles) {
                if (!shieldActive[i]) continue
                val sx = shieldPositions[i * 2]
                val sy = shieldPositions[i * 2 + 1]
                if (px >= sx && px < sx + tileSize && py >= sy && py < sy + tileSize) {
                    destroyShield(i)
                    return true
                }
            }
        }
        return false
    }

    private fun destroyShield(i: Int) {
        val sx = shieldPositions[i * 2]
        val sy = shieldPositions[i * 2 + 1]
        shieldActive[i] = false
        shieldCount--
        shieldRegenQueue[i] = shieldRegenDelay
        regenPending++

        val dx = sx - center.x
        val dy = sy - center.y
        val mag = sqrt(dx * dx + dy * dy)
        val speed = 80f + Random.nextFloat() * 80f
        val vx = if (mag > 0.001f) dx / mag * speed else 0f
        val vy = if (mag > 0.001f) dy / mag * speed else -speed
        particleSystem.addParticle(
            position = Vec2(sx, sy),
            velocity = Vec2(vx, vy),
            tile = Tile.GOLD_ORE,
        )
    }

    private fun updateShieldRegen(deltaTime: Float) {
        if (regenPending == 0) return
        val cx = center.x
        val cy = center.y
        for (i in 0 until maxShieldTiles) {
            if (shieldActive[i]) continue
            val timer = shieldRegenQueue[i]
            if (timer <= 0f) continue
            val next = timer - deltaTime
            if (next > 0f) {
                shieldRegenQueue[i] = next
                continue
            }
            shieldRegenQueue[i] = 0f
            shieldPositions[i * 2] = cx
            shieldPositions[i * 2 + 1] = cy
            shieldActive[i] = true
            shieldCount++
            regenPending--
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

    fun forEachEyeBeam(action: (startX: Float, startY: Float, endX: Float, endY: Float, charging: Boolean) -> Unit) {
        activeEyeBeam?.renderBeam(action)
    }

    fun forEachBomb(action: (topLeftX: Float, topLeftY: Float, sizePx: Float, flashAlpha: Float) -> Unit) {
        activeBombField?.forEachBomb(action)
    }

    fun forEachShieldTile(action: (x: Float, y: Float) -> Unit) {
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            action(shieldPositions[i * 2], shieldPositions[i * 2 + 1])
        }
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

    private fun updateAnimation(deltaTime: Float) {
        when (animState) {
            BossAnimState.IDLE -> spriteAnimator.animate(deltaTime, idleFrames)
            BossAnimState.ATTACK_1 -> spriteAnimator.animate(deltaTime, attack1Frames)
            BossAnimState.ATTACK_2 -> spriteAnimator.animate(deltaTime, attack2Frames)
        }
    }

    private fun findFreeSlot(): Int? {
        for (i in shieldActive.indices) {
            if (!shieldActive[i]) return i
        }
        return null
    }
}