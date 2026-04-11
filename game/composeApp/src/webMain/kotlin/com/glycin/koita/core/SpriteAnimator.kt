package com.glycin.koita.core

import org.jetbrains.compose.resources.DrawableResource

class SpriteAnimator(
    val sprite: DrawableResource,
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int,
    val totalSprites: Int,
    private val frameDuration: Float = 0.1f,
) {
    private var currentFrame = 0
    private var elapsed = 0f
    private var range = 0..<totalSprites

    private var oneShot = false
    private var onOneShotComplete: (() -> Unit)? = null

    val srcX get() = (currentFrame % columns) * frameWidth
    val srcY get() = (currentFrame / columns) * frameHeight

    fun setFrame(frame: Int) {
        currentFrame = frame.coerceIn(0, totalSprites - 1)
        elapsed = 0f
    }

    fun animateOneShot(deltaTime: Float, frames: IntRange, onComplete: () -> Unit) {
        if (!oneShot || range != frames) {
            oneShot = true
            onOneShotComplete = onComplete
            range = frames
            currentFrame = frames.first
            elapsed = 0f
        }
        advanceFrame(deltaTime)
    }

    fun animate(deltaTime: Float, frames: IntRange) {
        if (oneShot) {
            advanceFrame(deltaTime)
            return
        }

        if (range != frames) {
            range = frames
            currentFrame = currentFrame.coerceIn(range)
            elapsed = 0f
        }

        advanceFrame(deltaTime)
    }

    private fun advanceFrame(deltaTime: Float) {
        elapsed += deltaTime
        if (elapsed >= frameDuration) {
            elapsed -= frameDuration
            currentFrame++
            if (currentFrame > range.last) {
                if (oneShot) {
                    oneShot = false
                    val callback = onOneShotComplete
                    onOneShotComplete = null
                    callback?.invoke()
                } else {
                    currentFrame = range.first
                }
            }
        }
    }
}
