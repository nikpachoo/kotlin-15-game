package com.glycin.koita.gameplay.pickups

import com.glycin.koita.audio.SoundManager
import com.glycin.koita.audio.Sounds
import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.gameplay.GameSettings
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.PhysicsConstants
import com.glycin.koita.util.overlapsWith
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class PickupManager(
    private val gameState: GameState,
    private val collisionDetector: CollisionDetector,
) {
    var onPlayerMaxHealthIncrease: (() -> Unit)? = null

    private val maxFallSpeed = 400f
    private val activePickups = mutableListOf<Pickup>()
    fun randomChanceSpawn(position: Vec2, dropChance: Float = 0.2f) {
        val chance = if (gameState.devMode) 1.0f else dropChance
        if (Random.nextFloat() > chance) return
        spawn(position)
    }

    fun spawnHealth(position: Vec2) {
        activePickups.add(
            HealthPickup(
                onPickup = { onPlayerMaxHealthIncrease?.invoke() },
                position = position.copy(),
            )
        )
    }

    fun spawn(position: Vec2) {
        val pos = position.copy()
        val totalWeight = GameSettings.PICKUP_WEIGHT_DAMAGE + GameSettings.PICKUP_WEIGHT_VISION + GameSettings.PICKUP_WEIGHT_HEALTH
        val roll = Random.nextInt(totalWeight)
        val pickup = when {
            roll < GameSettings.PICKUP_WEIGHT_DAMAGE ->
                DamagePickup(onPickup = { gameState.damageMultiplier += 0.5f }, position = pos)
            roll < GameSettings.PICKUP_WEIGHT_DAMAGE + GameSettings.PICKUP_WEIGHT_VISION ->
                VisionPickup(onPickup = { gameState.visionMultiplier += 0.25f }, position = pos)
            else ->
                HealthPickup(onPickup = { onPlayerMaxHealthIncrease?.invoke() }, position = pos)
        }
        activePickups.add(pickup)
    }

    fun update(deltaTime: Float, player: Player) {
        val testPos = Vec2.zero()
        val playerPos = player.position
        activePickups.forEach { pickup ->
            if (abs(pickup.position.y - playerPos.y) < 800f) {
                pickup.update(deltaTime)

                pickup.vy = min(pickup.vy + PhysicsConstants.GRAVITY * deltaTime, maxFallSpeed)
                val newY = pickup.position.y + pickup.vy * deltaTime
                val size = pickup.size.toFloat()
                testPos.x = pickup.position.x
                testPos.y = newY
                if (collisionDetector.checkAABB(testPos, size, size)) {
                    pickup.vy = 0f
                } else {
                    pickup.position.y = newY
                }
            }
        }

        activePickups.removeAll { pickup ->
            if (player.overlapsWith(pickup.position, pickup.size, pickup.size)) {
                pickup.onPickup()
                gameState.pickupNotification = pickup.name
                gameState.pickupCounts[pickup.name] = (gameState.pickupCounts[pickup.name] ?: 0) + 1
                SoundManager.playOneShot(Sounds.POWERUP_PICKUP)
                true
            } else {
                false
            }
        }
    }

    fun forEachInRange(playerY: Float, range: Int, action: (Pickup) -> Unit) {
        for(i in activePickups.indices) {
            val item = activePickups[i]
            if(abs(item.position.y - playerY) <= range) {
                action(item)
            }
        }
    }

    //TODO: Called per-frame by WorldRenderer. Cache a Set<DrawableResource> updated only on add/remove instead of allocating list+set+list every call.
    fun getDistinctSprites() = activePickups.map { it.spriteAnimator.sprite }.distinct()
}
