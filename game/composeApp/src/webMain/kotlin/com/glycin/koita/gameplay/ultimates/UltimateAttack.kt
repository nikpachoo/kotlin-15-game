package com.glycin.koita.gameplay.ultimates

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.core.Camera
import com.glycin.koita.core.Player
import com.glycin.koita.gameplay.upgrades.UnlockId

abstract class UltimateAttack(
    val id: UltimateId,
    val name: String,
    val requiredUnlockIds: Set<UnlockId>,
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
