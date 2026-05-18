package com.glycin.koita

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.glycin.koita.ui_composables.atlas.AtlasScreen
import com.glycin.koita.ui_composables.main_menu.GameOverScreen
import com.glycin.koita.composables.GameScreen
import com.glycin.koita.ui_composables.main_menu.GameWonScreen
import com.glycin.koita.ui_composables.main_menu.HighscoresScreen
import com.glycin.koita.ui_composables.LocalIsPortrait
import com.glycin.koita.ui_composables.LocalLayoutMode
import com.glycin.koita.ui_composables.main_menu.MainMenu
import com.glycin.koita.ui_composables.main_menu.ModifierScreen
import com.glycin.koita.ui_composables.main_menu.OptionsScreen
import com.glycin.koita.ui_composables.isPortraitFor
import com.glycin.koita.ui_composables.layoutModeFor
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.gameplay.tutorial.TutorialScreen

@Composable
fun App() {
    val gameState = remember { GameState() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val mode = layoutModeFor(maxWidth, maxHeight)
        val portrait = isPortraitFor(maxWidth, maxHeight)
        CompositionLocalProvider(
            LocalLayoutMode provides mode,
            LocalIsPortrait provides portrait,
        ) {
            when (gameState.currentScreen) {
                Screen.MAIN_MENU -> MainMenu(gameState)
                Screen.TUTORIAL -> TutorialScreen(gameState)
                Screen.OPTIONS -> OptionsScreen(gameState)
                Screen.HIGHSCORES -> HighscoresScreen(gameState)
                Screen.ATLAS -> AtlasScreen(gameState)
                Screen.MODIFIERS -> ModifierScreen(gameState)
                Screen.GAME -> GameScreen(gameState)
                Screen.GAME_OVER -> GameOverScreen(gameState)
                Screen.GAME_WON -> GameWonScreen(gameState)
            }
        }
    }
}
