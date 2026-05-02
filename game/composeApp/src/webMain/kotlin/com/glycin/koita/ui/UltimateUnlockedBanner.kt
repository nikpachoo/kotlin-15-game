package com.glycin.koita.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private const val SLAM_IN_MS = 220
private const val IMPACT_FLASH_FADE_MS = 60
private const val IMPACT_FLASH_HOLD_MS = 220
private const val SHOCKWAVE_MS = 480
private const val BOUNCE_UP_MS = 130
private const val BOUNCE_SETTLE_MS = 200
private const val HOLD_MS = 1700L
private const val EXIT_MS = 420
private const val PULSE_HALF_MS = 450

private val BANNER_WIDTH = 460.dp
private val BANNER_HEIGHT = 110.dp
private val SHOCKWAVE_BOX_WIDTH = 1100.dp
private val SHOCKWAVE_BOX_HEIGHT = 440.dp
private const val SKEW_PX = 28f
private const val OFFSCREEN_X_DP = 900f
private const val TEXT_OUTLINE_WIDTH_PX = 4f

@Composable
fun UltimateUnlockedBanner(
    text: String?,
    modifier: Modifier = Modifier,
) {
    val slideIn = remember { Animatable(0f) }
    val bounce = remember { Animatable(0f) }
    val flash = remember { Animatable(0f) }
    val shockwave = remember { Animatable(0f) }
    val slideOut = remember { Animatable(0f) }
    val pulse = remember { Animatable(0f) }
    var displayText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        if (text == null) return@LaunchedEffect
        displayText = text
        slideIn.snapTo(0f)
        bounce.snapTo(0f)
        flash.snapTo(0f)
        shockwave.snapTo(0f)
        slideOut.snapTo(0f)
        pulse.snapTo(0f)

        slideIn.animateTo(1f, tween(SLAM_IN_MS, easing = EaseOutCubic))

        launch {
            flash.animateTo(1f, tween(IMPACT_FLASH_FADE_MS))
            flash.animateTo(0f, tween(IMPACT_FLASH_HOLD_MS))
        }
        launch {
            shockwave.animateTo(1f, tween(SHOCKWAVE_MS, easing = EaseOutCubic))
        }
        bounce.animateTo(1f, tween(BOUNCE_UP_MS, easing = EaseOutBack))
        bounce.animateTo(0f, tween(BOUNCE_SETTLE_MS))

        val pulseJob = launch {
            while (true) {
                pulse.animateTo(1f, tween(PULSE_HALF_MS))
                pulse.animateTo(0f, tween(PULSE_HALF_MS))
            }
        }

        delay(HOLD_MS)
        slideOut.animateTo(1f, tween(EXIT_MS, easing = EaseInCubic))
        pulseJob.cancel()
    }

    if (displayText.isEmpty() || slideOut.value >= 1f) return

    val translateX = ((1f - slideIn.value) * -OFFSCREEN_X_DP) + (slideOut.value * OFFSCREEN_X_DP)
    val baseScale = 1f + bounce.value * 0.18f + sin(pulse.value * PI.toFloat()) * 0.015f
    val groupAlpha = (1f - slideOut.value).coerceIn(0f, 1f)
    val showShockwave = shockwave.value > 0f && shockwave.value < 1f

    val parallelogramPath = remember { Path() }
    val stripePath = remember { Path() }
    val bannerBrush = remember {
        Brush.verticalGradient(
            0f to HudColors.BANNER_GOLD_TOP,
            0.55f to HudColors.BANNER_GOLD_MID,
            1f to HudColors.BANNER_GOLD_BOTTOM,
        )
    }
    val displayUpper = remember(displayText) { displayText.uppercase() }

    Box(modifier = modifier.fillMaxSize()) {
        if (showShockwave) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = SHOCKWAVE_BOX_WIDTH, height = SHOCKWAVE_BOX_HEIGHT)
                    .alpha(groupAlpha),
            ) {
                drawShockwave(size, shockwave.value)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = BANNER_WIDTH, height = BANNER_HEIGHT)
                .offset(x = translateX.dp)
                .scale(baseScale)
                .alpha(groupAlpha),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawBanner(size, parallelogramPath, stripePath, bannerBrush)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ULTIMATE UNLOCKED!",
                    fontFamily = pixelFont(),
                    fontSize = 14.sp,
                    color = HudColors.BANNER_LABEL,
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedBannerText(displayUpper, 30.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedBannerText(
                    value = "PRESS R",
                    fontSize = 18.sp,
                    color = HudColors.PANEL_ACCENT,
                    strokeWidth = 3f,
                    modifier = Modifier.alpha(0.55f + pulse.value * 0.45f),
                )
            }

            if (flash.value > 0f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(color = Color.White.copy(alpha = flash.value))
                }
            }
        }
    }
}

@Composable
private fun OutlinedBannerText(
    value: String,
    fontSize: TextUnit,
    color: Color = Color.White,
    strokeWidth: Float = TEXT_OUTLINE_WIDTH_PX,
    modifier: Modifier = Modifier,
) {
    val font = pixelFont()
    val outlineStyle = remember(fontSize, strokeWidth) {
        TextStyle(
            fontFamily = font,
            fontSize = fontSize,
            color = HudColors.BANNER_TEXT_SHADOW,
            drawStyle = Stroke(width = strokeWidth),
        )
    }
    val fillStyle = remember(fontSize, color) {
        TextStyle(
            fontFamily = font,
            fontSize = fontSize,
            color = color,
        )
    }
    Box(modifier = modifier) {
        Text(text = value, style = outlineStyle)
        Text(text = value, style = fillStyle)
    }
}

private fun DrawScope.drawShockwave(size: Size, t: Float) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    val maxRadius = size.minDimension / 2f
    val r = maxRadius * t
    val ringAlpha = (1f - t).coerceIn(0f, 1f)
    drawCircle(
        color = HudColors.BANNER_LABEL.copy(alpha = ringAlpha * 0.85f),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = 6f),
    )
    drawCircle(
        color = HudColors.BANNER_LABEL.copy(alpha = ringAlpha * 0.35f),
        radius = r * 0.85f,
        center = Offset(cx, cy),
        style = Stroke(width = 14f),
    )
}

private fun DrawScope.drawBanner(
    size: Size,
    parallelogram: Path,
    stripe: Path,
    brush: Brush,
) {
    val w = size.width
    val h = size.height
    parallelogram.rewind()
    parallelogram.moveTo(SKEW_PX, 0f)
    parallelogram.lineTo(w, 0f)
    parallelogram.lineTo(w - SKEW_PX, h)
    parallelogram.lineTo(0f, h)
    parallelogram.close()

    drawPath(path = parallelogram, brush = brush)

    val stripeStep = 22f
    var x = -h
    while (x < w + h) {
        stripe.rewind()
        stripe.moveTo(x, 0f)
        stripe.lineTo(x + 8f, 0f)
        stripe.lineTo(x + 8f - h, h)
        stripe.lineTo(x - h, h)
        stripe.close()
        drawPath(path = stripe, color = HudColors.BANNER_STRIPE)
        x += stripeStep
    }

    drawPath(path = parallelogram, color = HudColors.BANNER_GLOW, style = Stroke(width = 6f))
    drawPath(path = parallelogram, color = HudColors.BANNER_OUTLINE, style = Stroke(width = 2f))
}
