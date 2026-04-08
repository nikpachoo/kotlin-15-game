package com.glycin.koita.gameplay.upgrades

import com.glycin.koita.core.Player
import com.glycin.koita.core.SpriteAnimator
import com.glycin.koita.core.Vec2
import com.glycin.koita.util.overlapsWith
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.kotlin_shrine_sprite_sheet

class Shrine(
    val position: Vec2,
    val width: Float = 32f,
    val height: Float = 48f,
    val drawWidth: Float = 64f,
    val drawHeight: Float = 64f,
    val unlock: Unlock,
) {
    val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.kotlin_shrine_sprite_sheet,
        frameWidth = 64,
        frameHeight = 64,
        columns = 5,
        totalSprites = 5,
        frameDuration = 0.6f,
    )

    val center get() = Vec2(position.x + width / 2f, position.y + height / 2f)

    private val idleFrame = 0
    private val activatingFrames = 0..4

    var isActivating = false
        private set
    var activationTimer = 0f
        private set
    var isActivated = false
        private set

    fun startActivation() {
        if (!isActivating && !isActivated) {
            isActivating = true
            activationTimer = 0f
        }
    }

    fun cancelActivation() {
        if (isActivating) {
            isActivating = false
            activationTimer = 0f
            spriteAnimator.setFrame(idleFrame)
        }
    }

    fun update(deltaTime: Float) {
        if (isActivated) return

        if (isActivating) {
            activationTimer += deltaTime
            spriteAnimator.animate(deltaTime, activatingFrames)
            if (activationTimer >= ACTIVATION_TIME) {
                isActivated = true
            }
        } else {
            spriteAnimator.setFrame(idleFrame)
        }
    }

    fun overlapsPlayer(player: Player) = player.overlapsWith(position, width.toInt(), height.toInt())

    companion object {
        const val ACTIVATION_TIME = 3f
    }
}
