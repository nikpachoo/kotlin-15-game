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
            val viewHeight = camera.canvasHeight
            val startChunkX = ((parallaxOffsetX - viewWidth / 2) / backgroundChunkPixelSize).toInt() - 1
            val endChunkX = ((parallaxOffsetX + viewWidth / 2) / backgroundChunkPixelSize).toInt() + 1
            val startChunkY = ((parallaxOffsetY - viewHeight / 2) / backgroundChunkPixelSize).toInt() - 1
            val endChunkY = ((parallaxOffsetY + viewHeight / 2) / backgroundChunkPixelSize).toInt() + 1
            val worldMaxChunkX = (WorldConstants.WORLD_WIDTH_TILES * WorldConstants.TILE_SIZE / backgroundChunkPixelSize)
            val worldMaxChunkY = (WorldConstants.WORLD_HEIGHT_TILES * WorldConstants.TILE_SIZE / backgroundChunkPixelSize)

            for (chunkY in startChunkY..endChunkY) {
                if (chunkY < -1 || chunkY > worldMaxChunkY + 1) continue

                for (chunkX in startChunkX..endChunkX) {
                    if (chunkX < -1 || chunkX > worldMaxChunkX + 1) continue

                    val chunkBitmap = generateChunk(chunkX, chunkY)

                    val chunkWorldX = chunkX * backgroundChunkPixelSize.toFloat()
                    val chunkWorldY = chunkY * backgroundChunkPixelSize.toFloat()

                    val screenX = chunkWorldX - parallaxOffsetX + viewWidth / 2
                    val screenY = chunkWorldY - parallaxOffsetY + viewHeight / 2

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
            depth > 0.80f && detailValue > 0.6f -> {
                Color(1.3f, 1.1f, 0.9f)
            }
            depth > 0.80f && detailValue > 0.3f -> {
                Color(1.1f, 0.9f, 0.9f)
            }

            // Mid-deep
            depth > 0.60f && detailValue > 0.7f -> {
                Color(1.2f, 1.0f, 1.0f)
            }
            depth > 0.60f && detailValue > 0.4f -> {
                Color(0.9f, 0.9f, 0.9f)
            }

            // Mid caves
            depth > 0.40f && detailValue > 0.65f -> {
                Color(1.0f, 1.0f, 1.2f)
            }

            // Upper caves
            depth > 0.20f && detailValue > 0.6f -> {
                Color(0.9f, 1.1f, 1.0f)
            }

            // Surface
            depth <= 0.20f && detailValue > 0.5f -> {
                Color(0.85f, 1.0f, 0.95f)
            }
            depth <= 0.20f && detailValue < -0.2f -> {
                Color(0.8f, 0.8f, 0.8f)
            }

            else -> Color(1.0f, 1.0f, 1.0f)
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
            0.00f to Color(0xFF3a5555),
            0.20f to Color(0xFF4a4a55),
            0.40f to Color(0xFF4a4050),
            0.60f to Color(0xFF503a3a),
            0.80f to Color(0xFF553030),
            1.00f to Color(0xFF502020)
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