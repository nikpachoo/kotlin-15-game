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
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui.pixelFont

@Composable
fun GameOverScreen(gameState: GameState) {
    EndScreen(
        gameState = gameState,
        title = "GAME OVER",
        titleColor = MenuColors.GAME_OVER_TITLE,
        subtitle = "The void claimed you...",
    )
}

@Composable
fun GameWonScreen(gameState: GameState) {
    EndScreen(
        gameState = gameState,
        title = "VICTORY",
        titleColor = MenuColors.VICTORY_TITLE,
        subtitle = "The void has been silenced.",
        extraNote = "Congratulations, your score has been doubled!",
    )
}

@Composable
private fun EndScreen(
    gameState: GameState,
    title: String,
    titleColor: Color,
    subtitle: String,
    extraNote: String? = null,
) {
    val compact = isCompact()
    val subtitleSize = if (compact) 11.sp else 14.sp

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
                text = title,
                fontFamily = pixelFont(),
                fontSize = if (compact) 28.sp else 48.sp,
                color = titleColor,
            )

            Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))

            Text(
                text = subtitle,
                fontFamily = pixelFont(),
                fontSize = subtitleSize,
                color = Color.LightGray,
            )

            if (extraNote != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = extraNote,
                    fontFamily = pixelFont(),
                    fontSize = subtitleSize,
                    color = MenuColors.SUCCESS_TEXT,
                )
            }

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 32.dp))

            HighscoreSubmission(
                score = gameState.score,
                onSubmitted = { response ->
                    gameState.pendingHighscoresResponse = response
                    gameState.endRunAndGoTo(Screen.HIGHSCORES)
                },
            )

            Spacer(modifier = Modifier.height(if (compact) 10.dp else 24.dp))

            MenuOutlinedButton(
                text = "Main Menu",
                onClick = { gameState.endRunAndGoTo(Screen.MAIN_MENU) },
            )
        }
    }
}
