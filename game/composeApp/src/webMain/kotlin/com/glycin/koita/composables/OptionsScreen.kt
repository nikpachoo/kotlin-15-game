package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.audio.SoundManager
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui.pixelFont

@Composable
fun OptionsScreen(gameState: GameState) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
    ) {
        val layout = menuPanelLayout()
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(layout.panelGap),
        ) {
            BackTab(
                boxSize = layout.backTabSize,
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
            )
            SettingsPanel(
                width = layout.panelWidth,
                gameState = gameState,
            )
        }
    }
}

@Composable
private fun SettingsPanel(width: Dp, gameState: GameState) {
    Column(
        modifier = Modifier
            .width(width)
            .background(MenuColors.SIDEBAR)
            .padding(
                horizontal = compactOr(24.dp, 48.dp),
                vertical = compactOr(20.dp, 36.dp),
            ),
        verticalArrangement = Arrangement.spacedBy(compactOr(24.dp, 40.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
            Text(
                text = "SETTINGS",
                fontFamily = pixelFont(),
                fontSize = compactOr(20.sp, 36.sp),
                color = Color.White,
            )
        }
        SettingsSlider(
            label = "Music",
            value = gameState.musicVolume,
            onValueChange = {
                gameState.musicVolume = it
                SoundManager.musicVolume = it
            },
        )
        SettingsSlider(
            label = "SFX",
            value = gameState.sfxVolume,
            onValueChange = {
                gameState.sfxVolume = it
                SoundManager.sfxVolume = it
            },
        )
    }
}

@Composable
private fun SettingsSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(compactOr(6.dp, 10.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                fontFamily = pixelFont(),
                fontSize = compactOr(14.sp, 20.sp),
                color = Color.White,
            )
            Text(
                text = "${(value * 100).toInt()}%",
                fontFamily = pixelFont(),
                fontSize = compactOr(14.sp, 20.sp),
                color = Color.White,
            )
        }
        SliderTrack(value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun SliderTrack(value: Float, onValueChange: (Float) -> Unit) {
    val trackHeight = compactOr(3.dp, 5.dp)
    val thumbWidth = compactOr(4.dp, 6.dp)
    val thumbHeight = compactOr(18.dp, 26.dp)

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(thumbHeight)
            .pointerInput(Unit) {
                val emit = { x: Float -> onValueChange((x / size.width).coerceIn(0f, 1f)) }
                detectTapGestures { offset -> emit(offset.x) }
            }
            .pointerInput(Unit) {
                val emit = { x: Float -> onValueChange((x / size.width).coerceIn(0f, 1f)) }
                detectDragGestures(
                    onDragStart = { offset -> emit(offset.x) },
                    onDrag = { change, _ ->
                        change.consume()
                        emit(change.position.x)
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .align(Alignment.Center)
                .background(MenuColors.SLIDER_TRACK),
        )
        Box(
            modifier = Modifier
                .width(maxWidth * value)
                .height(trackHeight)
                .align(Alignment.CenterStart)
                .background(Color.White),
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (maxWidth * value - thumbWidth / 2).coerceIn(0.dp, maxWidth - thumbWidth))
                .size(thumbWidth, thumbHeight)
                .background(Color.White),
        )
    }
}
