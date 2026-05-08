package com.glycin.koita.core

object PlayerSettings {
    const val BASE_SPEED = 200f
    const val BASE_JUMP_FORCE = 500f
    const val MAX_FALL_SPEED = 500f
    const val STEP_UP_HEIGHT = 4f

    // Jetpack
    const val JETPACK_MAX_FUEL = 1f
    const val JETPACK_FORCE = 600f

    // Dash
    const val DASH_SPEED = 800f
    const val DASH_DURATION = 0.2f
    const val DASH_COOLDOWN = 1.0f

    // Ground pound
    const val GROUND_POUND_SPEED = 1200f
    const val GROUND_POUND_RADIUS = 60f
    const val GROUND_POUND_DAMAGE = 3f
    const val GROUND_POUND_COOLDOWN = 0.5f
    const val GROUND_POUND_BOUNCE_FORCE = 700f

    // Hover
    const val HOVER_MAX_FUEL = 2.0f
    const val HOVER_GRAVITY_FACTOR = 0.1f
    const val HOVER_MAX_FALL_SPEED = 50f

    // Jump
    const val JUMP_PAD_MULTIPLIER = 2f

    const val MINING_BOOST_FORCE = 350f
    const val MINING_PULL_DURATION = 0.15f

    // Shrine activation lift
    const val SHRINE_LIFT_FORCE = 425f

    // Anchor (Immutability)
    const val ANCHOR_EXIT_DELAY = 0.3f

    // Lava
    const val LAVA_DAMAGE_INTERVAL = 1.0f
    const val LAVA_DAMAGE_THRESHOLD = 0.2f

    // Drowning / underwater
    const val DROWN_GRACE_PERIOD = 3.0f
    const val DROWN_DAMAGE_INTERVAL = 1.0f
    const val SUBMERGED_SPEED_MULTIPLIER = 0.5f
    const val SUBMERGED_GRAVITY_MULTIPLIER = 0.25f
    const val SUBMERGED_JUMP_MULTIPLIER = 0.5f

    // Heal
    const val HEAL_COST = 100
    const val HEAL_COOLDOWN_SECONDS = 2f

    // Invulnerability frames after taking damage
    const val INVULNERABILITY_DURATION = 0.25f
}
