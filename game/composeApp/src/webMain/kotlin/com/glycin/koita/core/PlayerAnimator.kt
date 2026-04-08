package com.glycin.koita.core

import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.kodee_sheet
import org.jetbrains.compose.resources.DrawableResource

class PlayerAnimator(
    sprite: DrawableResource = Res.drawable.kodee_sheet,
    sheetWidth: Int = 2560,
    sheetHeight: Int = 6144,
    frameWidth: Int = 256,
    frameHeight: Int = 256,
    frameDuration: Float = 0.05f,
) {
    private val columns = sheetWidth / frameWidth
    private val totalSprites = columns * (sheetHeight / frameHeight)

    private val walkFrames: IntRange = 170..185
    private val idleFrames: IntRange = 90..110
    private val jumpFrames: IntRange = 130..140
    private val fallFrames: IntRange = 50..65
    private val attackFrames: IntRange = 200..209
    private val hurtFrames: IntRange = 71..81
    private val boostFrames: IntRange = 0..15
    private val deathFrames: IntRange = 20..45

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
        }
    }
}
