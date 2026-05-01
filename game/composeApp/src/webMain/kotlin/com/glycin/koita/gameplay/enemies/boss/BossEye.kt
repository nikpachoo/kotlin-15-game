package com.glycin.koita.gameplay.enemies.boss

import com.glycin.koita.core.Vec2

class BossEye(
    val irisRadius: Float = 4f,
    private val maxIrisOffset: Float = 5f,
) {
    var irisOffset = Vec2.zero()

    fun update(bossCenter: Vec2, target: Vec2) {
        val direction = (target - bossCenter).normalized()
        irisOffset = direction * maxIrisOffset
    }
}
