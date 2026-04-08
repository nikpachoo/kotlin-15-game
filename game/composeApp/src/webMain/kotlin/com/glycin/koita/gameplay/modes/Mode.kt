package com.glycin.koita.gameplay.modes

import com.glycin.koita.core.Mouse
import com.glycin.koita.core.Vec2
import com.glycin.koita.physics.CollisionDetector
import com.glycin.koita.physics.ParticleSystem
import com.glycin.koita.util.degreesToRadians
import com.glycin.koita.world.World
import org.jetbrains.compose.resources.DrawableResource
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Clock

abstract class Mode(
    var position: Vec2,
    val width: Float,
    val height: Float,
    protected val world: World,
    protected val collisionDetector: CollisionDetector,
    protected val particleSystem: ParticleSystem,
    protected val mouse: Mouse,
) {
    var lastUseTime = 0L
    var rotation = 0f
    var pivotPoint = position
    abstract val coolDownMs: Long

    abstract fun use()
    abstract fun update(deltaTime: Float)

    fun canUse(): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        return currentTime - lastUseTime >= coolDownMs
    }

    protected fun used() {
        lastUseTime = Clock.System.now().toEpochMilliseconds()
    }

    protected fun getActivationPoint(spriteHeightPercentage: Float): Vec2 {
        val tipX = position.x + width / 2f
        val tipY = position.y - height * spriteHeightPercentage
        val offsetX = tipX - pivotPoint.x
        val offsetY = tipY - pivotPoint.y

        val rotationRadians = degreesToRadians(rotation)
        val rotatedX = offsetX * cos(rotationRadians) - offsetY * sin(rotationRadians)
        val rotatedY = offsetX * sin(rotationRadians) + offsetY * cos(rotationRadians)

        return Vec2(
            pivotPoint.x + rotatedX,
            pivotPoint.y + rotatedY
        )
    }
}