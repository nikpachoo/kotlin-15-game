package com.glycin.koita.core

import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.kodee_sheet
import org.jetbrains.compose.resources.DrawableResource

class PlayerAnimator(
    sprite: DrawableResource = Res.drawable.kodee_sheet,
    sheetWidth: Int = 4096,
    sheetHeight: Int = 1280,
    frameWidth: Int = 256,
    frameHeight: Int = 256,
    frameDuration: Float = 0.1f,
) {
    private val columns = sheetWidth / frameWidth
    private val totalSprites = columns * (sheetHeight / frameHeight)

    private val walkFrames: IntRange = 64..71
    private val idleFrames: IntRange = 39..41
    private val jumpFrames: IntRange = 51..53
    private val fallFrames: IntRange = 26..28
    private val attackFrames: IntRange = 72..79
    private val hurtFrames: IntRange = 32..41
    private val boostFrames: IntRange = 0..6
    private val deathFrames: IntRange = 16..25
    private val immutableFrame: Int = 55

    val spriteAnimator = SpriteAnimator(
        sprite = sprite,
        frameWidth = frameWidth,
        frameHeight = frameHeight,
        columns = columns,
        totalSprites = totalSprites,
        frameDuration = frameDuration,
    )

    fun update(deltaTime: Float, state: PlayerState, onHurtComplete: () -> Unit) {
        when (state) {
            PlayerState.WALKING -> spriteAnimator.animate(deltaTime, walkFrames)
            PlayerState.IDLE -> spriteAnimator.animate(deltaTime, idleFrames)
            PlayerState.JUMPING -> spriteAnimator.animate(deltaTime, jumpFrames)
            PlayerState.FALLING -> spriteAnimator.animate(deltaTime, fallFrames)
            PlayerState.ATTACKING -> spriteAnimator.animate(deltaTime, attackFrames)
            PlayerState.HURT -> spriteAnimator.animateOneShot(deltaTime, hurtFrames, onHurtComplete)
            PlayerState.BOOST -> spriteAnimator.animate(deltaTime, boostFrames)
            PlayerState.IMMUTABLE -> spriteAnimator.setFrame(immutableFrame)
        }
    }
}
