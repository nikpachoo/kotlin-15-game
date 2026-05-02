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
                fontSize = 48.sp,
                color = titleColor,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                fontFamily = pixelFont(),
                fontSize = 14.sp,
                color = Color.LightGray,
            )

            if (extraNote != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = extraNote,
                    fontFamily = pixelFont(),
                    fontSize = 14.sp,
                    color = MenuColors.SUCCESS_TEXT,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            HighscoreSubmission(
                score = gameState.score,
                onSubmitted = { gameState.endRunAndGoTo(Screen.HIGHSCORES) },
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { gameState.endRunAndGoTo(Screen.MAIN_MENU) },
                modifier = Modifier.width(160.dp).height(44.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Main Menu",
                    fontFamily = pixelFont(),
                    fontSize = 16.sp,
                )
            }
        }
    }
}
