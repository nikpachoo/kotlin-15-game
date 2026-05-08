package com.glycin.koita.composables

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.util.requestBrowserFullscreen

private const val HOVER_ANIMATION_MS = 300

@Composable
fun MainMenu(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
        contentAlignment = Alignment.Center,
    ) {
        MainMenuPanel(gameState)
    }
}

@Composable
private fun MainMenuPanel(gameState: GameState) {
    val compact = isCompact()
    Column(
        modifier = Modifier
            .width(compactOr(320.dp, 480.dp))
            .background(MenuColors.SIDEBAR),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = compactOr(24.dp, 48.dp),
                    vertical = compactOr(20.dp, 36.dp),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
            Text(
                text = "Game",
                fontFamily = pixelFont(),
                fontSize = compactOr(28.sp, 56.sp),
                color = Color.White,
            )
        }

        MenuItem("Start") {
            if (compact) requestBrowserFullscreen()
            gameState.currentScreen = Screen.GAME
        }
        MenuItem("How to Play") {
            if (compact) requestBrowserFullscreen()
            gameState.currentScreen = Screen.TUTORIAL
        }
        MenuItem("Options") { gameState.currentScreen = Screen.OPTIONS }
        MenuItem("Highscores") { gameState.currentScreen = Screen.HIGHSCORES }

        Spacer(modifier = Modifier.size(compactOr(16.dp, 32.dp)))
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val active = hovered || focused
    val maxOffset = compactOr(12.dp, 20.dp)
    val hoverOffset by animateDpAsState(
        targetValue = if (active) maxOffset else 0.dp,
        animationSpec = tween(durationMillis = HOVER_ANIMATION_MS, easing = EaseOut),
    )
    val textColor by animateColorAsState(
        targetValue = if (active) Color.White else Color.Black,
        animationSpec = tween(durationMillis = HOVER_ANIMATION_MS, easing = EaseOut),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = compactOr(18.sp, 32.sp),
            color = textColor,
            modifier = Modifier
                .offset(x = hoverOffset)
                .padding(
                    horizontal = compactOr(24.dp, 48.dp),
                    vertical = compactOr(8.dp, 12.dp),
                ),
        )
    }
}
