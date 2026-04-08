package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.core.Player
import com.glycin.koita.core.Vec2
import com.glycin.koita.util.overlapsWith
import kotlin.math.min

class UnlockOrb(
    val unlock: Unlock,
    val targetPosition: Vec2,
    val groupId: Int,
    private val startPosition: Vec2,
) {
    val position = Vec2(startPosition.x, startPosition.y)
    val size = 24f

    private val flyDuration = 0.6f
    private var flyTimer = 0f

    var isFlying = true
        private set

    val center get() = Vec2(position.x + size / 2f, position.y + size / 2f)

    fun update(deltaTime: Float) {
        if (!isFlying) return

        flyTimer += deltaTime
        val t = min(flyTimer / flyDuration, 1f)
        // Ease-out cubic
        val eased = 1f - (1f - t) * (1f - t) * (1f - t)

        position.x = startPosition.x + (targetPosition.x - startPosition.x) * eased
        position.y = startPosition.y + (targetPosition.y - startPosition.y) * eased

        if (t >= 1f) {
            isFlying = false
        }
    }

    fun overlapsPlayer(player: Player): Boolean {
        val intSize = size.toInt()
        return player.overlapsWith(position, intSize, intSize)
    }
}
