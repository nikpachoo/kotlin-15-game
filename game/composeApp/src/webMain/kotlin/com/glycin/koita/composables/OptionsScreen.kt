package com.glycin.koita.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
                fontSize = 36.sp,
                color = Color.White,
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

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
                modifier = Modifier.width(160.dp).height(44.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Back",
                    fontFamily = pixelFont(),
                    fontSize = 16.sp,
                )
            }
        }
    }
}
