package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui.pixelFont

@Composable
fun OptionsScreen(gameState: GameState) {
    val compact = isCompact()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuColors.BACKGROUND),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "OPTIONS",
                fontFamily = pixelFont(),
                fontSize = if (compact) 22.sp else 36.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 32.dp))

            VolumeSlider(
                label = "Music",
                value = gameState.musicVolume,
                onValueChange = { newVolume ->
                    gameState.musicVolume = newVolume
                    SoundManager.musicVolume = newVolume
                },
            )

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

            VolumeSlider(
                label = "SFX",
                value = gameState.sfxVolume,
                onValueChange = { newVolume ->
                    gameState.sfxVolume = newVolume
                    SoundManager.sfxVolume = newVolume
                },
            )

            Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

            DevModeToggle(
                value = gameState.devMode,
                onValueChange = { gameState.devMode = it },
            )

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 32.dp))

            MenuOutlinedButton(
                text = "Back",
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
            )
        }
    }
}
