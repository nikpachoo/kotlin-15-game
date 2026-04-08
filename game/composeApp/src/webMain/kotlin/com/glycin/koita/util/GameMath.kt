package com.glycin.koita.util

import com.glycin.koita.core.Vec2
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Returns `1f + sin(this * frequency) * amplitude` — a unit pulse value useful for
 * scaling radii / sizes in render code.
 */
fun Float.pulse(frequency: Float, amplitude: Float = 1f): Float =
    1f + sin(this * frequency) * amplitude

const val TWO_PI = (2.0 * PI).toFloat()

fun radiansToDegrees(radians: Float): Float =
    radians * 180f / PI.toFloat()

fun degreesToRadians(degrees: Float): Float =
    degrees * PI.toFloat() / 180f

fun angleTo(fromX: Float, fromY: Float, toX: Float, toY: Float): Float {
    val dx = toX - fromX
    val dy = toY - fromY
    return radiansToDegrees(atan2(dy, dx))
}

fun Vec2.angleTo(target: Vec2): Float =
    angleTo(this.x, this.y, target.x, target.y)

/**
 * FloatArray variant of [steerToward] for hot loops that store directions in packed arrays.
 * Steers the (x, y) at [directions]\[i2], [directions]\[i2 + 1] toward [targetX], [targetY] from
 * [originX], [originY]. Writes back in place and re-normalizes.
 */
fun steerToward(
    directions: FloatArray,
    i2: Int,
    originX: Float,
    originY: Float,
    targetX: Float,
    targetY: Float,
    strength: Float,
    deltaTime: Float,
) {
    val dx = targetX - originX
    val dy = targetY - originY
    val mag = sqrt(dx * dx + dy * dy)
    if (mag <= 0f) return
    val toX = dx / mag
    val toY = dy / mag
    val t = (strength * deltaTime).coerceAtMost(1f)
    directions[i2] += (toX - directions[i2]) * t
    directions[i2 + 1] += (toY - directions[i2 + 1]) * t
    val dMag = sqrt(directions[i2] * directions[i2] + directions[i2 + 1] * directions[i2 + 1])
    if (dMag > 0f) {
        directions[i2] /= dMag
        directions[i2 + 1] /= dMag
    }
}

/**
 * Steers [direction] toward [target] from [origin] with the given [strength] and [deltaTime].
 * Modifies [direction] in place and re-normalizes it. Avoids allocations for use in hot paths.
 */
fun steerToward(direction: Vec2, origin: Vec2, target: Vec2, strength: Float, deltaTime: Float) {
    val dx = target.x - origin.x
    val dy = target.y - origin.y
    val mag = sqrt(dx * dx + dy * dy)
    if (mag <= 0f) return
    val toX = dx / mag
    val toY = dy / mag
    val t = (strength * deltaTime).coerceAtMost(1f)
    direction.x += (toX - direction.x) * t
    direction.y += (toY - direction.y) * t
    val dMag = sqrt(direction.x * direction.x + direction.y * direction.y)
    if (dMag > 0f) {
        direction.x /= dMag
        direction.y /= dMag
    }
}
