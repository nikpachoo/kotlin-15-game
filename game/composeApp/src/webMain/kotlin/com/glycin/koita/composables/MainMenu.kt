package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui.pixelFont

@Composable
fun MainMenu(gameState: GameState) {
    val backgroundBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                MenuColors.MAIN_BACKGROUND_DARK,
                MenuColors.MAIN_BACKGROUND_MID,
                MenuColors.MAIN_BACKGROUND_LIGHT,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GameBanner()

            Spacer(modifier = Modifier.size(28.dp))

            Column(
                modifier = Modifier
                    .width(480.dp)
                    .background(MenuColors.SIDEBAR)
                    .padding(horizontal = 48.dp, vertical = 42.dp),
            ) {
                KotlinLogo(modifier = Modifier.align(Alignment.CenterHorizontally))

                Spacer(modifier = Modifier.size(36.dp))

                MenuItem("Start") { gameState.currentScreen = Screen.GAME }
                MenuItem("How to Play") { gameState.currentScreen = Screen.TUTORIAL }
                MenuItem("Options") { gameState.currentScreen = Screen.OPTIONS }
                MenuItem("Highscores") { gameState.currentScreen = Screen.HIGHSCORES }
            }
        }
    }
}

@Composable
private fun GameBanner() {
    Box(
        modifier = Modifier
            .background(MenuColors.MAIN_BACKGROUND_DARK)
            .border(width = 4.dp, color = MenuColors.SIDEBAR)
            .padding(horizontal = 56.dp, vertical = 20.dp),
    ) {
        Text(
            text = "KGame",
            fontFamily = pixelFont(),
            fontSize = 80.sp,
            color = Color.White,
        )
    }
}

@Composable
private fun KotlinLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(96.dp)
            .drawWithCache {
                val logoPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height / 2f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                onDrawBehind { drawPath(logoPath, Color.White) }
            },
    )
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hovered) {
            Box(modifier = Modifier.size(20.dp).background(Color.Black))
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = 32.sp,
            color = Color.Black,
        )
    }
}
