package com.glycin.koita.core

enum class PlayerState {
    IDLE,
    WALKING,
    JUMPING,
    HURT,
    ATTACKING,
    FALLING,
    BOOST,
    IMMUTABLE,
}

enum class PlayerFacing {
    LEFT,
    RIGHT,
}