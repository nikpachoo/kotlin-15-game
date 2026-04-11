package com.glycin.koita.core

import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.drone_sheet

enum class DroneState {
    BUILD_IDLE,
    BUILD_ACTIVE,
    ATTACK_IDLE,
    ATTACK_ACTIVE,
    MINING_IDLE,
    MINING_ACTIVE,
}

class DroneAnimator {

    private val buildIdleFrames: IntRange = 18..20
    private val buildActiveFrames: IntRange = 9..17
    private val attackIdleFrames: IntRange = 6..8
    private val attackActiveFrames: IntRange = 0..5
    private val miningIdleFrames: IntRange = 28..30
    private val miningActiveFrames: IntRange = 21..27

    private val spriteAnimator = SpriteAnimator(
        sprite = Res.drawable.drone_sheet,
        frameWidth = 64,
        frameHeight = 64,
        columns = 31,
        totalSprites = 31,
        frameDuration = 0.05f,
    )

    val sprite get() = spriteAnimator.sprite
    val srcX get() = spriteAnimator.srcX
    val srcY get() = spriteAnimator.srcY
    val frameWidth get() = spriteAnimator.frameWidth
    val frameHeight get() = spriteAnimator.frameHeight

    fun update(deltaTime: Float, state: DroneState) {
        when (state) {
            DroneState.BUILD_IDLE -> spriteAnimator.animate(deltaTime, buildIdleFrames)
            DroneState.BUILD_ACTIVE -> spriteAnimator.animate(deltaTime, buildActiveFrames)
            DroneState.ATTACK_IDLE -> spriteAnimator.animate(deltaTime, attackIdleFrames)
            DroneState.ATTACK_ACTIVE -> spriteAnimator.animate(deltaTime, attackActiveFrames)
            DroneState.MINING_IDLE -> spriteAnimator.animate(deltaTime, miningIdleFrames)
            DroneState.MINING_ACTIVE -> spriteAnimator.animate(deltaTime, miningActiveFrames)
        }
    }
}
