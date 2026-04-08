package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player

abstract class UltimateAttack(
    val id: String,
    val name: String,
    val requiredUnlockIds: Set<String>,
) {
    var isActive = false
        protected set

    abstract fun activate(player: Player)
    abstract fun update(deltaTime: Float, player: Player)
    abstract fun deactivate(player: Player)
    abstract fun isFinished(): Boolean
    abstract fun DrawScope.render(camera: Camera, player: Player, frameCount: Long)

    open fun onReactivate(): Boolean = false
}
