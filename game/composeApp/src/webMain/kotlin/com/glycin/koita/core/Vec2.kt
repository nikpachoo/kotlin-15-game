package com.glycin.koita.core

import kotlin.math.sqrt

data class Vec2(
    var x: Float = 0f,
    var y: Float = 0f,
) {
    companion object {
        fun zero() = Vec2(0f, 0f)
        fun one() = Vec2(1f, 1f)
        fun up() = Vec2(0f, -1f)
        fun down() = Vec2(0f, 1f)
        fun left() = Vec2(-1f, 0f)
        fun right() = Vec2(1f, 0f)

        fun distance(a: Vec2, b: Vec2): Float {
            val dx = b.x - a.x
            val dy = b.y - a.y
            return sqrt(dx * dx + dy * dy)
        }

        fun fastDistance(a: Vec2, b: Vec2): Float {
            val dx = b.x - a.x
            val dy = b.y - a.y
            return dx * dx + dy * dy
        }

        fun fastDistance(ax: Float, ay: Float, bx: Float, by: Float): Float {
            val dx = bx - ax
            val dy = by - ay
            return dx * dx + dy * dy
        }
    }

    operator fun plus(other: Vec2) = Vec2(x + other.x, y + other.y)
    operator fun minus(other: Vec2) = Vec2(x - other.x, y - other.y)
    operator fun times(scalar: Int) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Int) = Vec2(x / scalar, y / scalar)

    operator fun plus(other: Float) = Vec2(x + other, y + other)
    operator fun minus(other: Float) = Vec2(x - other, y - other)
    operator fun times(scalar: Float) = Vec2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vec2(x / scalar, y / scalar)

    operator fun unaryMinus() = Vec2(-x, -y)

    fun dot(other: Vec2) = x * other.x + y * other.y

    fun magnitude(): Float = sqrt(x * x + y * y)

    fun normalized(): Vec2 {
        val mag = magnitude()
        return if (mag != 0f) this / mag else zero()
    }
}