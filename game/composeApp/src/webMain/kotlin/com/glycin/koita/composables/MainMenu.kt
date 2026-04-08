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

//TODO: Extract colors to central object
@Composable
fun MainMenu(gameState: GameState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "KOITA",
                fontFamily = pixelFont(),
                fontSize = 72.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(48.dp))

            MenuButton("Start") { gameState.currentScreen = Screen.GAME }

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton("How to Play") { gameState.currentScreen = Screen.HOW_TO_PLAY }

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton("Options") { gameState.currentScreen = Screen.OPTIONS }

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton("Highscores") { gameState.currentScreen = Screen.HIGHSCORES }
        }
    }
}

@Composable
private fun MenuButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.width(220.dp).height(48.dp),
        border = BorderStroke(2.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = 18.sp,
        )
    }
}
