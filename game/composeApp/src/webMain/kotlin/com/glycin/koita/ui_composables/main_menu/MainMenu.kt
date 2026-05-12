package com.glycin.koita.ui_composables.main_menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.MenuHeader
import com.glycin.koita.ui_composables.SidebarMenuItem
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.isCompact
import com.glycin.koita.ui_composables.rememberMenuBackgroundBrush
import com.glycin.koita.util.requestBrowserFullscreen

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
        MenuHeader(
            title = "Game",
            modifier = Modifier.fillMaxWidth(),
        )

        MainMenuItem("Start") {
            if (compact) requestBrowserFullscreen()
            gameState.currentScreen = Screen.GAME
        }
        MainMenuItem("Modifiers") { gameState.currentScreen = Screen.MODIFIERS }
        MainMenuItem("How to Play") {
            if (compact) requestBrowserFullscreen()
            gameState.currentScreen = Screen.TUTORIAL
        }
        MainMenuItem("Options") { gameState.currentScreen = Screen.OPTIONS }
        MainMenuItem("Highscores") { gameState.currentScreen = Screen.HIGHSCORES }
        MainMenuItem("Atlas") { gameState.currentScreen = Screen.ATLAS }

        Spacer(modifier = Modifier.size(compactOr(16.dp, 32.dp)))
    }
}

@Composable
private fun MainMenuItem(text: String, onClick: () -> Unit) {
    SidebarMenuItem(
        text = text,
        fontSize = compactOr(18.sp, 32.sp),
        verticalPadding = compactOr(8.dp, 12.dp),
        onClick = onClick,
    )
}
