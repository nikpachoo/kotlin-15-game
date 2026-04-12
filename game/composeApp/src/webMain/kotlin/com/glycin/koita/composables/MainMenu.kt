package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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

    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .background(MenuColors.SIDEBAR)
                .padding(horizontal = 32.dp, vertical = 28.dp),
        ) {
            KotlinLogo()

            Spacer(modifier = Modifier.weight(1f))

            MenuItem("Start") { gameState.currentScreen = Screen.GAME }
            MenuItem("How to Play") { gameState.currentScreen = Screen.HOW_TO_PLAY }
            MenuItem("Options") { gameState.currentScreen = Screen.OPTIONS }
            MenuItem("Highscores") { gameState.currentScreen = Screen.HIGHSCORES }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush),
        )
    }
}

@Composable
private fun KotlinLogo() {
    Box(
        modifier = Modifier
            .size(64.dp)
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hovered) {
            Box(modifier = Modifier.size(14.dp).background(Color.Black))
        } else {
            Spacer(modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = 22.sp,
            color = Color.Black,
        )
    }
}
