package com.glycin.koita.world

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.glycin.koita.algorithms.SimplexNoise
import com.glycin.koita.core.Camera
import com.glycin.koita.util.lerp
import kotlin.random.Random

class ParallaxBackground(
    seed: Long = Random.nextLong(),
    private val camera: Camera,
) {
    private val noise = SimplexNoise(seed)
    private val detailNoise = SimplexNoise(seed + 1)
    private val scrollSpeed = 0.85f
    private val tileSize = WorldConstants.TILE_SIZE * 4
    private val backgroundChunkSize = 16
    private val backgroundChunkPixelSize = backgroundChunkSize * tileSize
    private val chunks = mutableMapOf<Pair<Int, Int>, ImageBitmap>()

    fun generateChunk(chunkX: Int, chunkY: Int): ImageBitmap {
        val key = Pair(chunkX, chunkY)
        if (chunks.containsKey(key)) {
            return chunks[key]!!
        }

        val bitmap = ImageBitmap(backgroundChunkPixelSize, backgroundChunkPixelSize)
        val canvas = Canvas(bitmap)

        val worldHeightPixels = WorldConstants.WORLD_HEIGHT_PIXELS

        for (localTileY in 0 until backgroundChunkSize) {
            for (localTileX in 0 until backgroundChunkSize) {
                val tileX = chunkX * backgroundChunkSize + localTileX
                val tileY = chunkY * backgroundChunkSize + localTileY

                val worldY = tileY * tileSize.toFloat()

                val depth = when {
                    worldY < 0 -> 0.0f
                    worldY > worldHeightPixels -> 1.0f
                    else -> worldY / worldHeightPixels
                }

                val x = localTileX * tileSize.toFloat()
                val y = localTileY * tileSize.toFloat()

                val tileColor = getTileColorWithDetails(depth, tileX, tileY)
                val paint = Paint().apply {
                    color = tileColor
                }

                canvas.drawRect(
                    Rect(x, y, x + tileSize + 1f, y + tileSize + 1f),
                    paint
                )
            }
        }

        chunks[key] = bitmap
        return bitmap
    }

    fun render(
        drawScope: DrawScope,
    ) {
        with(drawScope) {
            val parallaxOffsetX = camera.position.x
            val parallaxOffsetY = camera.position.y * scrollSpeed

            val viewWidth = camera.canvasWidth
            val viewHeight = camera.actualHeight / camera.scale
            val startChunkX = ((parallaxOffsetX - viewWidth / 2) / backgroundChunkPixelSize).toInt() - 1
            val endChunkX = ((parallaxOffsetX + viewWidth / 2) / backgroundChunkPixelSize).toInt() + 1
            val startChunkY = ((parallaxOffsetY - viewHeight / 2) / backgroundChunkPixelSize).toInt() - 1
            val endChunkY = ((parallaxOffsetY + viewHeight / 2) / backgroundChunkPixelSize).toInt() + 1
            val worldMaxChunkX = (WorldConstants.WORLD_WIDTH_TILES * WorldConstants.TILE_SIZE / backgroundChunkPixelSize)
            val worldMaxChunkY = (WorldConstants.WORLD_HEIGHT_TILES * WorldConstants.TILE_SIZE / backgroundChunkPixelSize)
            val marginY = (viewHeight - camera.canvasHeight) / 2f
            val halfCanvasWidth = camera.canvasWidth / 2
            val screenYBase = camera.canvasHeight / 2 - marginY - parallaxOffsetY

            for (chunkY in startChunkY..endChunkY) {
                if (chunkY < -1 || chunkY > worldMaxChunkY + 1) continue

                for (chunkX in startChunkX..endChunkX) {
                    if (chunkX < 0 || chunkX >= worldMaxChunkX) continue

                    val chunkBitmap = generateChunk(chunkX, chunkY)

                    val chunkWorldX = chunkX * backgroundChunkPixelSize.toFloat()
                    val chunkWorldY = chunkY * backgroundChunkPixelSize.toFloat()

                    val screenX = chunkWorldX - parallaxOffsetX + halfCanvasWidth
                    val screenY = chunkWorldY + screenYBase

                    drawImage(
                        image = chunkBitmap,
                        topLeft = Offset(screenX, screenY),
                        alpha = 1.0f,
                    )
                }
            }
        }
    }

    private fun getTileColorWithDetails(depth: Float, tileX: Int, tileY: Int): Color {
        val baseNoiseValue = noise.noise(tileX / 10f, tileY / 10f) * 0.15f + 0.85f
        val detailValue = detailNoise.noise(tileX / 5f, tileY / 5f)
        val baseColor = interpolateDepthColor(depth)

        val detailMultiplier = when {
            // Deep caves
            depth > 0.80f && detailValue > 0.6f -> BackgroundColors.DEEP_CAVE_BRIGHT
            depth > 0.80f && detailValue > 0.3f -> BackgroundColors.DEEP_CAVE_WARM

            // Mid-deep
            depth > 0.60f && detailValue > 0.7f -> BackgroundColors.MID_DEEP_BRIGHT
            depth > 0.60f && detailValue > 0.4f -> BackgroundColors.MID_DEEP_DIM

            // Mid caves
            depth > 0.40f && detailValue > 0.65f -> BackgroundColors.MID_CAVE_COOL

            // Upper caves
            depth > 0.20f && detailValue > 0.6f -> BackgroundColors.UPPER_CAVE_GREEN

            // Surface
            depth <= 0.20f && detailValue > 0.5f -> BackgroundColors.SURFACE_LIGHT
            depth <= 0.20f && detailValue < -0.2f -> BackgroundColors.SURFACE_SHADOW

            else -> BackgroundColors.NEUTRAL
        }

        return Color(
            red = (baseColor.red * baseNoiseValue * detailMultiplier.red).coerceIn(0f, 1f),
            green = (baseColor.green * baseNoiseValue * detailMultiplier.green).coerceIn(0f, 1f),
            blue = (baseColor.blue * baseNoiseValue * detailMultiplier.blue).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    private fun interpolateDepthColor(depth: Float): Color {
        val stops = listOf(
            0.00f to BackgroundColors.DEPTH_0,
            0.20f to BackgroundColors.DEPTH_20,
            0.40f to BackgroundColors.DEPTH_40,
            0.60f to BackgroundColors.DEPTH_60,
            0.80f to BackgroundColors.DEPTH_80,
            1.00f to BackgroundColors.DEPTH_100,
        )

        for (i in 0 until stops.size - 1) {
            val (depth1, color1) = stops[i]
            val (depth2, color2) = stops[i + 1]

            if (depth in depth1..depth2) {
                val t = ((depth - depth1) / (depth2 - depth1)).coerceIn(0f, 1f)

                return Color(
                    red = color1.red.lerp(color2.red, t),
                    green = color1.green.lerp(color2.green, t),
                    blue = color1.blue.lerp(color2.blue, t)
                )
            }
        }

        return stops.last().second
    }
}