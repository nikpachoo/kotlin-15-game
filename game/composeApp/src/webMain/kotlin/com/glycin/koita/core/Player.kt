package com.glycin.koita.core

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import com.glycin.koita.audio.Music
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.gameplay.weapon.Weapon
import com.glycin.koita.gameplay.Drone
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.ModifierConfiguration
import com.glycin.koita.gameplay.ResourceShield
import com.glycin.koita.gameplay.modes.BuildMode
import com.glycin.koita.gameplay.modes.MiningMode
import com.glycin.koita.gameplay.modes.AttackMode
import com.glycin.koita.gameplay.SpawnSettings
import com.glycin.koita.gameplay.enemies.EnemyManager
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.physics.PhysicsConstants
import com.glycin.koita.util.explodeTerrain
import com.glycin.koita.world.Tile
import com.glycin.koita.world.World
import com.glycin.koita.world.WorldConstants

private const val SURFACE_MUSIC_TRIGGER_Y = WorldConstants.WORLD_HEIGHT_PIXELS * 0.35f

class Player(
    var position: Vec2,
    var width: Float = 32f,
    var height: Float = 32f,
    val drawWidth: Float = 90f,
    val drawHeight: Float = 90f,
    val spriteFeetRatio: Float = 0.75f, // How far down the sprite frame the feet are. This because there is quite some empty space in the spritesheets
    private val drone: Drone,
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
    private val particleSystem: ParticleSystem,
    private val enemyManager: EnemyManager,
    private val world: World,
    input: Input,
) {
    private val keyMap = input.keyMap
    private val mouse = input.mouse

    private var isGrounded = false
    private var hasDoubleJumped = false
    private var jumpPressed = false
    private var fallingTimer = 0f

    private var jetpackFuel = PlayerSettings.JETPACK_MAX_FUEL
    private var jetpackEngaged = false

    private var dashTimer = 0f
    private var dashCooldownTimer = 0f
    private var dashDirection = Vec2.zero()
    private var isDashing = false
    private var shiftPressed = false

    private var isGroundPounding = false
    private var groundPoundCooldownTimer = 0f
    private var healCooldownTimer by mutableStateOf(0f)

    private var lavaDamageTimer = 0f

    private var drownTimer = 0f
    private var drownDamageTimer = 0f
    private var isSubmerged = false

    private var hoverFuel = PlayerSettings.HOVER_MAX_FUEL
    private var hoverEngaged = false

    private var isAnchored = false
    private var anchorExitTimer = 0f
    val isAnchorLocked: Boolean
        get() = isAnchored || anchorExitTimer > 0f

    private var ultimateVelocityOverride: Vec2? = null

    private var pullTimer = 0f
    private var pullVelocityY = 0f

    private var slimedNotificationShown = false

    private var invulnerabilityTimer = 0f

    fun applyUltimateVelocity(velocity: Vec2) {
        ultimateVelocityOverride = velocity
    }

    fun clearUltimateVelocity() {
        ultimateVelocityOverride = null
    }

    fun applyShrineLift() {
        if (isAnchorLocked) return
        velocity = Vec2(velocity.x, -PlayerSettings.SHRINE_LIFT_FORCE)
        isGrounded = false
        hasDoubleJumped = false
        isGroundPounding = false
        pullTimer = 0f
        jetpackEngaged = false
        hoverEngaged = false
    }

    fun applyDigBoost(impactPoint: Vec2) {
        if (ModifierConfiguration.noMiningBoost) return
        if (isAnchorLocked || isGrounded || isDashing || isGroundPounding) return
        if (impactPoint.y >= center.y) return

        val direction = (impactPoint - center).normalized()
        pullVelocityY = direction.y * PlayerSettings.MINING_BOOST_FORCE
        pullTimer = PlayerSettings.MINING_PULL_DURATION
    }

    private var velocity = Vec2(0f, 0f)
    private var state = PlayerState.IDLE
    var facing = PlayerFacing.RIGHT
        private set

    var dronePosition: Vec2 = Vec2(position.x + width / 2f, position.y + height / 2f) + Vec2.up() * 2.0f

    private val droneRangeX = 40f
    private val droneRangeY = 30f

    val animator = PlayerAnimator()

    private val onHurtComplete: () -> Unit = {
        state = PlayerState.IDLE
        droneState = getDroneIdleState()
    }
    private val onDeathComplete: () -> Unit = {
        isDead = true
    }
    var isDead by mutableStateOf(false)
        private set
    private val onVictoryComplete: () -> Unit = {
        isVictorious = true
    }
    var isVictorious by mutableStateOf(false)
        private set
    val droneAnimator = DroneAnimator()
    var droneState = DroneState.MINING_IDLE

    //TODO: Keep attacks here or inject the weapons in the renderer?
    val weapons = mutableStateListOf<Weapon>()
    var center = Vec2(position.x + width / 2f, position.y + height / 2f)

    var health by mutableStateOf(10)
    var maxHealth by mutableStateOf(10)
    var currentWeapon by mutableStateOf(drone.modes.first())


    var giantScale = 1f

    private var attackTimer = 0f
    private val attackDuration = 0.3f

    private val resourceShield = ResourceShield(this, gameState, world)

    init {
        drone.getMiningMode()?.onCollectHit = ::applyDigBoost
    }

    fun update(deltaTime: Float) {
        if (state == PlayerState.DEAD || state == PlayerState.VICTORY) {
            animator.update(deltaTime, state, onHurtComplete, onDeathComplete, onVictoryComplete)
            return
        }
        center = Vec2(position.x + width / 2f, position.y + height / 2f) // Small optimization, calculate the center once each frame
        updatePosition(deltaTime)
        updateAttacks(deltaTime)
        updateCurrentWeapon(deltaTime, center)
        updateTimers(deltaTime)
        updateDrone(center)
        resourceShield.update(deltaTime)
        updateScoreMultiplier()
        animator.update(deltaTime, state, onHurtComplete, onDeathComplete, onVictoryComplete)
        droneAnimator.update(deltaTime, droneState)
    }

    private fun updateScoreMultiplier() {
        val newMult = SpawnSettings.scoreMultiplierForY(center.y)
        val oldMult = gameState.scoreMultiplier
        if (newMult == oldMult) return
        gameState.scoreMultiplier = newMult
        if (newMult > oldMult) {
            gameState.pickupNotification = "Score multiplier ×$newMult!"
        }
    }

    fun enterBoostState() {
        if (state == PlayerState.DEAD || state == PlayerState.VICTORY) return
        state = PlayerState.BOOST
    }

    fun enterVictoryState() {
        if (state == PlayerState.DEAD || state == PlayerState.VICTORY) return
        state = PlayerState.VICTORY
        velocity = Vec2.zero()
        droneState = getDroneIdleState()
    }

    private fun updateTimers(deltaTime: Float) {
        if (attackTimer > 0f) {
            attackTimer -= deltaTime
            if (attackTimer <= 0f) {
                attackTimer = 0f
                if (state == PlayerState.ATTACKING) {
                    state = PlayerState.IDLE
                    droneState = getDroneIdleState()
                }
            }
        }
        if (healCooldownTimer > 0f) {
            healCooldownTimer = (healCooldownTimer - deltaTime).coerceAtLeast(0f)
        }
        if (invulnerabilityTimer > 0f) {
            invulnerabilityTimer = (invulnerabilityTimer - deltaTime).coerceAtLeast(0f)
        }
    }

    fun useWeapon() {
        if (state == PlayerState.DEAD || state == PlayerState.VICTORY) return
        if (isAnchorLocked) return
        if (gameState.ultimateActive) return
        currentWeapon.use()
        state = PlayerState.ATTACKING
        attackTimer = attackDuration
        droneState = getDroneActiveState()
        when(val weapon = currentWeapon) {
            is AttackMode -> {
                val activeWeapons = weapon.getActiveWeapon()
                activeWeapons.forEach { w ->
                    if (w !in weapons) {
                        weapons.add(w)
                    }
                }
            }
        }
    }

    fun equip(weaponIndex: Int) {
        currentWeapon = drone.modes[weaponIndex]
        gameState.selectedHotkeyIndex = weaponIndex
        droneState = getDroneIdleState()
        when (val weapon = currentWeapon) {
            is BuildMode -> weapon.onEquipped()
        }
    }

    fun takeDamage(amount: Int) {
        if (state == PlayerState.DEAD || state == PlayerState.VICTORY) return
        if (gameState.ultimateActive) return
        if (isAnchorLocked) return
        if (invulnerabilityTimer > 0f) return
        health = (health - amount).coerceAtLeast(0)
        SoundManager.playOneShot(Sounds.HIT)
        droneState = getDroneIdleState()
        if (health == 0) {
            state = PlayerState.DEAD
            velocity = Vec2.zero()
            return
        }
        invulnerabilityTimer = PlayerSettings.INVULNERABILITY_DURATION
        state = PlayerState.HURT
    }

    val canHeal: Boolean by derivedStateOf {
        !ModifierConfiguration.noHeal &&
            healCooldownTimer <= 0f &&
            gameState.collectedRich >= gameState.nextHealCost &&
            health < maxHealth
    }

    fun heal() {
        when {
            ModifierConfiguration.noHeal -> {
                gameState.pickupNotification = "Na-uh"
                return
            }
            health >= maxHealth -> {
                gameState.pickupNotification = "Already at full health"
                return
            }
            healCooldownTimer > 0f -> {
                gameState.pickupNotification = "Heal on cooldown"
                return
            }
            gameState.collectedRich < gameState.nextHealCost -> {
                gameState.pickupNotification = "Need ${gameState.nextHealCost} ORE"
                return
            }
        }
        gameState.collectedRich -= gameState.nextHealCost
        health = (health + 1).coerceAtMost(maxHealth)
        healCooldownTimer = PlayerSettings.HEAL_COOLDOWN_SECONDS
        gameState.nextHealCost *= 2
        gameState.pickupNotification = "+1 HP"
    }

    // TODO: im rechecking the neighboroing tiles multiple times here, maybe check them once for each frame and memoize the result
    private fun updatePosition(deltaTime: Float) {
        val ultimateOverride = ultimateVelocityOverride
        if (ultimateOverride != null) {
            position += ultimateOverride * deltaTime
            position = Vec2(
                x = position.x.coerceIn(0f, WorldConstants.WORLD_WIDTH_PIXELS - width),
                y = position.y.coerceIn(0f, WorldConstants.WORLD_HEIGHT_PIXELS - height),
            )
            velocity = Vec2.zero()
            isGrounded = false
            currentWeapon.position = position
            return
        }

        handleAnchor(deltaTime)
        if (isAnchorLocked) {
            velocity = Vec2.zero()
            currentWeapon.position = position
            state = PlayerState.IMMUTABLE
            return
        }

        val slimed = isFeetOnTile(Tile.SLIME)
        if (slimed && isGroundPounding) {
            isGroundPounding = false
        }

        if (slimed) {
            val tryingToMove = keyMap[Key.A] == true || keyMap[Key.D] == true
            if (tryingToMove && !slimedNotificationShown) {
                gameState.pickupNotification = "You are stuck on slime!"
                slimedNotificationShown = true
            }
        } else {
            slimedNotificationShown = false
        }

        val lavaRatio = tileOverlapRatio(Tile.LAVA)
        val submergedInLava = lavaRatio >= 1.0f
        val submergedInWater = tileOverlapRatio(Tile.WATER) >= 1.0f
        isSubmerged = submergedInWater || submergedInLava

        if (lavaRatio > PlayerSettings.LAVA_DAMAGE_THRESHOLD) {
            lavaDamageTimer -= deltaTime
            if (lavaDamageTimer <= 0f) {
                takeDamage(1)
                lavaDamageTimer = PlayerSettings.LAVA_DAMAGE_INTERVAL
                if (state == PlayerState.DEAD) return
            }
        } else {
            lavaDamageTimer = 0f
        }

        if (submergedInWater) {
            drownTimer += deltaTime
            if (drownTimer >= PlayerSettings.DROWN_GRACE_PERIOD) {
                drownDamageTimer -= deltaTime
                if (drownDamageTimer <= 0f) {
                    takeDamage(1)
                    drownDamageTimer = PlayerSettings.DROWN_DAMAGE_INTERVAL
                    if (state == PlayerState.DEAD) return
                }
            }
        } else {
            drownTimer = 0f
            drownDamageTimer = 0f
        }

        val horizontalInput = if (slimed || isGroundPounding) 0f else when {
            keyMap[Key.A] == true -> -1f
            keyMap[Key.D] == true -> 1f
            else -> 0f
        }

        if (horizontalInput != 0f) {
            facing = if (horizontalInput > 0f) PlayerFacing.RIGHT else PlayerFacing.LEFT
        }

        if (!slimed) handleDash(horizontalInput, deltaTime)

        if (isDashing) {
            val dashMove = dashDirection * PlayerSettings.DASH_SPEED * deltaTime
            val newPos = position + dashMove
            if (!collisionDetector.checkAABB(newPos, width, height, position)) {
                position = newPos
            } else {
                isDashing = false
                dashTimer = 0f
            }
        } else {
            val speedMultiplier = if (isSubmerged) PlayerSettings.SUBMERGED_SPEED_MULTIPLIER else 1f
            val moveX = horizontalInput * PlayerSettings.BASE_SPEED * speedMultiplier * deltaTime
            if (moveX != 0f) {
                val newPosX = Vec2(position.x + moveX, position.y)
                if (!collisionDetector.checkAABB(newPosX, width, height, position)) {
                    position = newPosX
                } else if (isGrounded) {
                    tryStepUp(newPosX)
                }
            }

            if (pullTimer > 0f) {
                pullTimer -= deltaTime
                velocity = Vec2(velocity.x, pullVelocityY)
            } else if (isGroundPounding) {
                velocity = Vec2(0f, PlayerSettings.GROUND_POUND_SPEED)
            } else if (hoverEngaged && keyMap[Key.Spacebar] == true && hoverFuel > 0f) {
                velocity = Vec2(velocity.x, (velocity.y + PhysicsConstants.GRAVITY * PlayerSettings.HOVER_GRAVITY_FACTOR * deltaTime).coerceAtMost(PlayerSettings.HOVER_MAX_FALL_SPEED))
            } else {
                val gravityMultiplier = if (isSubmerged) PlayerSettings.SUBMERGED_GRAVITY_MULTIPLIER else 1f
                velocity = Vec2(velocity.x, (velocity.y + PhysicsConstants.GRAVITY * gravityMultiplier * deltaTime).coerceAtMost(PlayerSettings.MAX_FALL_SPEED))
            }

            val moveY = velocity.y * deltaTime
            val newPosY = Vec2(position.x, position.y + moveY)

            if (!collisionDetector.checkAABB(newPosY, width, height, position)) {
                position = newPosY
                isGrounded = false

                if (isGroundPounding && enemyManager.anyHostileColliding(position.x, position.y, width, height)) {
                    groundPoundImpact()
                    velocity = Vec2(0f, -PlayerSettings.GROUND_POUND_BOUNCE_FORCE)
                    hasDoubleJumped = false
                }
            } else {
                val wasFalling = velocity.y > 0f
                velocity = Vec2(velocity.x, 0f)
                if (wasFalling) {
                    isGrounded = true
                    if (isGroundPounding) {
                        groundPoundImpact()
                    }
                }
            }

            if (!isGrounded) {
                val groundCheckPos = position + Vec2(0f, height + 2f)
                isGrounded = collisionDetector.checkAABB(groundCheckPos, width, 1f)
                if (isGrounded) {
                    velocity = Vec2(velocity.x, 0f)
                }
            }
        }

        val spaceDown = keyMap[Key.Spacebar] == true
        val jumpJustPressed = spaceDown && !jumpPressed //TODO: Is it possible to do this without keeping track of an extra boolean?
        jumpPressed = spaceDown

        if (!slimed && !isGroundPounding) {
            val jumpConsumed = handleJump(jumpJustPressed)
            val jetpackConsumed = handleJetpack(jumpJustPressed && !jumpConsumed, spaceDown, deltaTime)
            handleHover(jumpJustPressed && !jumpConsumed && !jetpackConsumed, spaceDown, deltaTime)
        }

        handleGroundPound(deltaTime)

        if (!gameState.passedPortal) {
            val cd = collisionDetector
            if (position.y + height > cd.portalY && position.y < cd.portalY + cd.portalHeight) {
                if (position.x + width > cd.portalX && position.x < cd.portalX + cd.portalWidth) {
                    position = Vec2(position.x, cd.portalDestY)
                    velocity = Vec2.zero()
                    isGrounded = false
                    gameState.passedPortal = true
                }
            }
        }

        if (!gameState.reachedSurfaceMusic && position.y <= SURFACE_MUSIC_TRIGGER_Y) {
            gameState.reachedSurfaceMusic = true
            SoundManager.switchLoop(Music.BACKGROUND_TOP)
        }

        position = Vec2(
            x = position.x.coerceIn(0f, WorldConstants.WORLD_WIDTH_PIXELS - width),
            y = position.y.coerceIn(0f, WorldConstants.WORLD_HEIGHT_PIXELS - height)
        )

        if (position.y >= WorldConstants.WORLD_HEIGHT_PIXELS - height) {
            velocity = Vec2.zero()
            isGrounded = true
        }

        currentWeapon.position = position

        // DEBUG
        if(gameState.devMode && keyMap[Key.AltLeft] == true && keyMap[Key.W] == true) {
            val debugSpeed = PlayerSettings.BASE_SPEED * 10
            position += Vec2.up() * debugSpeed * deltaTime
        }

        updateState(deltaTime, horizontalInput)
    }

    private fun tryStepUp(blockedNewPos: Vec2): Boolean {
        val stepped = Vec2(blockedNewPos.x, blockedNewPos.y - PlayerSettings.STEP_UP_HEIGHT)
        if (collisionDetector.checkAABB(stepped, width, height, position)) return false
        position = stepped
        return true
    }

    private fun updateState(deltaTime: Float, horizontalInput: Float) {
        if (state == PlayerState.HURT || state == PlayerState.ATTACKING || state == PlayerState.DEAD || state == PlayerState.VICTORY) return

        state = when {
            !isGrounded && velocity.y < 0f -> {
                fallingTimer = 0f
                PlayerState.JUMPING
            }
            !isGrounded -> {
                fallingTimer += deltaTime
                if (fallingTimer >= 1f) PlayerState.FALLING else state
            }
            else -> {
                fallingTimer = 0f
                if (horizontalInput != 0f) PlayerState.WALKING else PlayerState.IDLE
            }
        }
    }

    private fun handleJump(jumpJustPressed: Boolean): Boolean {
        if (!jumpJustPressed) return false

        val swimMultiplier = if (isSubmerged) PlayerSettings.SUBMERGED_JUMP_MULTIPLIER else 1f
        val giantMultiplier = if (giantScale > 1f) 2f else 1f

        if (isGrounded) {
            val jumpMultiplier = if (isFeetOnTile(Tile.BOUNCY)) PlayerSettings.JUMP_PAD_MULTIPLIER else 1f
            velocity = Vec2(0f, -PlayerSettings.BASE_JUMP_FORCE * jumpMultiplier * swimMultiplier * giantMultiplier)
            SoundManager.playOneShot(Sounds.JUMP)
            isGrounded = false
            hasDoubleJumped = false
            pullTimer = 0f
            return true
        } else if (gameState.canDoubleJump && !hasDoubleJumped) {
            velocity = Vec2(0f, -PlayerSettings.BASE_JUMP_FORCE * swimMultiplier * giantMultiplier)
            SoundManager.playOneShot(Sounds.JUMP)
            hasDoubleJumped = true
            pullTimer = 0f
            return true
        }

        return false
    }

    private fun handleJetpack(jumpJustPressed: Boolean, spaceDown: Boolean, deltaTime: Float): Boolean {
        if (!gameState.canJetpack) return false

        if (isGrounded) {
            jetpackFuel = (jetpackFuel + deltaTime).coerceAtMost(PlayerSettings.JETPACK_MAX_FUEL)
            jetpackEngaged = false
            return false
        }

        if (!spaceDown) {
            jetpackEngaged = false
        }

        // Engage jetpack on a fresh press while airborne, after all jumps are spent
        if (jumpJustPressed && !isGrounded
            && (!gameState.canDoubleJump || hasDoubleJumped)
            && jetpackFuel > 0f
        ) {
            jetpackEngaged = true
            pullTimer = 0f
        }

        if (jetpackEngaged && spaceDown && jetpackFuel > 0f) {
            velocity = Vec2(velocity.x, (-PlayerSettings.JETPACK_FORCE).coerceAtMost(velocity.y))
            jetpackFuel -= deltaTime
            if (jetpackFuel <= 0f) {
                jetpackEngaged = false
            }
        }

        return jetpackEngaged
    }

    private fun handleDash(horizontalInput: Float, deltaTime: Float) {
        if (!gameState.canDash) return

        if (dashCooldownTimer > 0f) {
            dashCooldownTimer -= deltaTime
        }

        if (isDashing) {
            dashTimer -= deltaTime
            if (dashTimer <= 0f) {
                isDashing = false
                velocity = Vec2.zero()
            }
            return
        }

        val shiftDown = keyMap[Key.ShiftLeft] == true || keyMap[Key.ShiftRight] == true
        val shiftJustPressed = shiftDown && !shiftPressed
        shiftPressed = shiftDown

        if (shiftJustPressed && dashCooldownTimer <= 0f && horizontalInput != 0f) {
            dashDirection = Vec2(horizontalInput, 0f)
            isDashing = true
            dashTimer = PlayerSettings.DASH_DURATION
            dashCooldownTimer = PlayerSettings.DASH_COOLDOWN
            velocity = Vec2.zero()
            pullTimer = 0f
        }
    }

    private fun handleHover(jumpJustPressed: Boolean, spaceDown: Boolean, deltaTime: Float) {
        if (!gameState.canHover) return

        if (isGrounded) {
            hoverFuel = (hoverFuel + deltaTime).coerceAtMost(PlayerSettings.HOVER_MAX_FUEL)
            hoverEngaged = false
            return
        }

        if (!spaceDown) {
            hoverEngaged = false
        }

        if (jumpJustPressed && !isGrounded
            && velocity.y >= 0f
            && (!gameState.canDoubleJump || hasDoubleJumped)
            && (!gameState.canJetpack || jetpackFuel <= 0f)
            && !isGroundPounding && !isDashing
            && hoverFuel > 0f
        ) {
            hoverEngaged = true
        }

        if (hoverEngaged && spaceDown && hoverFuel > 0f) {
            hoverFuel -= deltaTime
            if (hoverFuel <= 0f) {
                hoverEngaged = false
            }
        }
    }

    private fun handleAnchor(deltaTime: Float) {
        if (!gameState.canAnchor) return

        if (anchorExitTimer > 0f) {
            anchorExitTimer -= deltaTime
            if (anchorExitTimer <= 0f) {
                isAnchored = false
            }
            return
        }

        val sDown = keyMap[Key.S] == true

        if (isAnchored && !sDown) {
            anchorExitTimer = PlayerSettings.ANCHOR_EXIT_DELAY
            return
        }

        if (!isAnchored && isGrounded && velocity.y <= 0f && sDown && !isGroundPounding && !isDashing) {
            isAnchored = true
            velocity = Vec2.zero()
        }
    }

    private fun handleGroundPound(deltaTime: Float) {
        if (!gameState.canGroundPound) return

        if (groundPoundCooldownTimer > 0f) {
            groundPoundCooldownTimer -= deltaTime
        }

        if (!isGroundPounding
            && !isGrounded
            && !isDashing
            && groundPoundCooldownTimer <= 0f
            && keyMap[Key.S] == true
            && tilesAboveGround() > 10
        ) {
            isGroundPounding = true
            hoverEngaged = false
            jetpackEngaged = false
            velocity = Vec2(0f, PlayerSettings.GROUND_POUND_SPEED)
            pullTimer = 0f
        }
    }

    private fun groundPoundImpact() {
        isGroundPounding = false
        groundPoundCooldownTimer = PlayerSettings.GROUND_POUND_COOLDOWN

        val impactPoint = Vec2(position.x + width / 2f, position.y + height)

        val affectedTiles = collisionDetector.getTilesInRadius(impactPoint, PlayerSettings.GROUND_POUND_RADIUS)
        SoundManager.playOneShot(Sounds.EXPLODE)
        explodeTerrain(affectedTiles, impactPoint, PlayerSettings.GROUND_POUND_RADIUS, world, particleSystem)

        enemyManager.damageInRange(
            impactPoint,
            PlayerSettings.GROUND_POUND_RADIUS,
            PlayerSettings.GROUND_POUND_DAMAGE * gameState.damageMultiplier,
        )
    }

    private fun tilesAboveGround(): Int {
        val tileSize = WorldConstants.TILE_SIZE
        val feetTileX = ((position.x + width / 2f) / tileSize).toInt()
        val feetTileY = ((position.y + height) / tileSize).toInt()
        for (dy in 1..WorldConstants.WORLD_HEIGHT_TILES) {
            val tile = world[feetTileX, feetTileY + dy]
            if (tile.isSolid) return dy
        }
        return WorldConstants.WORLD_HEIGHT_TILES
    }

    private fun tileOverlapRatio(tile: Tile): Float {
        val tileSize = WorldConstants.TILE_SIZE
        val minX = (position.x / tileSize).toInt()
        val maxX = ((position.x + width) / tileSize).toInt()
        val minY = (position.y / tileSize).toInt()
        val maxY = ((position.y + height) / tileSize).toInt()

        var total = 0
        var matching = 0
        for (ty in minY..maxY) {
            for (tx in minX..maxX) {
                total++
                if (world[tx, ty] == tile) matching++
            }
        }
        return if (total == 0) 0f else matching.toFloat() / total
    }

    private fun isFeetOnTile(tile: Tile): Boolean {
        val tileSize = WorldConstants.TILE_SIZE
        val feetY = ((position.y + height) / tileSize).toInt()
        val minX = (position.x / tileSize).toInt()
        val maxX = ((position.x + width) / tileSize).toInt()

        for (ty in feetY..feetY + 1) {
            for (tx in minX..maxX) {
                if (world[tx, ty] == tile) return true
            }
        }
        return false
    }

    private fun updateAttacks(deltaTime: Float) {
        weapons.removeAll { !it.isAlive }
    }

    private fun updateCurrentWeapon(deltaTime: Float, centerVec2: Vec2) {
        currentWeapon.position = position
        currentWeapon.pivotPoint = centerVec2
        currentWeapon.update(deltaTime)
        for (i in 0..<drone.modes.size) {
            drone.modes[i].updateBackground(deltaTime)
        }
    }

    private fun updateDrone(centerVec2: Vec2) {
        val dir = (mouse.worldPosition - center).normalized()
        dronePosition = Vec2(
            x = centerVec2.x + (dir.x * droneRangeX).coerceIn(-droneRangeX, droneRangeX),
            y = centerVec2.y + (dir.y * droneRangeY).coerceIn(-droneRangeY, droneRangeY),
        )

        drone.getAttackMode()?.dronePosition = dronePosition
    }

    private fun getDroneIdleState(): DroneState = when (currentWeapon) {
        is MiningMode -> DroneState.MINING_IDLE
        is BuildMode -> DroneState.BUILD_IDLE
        is AttackMode -> DroneState.ATTACK_IDLE
        else -> DroneState.MINING_IDLE
    }

    private fun getDroneActiveState(): DroneState = when (currentWeapon) {
        is MiningMode -> DroneState.MINING_ACTIVE
        is BuildMode -> DroneState.BUILD_ACTIVE
        is AttackMode -> DroneState.ATTACK_ACTIVE
        else -> DroneState.MINING_ACTIVE
    }

}
