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
    DEAD,
}

enum class PlayerFacing {
    LEFT,
    RIGHT,
}