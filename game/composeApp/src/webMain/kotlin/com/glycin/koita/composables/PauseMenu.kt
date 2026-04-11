package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.ui.pixelFont

internal val sliderColors
    @Composable get() = SliderDefaults.colors(
        thumbColor = Color.White,
        activeTrackColor = Color.White,
        inactiveTrackColor = Color.Gray,
    )

@Composable
fun PauseMenu(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.PAUSE_OVERLAY),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "PAUSED",
                fontFamily = pixelFont(),
                fontSize = 48.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Press ESC to resume",
                fontFamily = pixelFont(),
                fontSize = 16.sp,
                color = Color.LightGray,
            )

            Spacer(modifier = Modifier.height(32.dp))

            VolumeSlider(
                label = "Music",
                value = gameState.musicVolume,
                onValueChange = { newVolume ->
                    gameState.musicVolume = newVolume
                    SoundManager.musicVolume = newVolume
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            VolumeSlider(
                label = "SFX",
                value = gameState.sfxVolume,
                onValueChange = { newVolume ->
                    gameState.sfxVolume = newVolume
                    SoundManager.sfxVolume = newVolume
                },
            )
        }
    }
}

@Composable
internal fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(60.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier.width(200.dp),
            colors = sliderColors,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${(value * 100).toInt()}%",
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.width(48.dp),
        )
    }
}
