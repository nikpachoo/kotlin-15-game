package com.glycin.koita.gameplay

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.TWO_PI
import com.glycin.koita.util.spawnRadialBurst
import com.glycin.koita.world.Tile
import kotlin.math.cos
import kotlin.math.sin

class ResourceShield(
    private val player: Player,
    private val gameState: GameState,
    private val particleSystem: ParticleSystem,
) {
    private val maxRects = 16
    private val orbitRadius = 56f
    private val orbitSpeed = 2f
    private val regenDelay = 2.5f
    private val totalRegenCost = 1000
    private val followFactor = 10f

    private val positions = FloatArray(maxRects * 2)
    private val active = BooleanArray(maxRects)
    private var orbitAngle = 0f
    private var cooldownTimer = 0f
    private var activeCount = 0
    private var firstSpawnFree = true

    fun update(deltaTime: Float) {
        if (!gameState.resourceShield) {
            reset()
            return
        }
        val center = player.center
        orbitAngle += orbitSpeed * deltaTime
        updateOrbit(deltaTime, center)
        updateRegen(deltaTime, center)
    }

    fun tryAbsorbHit(): Boolean {
        if (!gameState.resourceShield) return false
        if (activeCount == 0) return false
        consumeAll()
        return true
    }

    fun forEachActiveRect(action: (x: Float, y: Float) -> Unit) {
        for (i in 0..<maxRects) {
            if (!active[i]) continue
            action(positions[i * 2], positions[i * 2 + 1])
        }
    }

    private fun updateOrbit(deltaTime: Float, center: Vec2) {
        val follow = followFactor * deltaTime
        val cx = center.x
        val cy = center.y
        for (i in 0..<maxRects) {
            if (!active[i]) continue
            val angle = orbitAngle + (i.toFloat() / maxRects) * TWO_PI
            val targetX = cx + cos(angle) * orbitRadius
            val targetY = cy + sin(angle) * orbitRadius
            val i2 = i * 2
            positions[i2] += (targetX - positions[i2]) * follow
            positions[i2 + 1] += (targetY - positions[i2 + 1]) * follow
        }
    }

    private fun updateRegen(deltaTime: Float, center: Vec2) {
        if (activeCount > 0) return
        if (cooldownTimer > 0f) {
            cooldownTimer = (cooldownTimer - deltaTime).coerceAtLeast(0f)
            return
        }
        if (firstSpawnFree) {
            firstSpawnFree = false
        } else {
            if (gameState.collectedMinerals < totalRegenCost) return
            gameState.collectedMinerals -= totalRegenCost
        }
        for (i in 0..<maxRects) {
            spawnAt(i, center)
        }
    }

    private fun spawnAt(index: Int, center: Vec2) {
        positions[index * 2] = center.x
        positions[index * 2 + 1] = center.y
        active[index] = true
        activeCount++
    }

    private fun consumeAll() {
        val center = player.center
        for (i in 0..<maxRects) {
            if (!active[i]) continue
            particleSystem.spawnRadialBurst(positions[i * 2], positions[i * 2 + 1], center.x, center.y, Tile.STONE)
            active[i] = false
        }
        activeCount = 0
        cooldownTimer = regenDelay
    }

    private fun reset() {
        for (i in 0..<maxRects) {
            active[i] = false
        }
        activeCount = 0
        cooldownTimer = 0f
        firstSpawnFree = true
    }
}
