package com.glycin.koita

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.glycin.koita.composables.GameOverScreen
import com.glycin.koita.composables.GameScreen
import com.glycin.koita.composables.GameWonScreen
import com.glycin.koita.composables.HighscoresScreen
import com.glycin.koita.composables.HowToPlayScreen
import com.glycin.koita.composables.MainMenu
import com.glycin.koita.composables.OptionsScreen
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen

@Composable
fun App() {
    val gameState = remember { GameState() }

    when (gameState.currentScreen) {
        Screen.MAIN_MENU -> MainMenu(gameState)
        Screen.HOW_TO_PLAY -> HowToPlayScreen(gameState)
        Screen.OPTIONS -> OptionsScreen(gameState)
        Screen.HIGHSCORES -> HighscoresScreen(gameState)
        Screen.GAME -> GameScreen(gameState)
        Screen.GAME_OVER -> GameOverScreen(gameState)
        Screen.GAME_WON -> GameWonScreen(gameState)
    }
}
