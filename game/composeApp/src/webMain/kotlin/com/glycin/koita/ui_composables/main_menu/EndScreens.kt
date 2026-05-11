package com.glycin.koita.ui_composables.main_menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.MenuHeader
import com.glycin.koita.ui_composables.MenuOutlinedButton
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.ui_composables.rememberMenuBackgroundBrush

@Composable
fun GameOverScreen(gameState: GameState) {
    EndScreen(
        gameState = gameState,
        title = "GAME OVER",
        titleColor = Color.White,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MenuHeader(
            title = title,
            titleColor = titleColor,
            modifier = Modifier
                .width(compactOr(360.dp, 640.dp))
                .background(MenuColors.SIDEBAR),
            horizontalArrangement = Arrangement.spacedBy(
                space = compactOr(16.dp, 24.dp),
                alignment = Alignment.CenterHorizontally,
            ),
        )

        Spacer(modifier = Modifier.height(compactOr(16.dp, 28.dp)))

        EndScreenCard(
            gameState = gameState,
            subtitle = subtitle,
            extraNote = extraNote,
            width = compactOr(320.dp, 480.dp),
        )
    }
}

@Composable
private fun EndScreenCard(
    gameState: GameState,
    subtitle: String,
    extraNote: String?,
    width: Dp,
) {
    Column(
        modifier = Modifier
            .width(width)
            .background(MenuColors.SIDEBAR)
            .padding(
                horizontal = compactOr(20.dp, 36.dp),
                vertical = compactOr(16.dp, 28.dp),
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CardSubtitle(text = subtitle)

        if (extraNote != null) {
            Spacer(modifier = Modifier.height(4.dp))
            CardSubtitle(text = extraNote)
        }

        Spacer(modifier = Modifier.height(compactOr(12.dp, 24.dp)))

        HighscoreSubmission(
            score = gameState.score,
            onSubmitted = { response ->
                gameState.pendingHighscoresResponse = response
                gameState.endRunAndGoTo(Screen.HIGHSCORES)
            },
        )

        Spacer(modifier = Modifier.height(compactOr(10.dp, 20.dp)))

        MenuOutlinedButton(
            text = "Main Menu",
            onClick = { gameState.endRunAndGoTo(Screen.MAIN_MENU) },
        )
    }
}

@Composable
private fun CardSubtitle(text: String) {
    Text(
        text = text,
        fontFamily = pixelFont(),
        fontSize = compactOr(11.sp, 14.sp),
        color = Color.White,
    )
}
