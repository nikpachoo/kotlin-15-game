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
import com.glycin.koita.util.lerp
import com.glycin.koita.util.spawnRadialBurst
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.boss_sheet
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

enum class BossAnimState { IDLE, ATTACK_1, ATTACK_2 }

private const val BOSS_NAME_PREFIX = "The"
private const val BOSS_NAME_SUFFIX = "Final Void"

private val BOSS_KEYWORD_POOL = listOf(
    "Public", "Private", "Protected", "Internal",
    "Static", "Final", "Abstract", "Open", "Override",
    "Synchronized", "Native", "Strictfp", "Default", "Void",
    "Inline", "Suspend", "Tailrec", "Infix", "Operator",
    "External", "Noinline", "Crossinline", "Expect", "Actual",
)

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
    private val maxHealth: Float = 200f,
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
    var renderOffset: Vec2 = Vec2.zero()
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

    private val contactDamage = 3
    private val contactDamageRadius = orbitRadius + 24f
    private val contactDamageInterval = 0.5f
    private var contactDamageCooldown = 0f

    private var activeLaser: BossLaser? = null
    private var activeEyeBeam: BossEyeBeam? = null
    private var activeBombField: BossBombField? = null
    private var activePolarityFlip: BossPolarityFlip? = null
    private var activeMeteorShower: BossMeteorShower? = null

    private val commandQueue = ArrayDeque<BossCommand>()
    private var currentCommand: BossCommand? = null
    private var idleTimer = 0f
    private var nextIdleInterval = rollIdleInterval()
    private var lastAttack = -1

    private val shuffledKeywords = BOSS_KEYWORD_POOL.shuffled()
    private var keywordCount = 0

    init {
        commandQueue.addLast(shieldCommand())
        commandQueue.addLast(lavaStreamCommand())
        gameState.bossName = "$BOSS_NAME_PREFIX $BOSS_NAME_SUFFIX"
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
                nextIdleInterval = rollIdleInterval()
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

        val polarityFlip = activePolarityFlip
        if (polarityFlip != null) {
            polarityFlip.update(deltaTime)
            if (!polarityFlip.alive) activePolarityFlip = null
        }

        val meteorShower = activeMeteorShower
        if (meteorShower != null) {
            meteorShower.update(deltaTime)
            if (!meteorShower.alive) activeMeteorShower = null
        }

        eye.update(center, player.center)
        updateAnimation(deltaTime)
        applyContactDamage(deltaTime)
        checkWeaponCollisions()
    }

    private fun applyContactDamage(deltaTime: Float) {
        if (contactDamageCooldown > 0f) {
            contactDamageCooldown -= deltaTime
            return
        }
        val pc = player.center
        val c = center
        if (Vec2.fastDistance(pc.x, pc.y, c.x, c.y) <= contactDamageRadius * contactDamageRadius) {
            player.takeDamage(contactDamage)
            contactDamageCooldown = contactDamageInterval
        }
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

    private fun rollIdleInterval(): Float {
        val base = Random.nextFloat() * 3f + 3f
        return if (health <= maxHealth * 0.5f) base * 0.5f else base
    }

    private fun pickRandomAttack(): BossCommand {
        val options = (0 until 6).filter { it != lastAttack }
        val pick = options.random()
        lastAttack = pick
        return when (pick) {
            0 -> { animState = BossAnimState.ATTACK_2; laserAttackCommand() }
            1 -> { animState = BossAnimState.ATTACK_2; eyeBeamCommand() }
            2 -> { animState = BossAnimState.ATTACK_1; lavaStreamCommand() }
            3 -> { animState = BossAnimState.ATTACK_1; bombFieldCommand() }
            4 -> { animState = BossAnimState.ATTACK_2; polarityFlipCommand() }
            5 -> { animState = BossAnimState.ATTACK_1; meteorShowerCommand() }
            else -> { animState = BossAnimState.ATTACK_2; eyeBeamCommand() }
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

    private fun polarityFlipCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activePolarityFlip = BossPolarityFlip(
                    world = world,
                    player = player,
                    fluidSimulator = fluidSimulator,
                    bossCenter = center,
                )
            }
            activePolarityFlip?.alive != true
        }
    }

    private fun meteorShowerCommand(): BossCommand {
        var started = false
        return BossCommand { deltaTime ->
            if (!started) {
                started = true
                activeMeteorShower = BossMeteorShower(
                    origin = { center },
                    world = world,
                    player = player,
                    collisionDetector = collisionDetector,
                    particleSystem = particleSystem,
                )
            }
            activeMeteorShower?.alive != true
        }
    }

    private fun checkWeaponCollisions() {
        player.weapons.forEach { weapon ->
            when (weapon) {
                is MagicMissile -> {
                    if (!weapon.isAlive) return@forEach
                    if (hitShield(weapon.position, weapon.baseSize, weapon.baseSize, weapon.bossShieldDamage)) {
                        weapon.isAlive = false
                    } else if (overlaps(weapon.position, weapon.baseSize, weapon.baseSize)) {
                        applyDamage(2f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Laser -> {
                    if (!isAlive) return@forEach
                    val shieldT = firstShieldHitT(weapon.start, weapon.end)
                    val bossT = lineHitsBoxT(weapon.start, weapon.end, position, width, height)

                    if (weapon.didDamageTick) {
                        val shieldFirst = shieldT != null && (bossT == null || shieldT <= bossT)
                        if (shieldFirst) {
                            lineHitsShield(weapon.start, weapon.end, weapon.bossShieldDamage)
                        } else if (bossT != null) {
                            applyDamage(0.25f * gameState.damageMultiplier)
                        }
                    }

                    truncateLaserEnd(weapon, shieldT, bossT)
                }
                is SuperSoaker -> {
                    weapon.droplets.forEach { droplet ->
                        if (!droplet.alive) return@forEach
                        if (hitShield(droplet.position, 4f, 4f, weapon.bossShieldDamage)) {
                            droplet.alive = false
                        } else if (overlaps(droplet.position, 4f, 4f)) {
                            applyDamage(0.15f * gameState.damageMultiplier)
                        }
                    }
                }

                is Rocket -> {
                    if (!weapon.isAlive) return@forEach
                    if (hitShield(weapon.position, Rocket.BASE_SIZE, Rocket.BASE_SIZE, weapon.bossShieldDamage)) {
                        weapon.detonate()
                    } else if (overlaps(weapon.position, Rocket.BASE_SIZE, Rocket.BASE_SIZE)) {
                        applyDamage(4f * gameState.damageMultiplier)
                        weapon.isAlive = false
                    }
                }
                is Sniper -> {
                    if (!weapon.bulletActive) return@forEach
                    if (lineHitsShield(weapon.bulletStart, weapon.bulletEnd, weapon.bossShieldDamage)) {
                        weapon.bulletActive = false
                    } else if (lineOverlaps(
                            weapon.bulletStart, weapon.bulletEnd,
                            position, width, height,
                        )
                    ) {
                        applyDamage(weapon.bulletDamage * gameState.damageMultiplier)
                        weapon.bulletActive = false
                    }
                }
            }
        }
    }

    private fun hitShield(pos: Vec2, w: Float, h: Float, damage: Int): Boolean {
        if (damage <= 0) return false
        val effective = damage + gameState.damageUpCount
        val tileSize = WorldConstants.TILE_SIZE.toFloat()
        var destroyed = 0
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            val sx = shieldPositions[i * 2]
            val sy = shieldPositions[i * 2 + 1]
            if (pos.x < sx + tileSize && pos.x + w > sx &&
                pos.y < sy + tileSize && pos.y + h > sy) {
                destroyShield(i)
                destroyed++
                if (destroyed >= effective) return true
            }
        }
        return destroyed > 0
    }

    private fun lineHitsShield(start: Vec2, end: Vec2, damage: Int): Boolean {
        if (damage <= 0) return false
        if (start.x == end.x && start.y == end.y) return hitShield(start, 1f, 1f, damage)
        val effective = damage + gameState.damageUpCount
        var destroyed = 0
        forEachShieldHitOnLine(start, end) { i, _ ->
            destroyShield(i)
            destroyed++
            destroyed >= effective
        }
        return destroyed > 0
    }

    private fun firstShieldHitT(start: Vec2, end: Vec2): Float? {
        var hitT: Float? = null
        forEachShieldHitOnLine(start, end) { _, t ->
            hitT = t
            true
        }
        return hitT
    }

    private inline fun forEachShieldHitOnLine(
        start: Vec2,
        end: Vec2,
        onHit: (shieldIndex: Int, t: Float) -> Boolean,
    ) {
        if (shieldCount == 0) return
        val dx = end.x - start.x
        val dy = end.y - start.y
        val len = sqrt(dx * dx + dy * dy)
        if (len == 0f) return
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
                    if (onHit(i, t)) return
                }
            }
        }
    }

    private fun lineHitsBoxT(
        start: Vec2,
        end: Vec2,
        boxPos: Vec2,
        boxW: Float,
        boxH: Float,
    ): Float? {
        val dx = end.x - start.x
        val dy = end.y - start.y
        if (dx == 0f && dy == 0f) {
            val inside = start.x >= boxPos.x && start.x < boxPos.x + boxW &&
                    start.y >= boxPos.y && start.y < boxPos.y + boxH
            return if (inside) 0f else null
        }

        var tEnter = Float.NEGATIVE_INFINITY
        var tExit = Float.POSITIVE_INFINITY

        if (dx == 0f) {
            if (start.x < boxPos.x || start.x > boxPos.x + boxW) return null
        } else {
            val invDx = 1f / dx
            val tx1 = (boxPos.x - start.x) * invDx
            val tx2 = (boxPos.x + boxW - start.x) * invDx
            tEnter = maxOf(tEnter, minOf(tx1, tx2))
            tExit = minOf(tExit, maxOf(tx1, tx2))
        }

        if (dy == 0f) {
            if (start.y < boxPos.y || start.y > boxPos.y + boxH) return null
        } else {
            val invDy = 1f / dy
            val ty1 = (boxPos.y - start.y) * invDy
            val ty2 = (boxPos.y + boxH - start.y) * invDy
            tEnter = maxOf(tEnter, minOf(ty1, ty2))
            tExit = minOf(tExit, maxOf(ty1, ty2))
        }

        if (tEnter > tExit || tExit < 0f || tEnter > 1f) return null
        return if (tEnter < 0f) 0f else tEnter
    }

    private fun truncateLaserEnd(weapon: Laser, shieldT: Float?, bossT: Float?) {
        val tMin = when {
            shieldT == null -> bossT ?: return
            bossT == null -> shieldT
            else -> minOf(shieldT, bossT)
        }
        weapon.end.x = weapon.start.x.lerp(weapon.end.x, tMin)
        weapon.end.y = weapon.start.y.lerp(weapon.end.y, tMin)
    }

    private fun destroyShield(i: Int) {
        val sx = shieldPositions[i * 2]
        val sy = shieldPositions[i * 2 + 1]
        shieldActive[i] = false
        shieldCount--
        shieldRegenQueue[i] = shieldRegenDelay
        regenPending++
        particleSystem.spawnRadialBurst(sx, sy, center.x, center.y, Tile.GOLD_ORE)
    }

    private fun updateShieldRegen(deltaTime: Float) {
        if (regenPending == 0) return
        val effectiveDelta = if (health <= maxHealth * 0.2f) deltaTime * 2f else deltaTime
        val cx = center.x
        val cy = center.y
        for (i in 0 until maxShieldTiles) {
            if (shieldActive[i]) continue
            val timer = shieldRegenQueue[i]
            if (timer <= 0f) continue
            val next = timer - effectiveDelta
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

    fun takeDamage(amount: Float, shieldDamage: Int = 1) {
        if (!isAlive) return
        if (shieldCount > 0) {
            consumeFirstActiveShields(shieldDamage + gameState.damageUpCount)
            return
        }
        applyDamage(amount)
    }

    private fun applyDamage(amount: Float) {
        if (!isAlive) return
        health -= amount
        if (health <= 0f) {
            health = 0f
            isAlive = false
            gameState.bossHealthPercent = 0f
        }
        updateBossNameKeywords()
    }

    // This will overflow the screen, which looks weird, but is very funny
    private fun updateBossNameKeywords() {
        val missing = (maxHealth - health).coerceAtLeast(0f)
        val target = (missing / (maxHealth * 0.1f)).toInt().coerceAtMost(shuffledKeywords.size)
        if (target <= keywordCount) return
        keywordCount = target
        gameState.bossName = shuffledKeywords.take(keywordCount).joinToString(
            prefix = "$BOSS_NAME_PREFIX ",
            separator = " ",
            postfix = " $BOSS_NAME_SUFFIX",
        )
    }

    private fun consumeFirstActiveShields(count: Int) {
        if (count <= 0) return
        var remaining = count
        for (i in 0 until maxShieldTiles) {
            if (shieldActive[i]) {
                destroyShield(i)
                remaining--
                if (remaining <= 0) return
            }
        }
    }

    fun destroyShieldsInRadius(pos: Vec2, radius: Float, maxCount: Int = Int.MAX_VALUE) {
        if (shieldCount == 0 || maxCount <= 0) return
        val effective = if (maxCount == Int.MAX_VALUE) maxCount else maxCount + gameState.damageUpCount
        val px = pos.x
        val py = pos.y
        val rSq = radius * radius
        val halfTile = WorldConstants.TILE_SIZE * 0.5f
        var destroyed = 0
        for (i in 0 until maxShieldTiles) {
            if (!shieldActive[i]) continue
            val sx = shieldPositions[i * 2] + halfTile
            val sy = shieldPositions[i * 2 + 1] + halfTile
            if (Vec2.fastDistance(px, py, sx, sy) <= rSq) {
                destroyShield(i)
                destroyed++
                if (destroyed >= effective) return
            }
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

    fun forEachPolarityTile(action: (x: Float, y: Float, tile: Tile) -> Unit) {
        activePolarityFlip?.forEachPulledTile(action)
    }

    val meteorRadius: Float get() = activeMeteorShower?.meteorRadius ?: 0f

    fun forEachMeteor(action: (x: Float, y: Float) -> Unit) {
        activeMeteorShower?.forEachMeteor(action)
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