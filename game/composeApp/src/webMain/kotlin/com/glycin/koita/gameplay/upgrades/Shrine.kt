package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.util.overlapsWith
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.kotlin_shrine_sprite_sheet

class Shrine(
    val position: Vec2,
    val width: Float = 64f,
    val height: Float = 128f,
    val drawWidth: Float = 128f,
    val drawHeight: Float = 128f,
    val choices: List<Unlock>? = null,
) {
    private val idleFrame = 0
    private val chargingFrames = 1..8

    val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.kotlin_shrine_sprite_sheet,
        frameWidth = 256,
        frameHeight = 256,
        columns = 9,
        totalSprites = 9,
        frameDuration = ACTIVATION_TIME / chargingFrames.count(),
    )

    val center get() = Vec2(position.x + width / 2f, position.y + height / 2f)

    var state = ShrineState.IDLE
        private set
    var activationTimer = 0f
        private set
    var isActivated = false
        private set

    fun startActivation() {
        if (state == ShrineState.IDLE && !isActivated) {
            state = ShrineState.CHARGING
            spriteAnimator.setFrame(chargingFrames.first)
            activationTimer = 0f
        }
    }

    fun cancelActivation() {
        if (state == ShrineState.CHARGING) {
            state = ShrineState.IDLE
            activationTimer = 0f
            spriteAnimator.setFrame(idleFrame)
        }
    }

    fun update(deltaTime: Float) {
        if (isActivated) return

        if (state == ShrineState.CHARGING) {
            activationTimer += deltaTime
            spriteAnimator.animate(deltaTime, chargingFrames)
            if (activationTimer >= ACTIVATION_TIME) {
                isActivated = true
            }
        }
    }

    fun overlapsPlayer(player: Player) = player.overlapsWith(position, width.toInt(), height.toInt())

    companion object {
        const val ACTIVATION_TIME = 3f
    }
}
