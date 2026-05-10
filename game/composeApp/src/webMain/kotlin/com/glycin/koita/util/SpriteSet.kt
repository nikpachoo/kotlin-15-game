package com.glycin.koita.util

import org.jetbrains.compose.resources.DrawableResource

class SpriteSet {
    private val counts = mutableMapOf<DrawableResource, Int>()

    val distinct: Set<DrawableResource> get() = counts.keys

    fun add(sprite: DrawableResource) {
        counts[sprite] = (counts[sprite] ?: 0) + 1
    }

    fun remove(sprite: DrawableResource) {
        val c = counts[sprite] ?: return
        if (c <= 1) counts.remove(sprite) else counts[sprite] = c - 1
    }

    fun clear() {
        counts.clear()
    }
}
