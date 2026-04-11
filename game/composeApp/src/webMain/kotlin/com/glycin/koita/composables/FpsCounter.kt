package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive

private val FPS_TEXT_COLOR = Color(0xFF00FF00)

@Composable
fun BoxScope.FpsCounter(modifier: Modifier = Modifier) {
    var fps by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var frameCount = 0
        var prevTimeNanos = withFrameNanos { it }

        while (isActive) {
            withFrameNanos { currentTimeNanos ->
                frameCount++
                val elapsedSeconds = (currentTimeNanos - prevTimeNanos) / 1_000_000_000.0

                if (elapsedSeconds >= 1.0) {
                    fps = (frameCount / elapsedSeconds).toInt()
                    prevTimeNanos = currentTimeNanos
                    frameCount = 0
                }
            }
        }
    }

    Text(
        text = "FPS: $fps",
        color = FPS_TEXT_COLOR,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.LightGray.copy(alpha = 0.8f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}