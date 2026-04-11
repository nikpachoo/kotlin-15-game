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
fun HowToPlayScreen(gameState: GameState) {
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
                text = "HOW TO PLAY",
                fontFamily = pixelFont(),
                fontSize = 36.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(32.dp))

            SectionTitle("Controls")
            ControlLine("WASD / Arrows", "Move")
            ControlLine("Space", "Jump")
            ControlLine("Left Click", "Use weapon")
            ControlLine("Right Click", "Build / Place")
            ControlLine("1 - 3", "Select weapon")
            ControlLine("ESC", "Pause")

            Spacer(modifier = Modifier.height(24.dp))

            SectionTitle("Tips")
            TipLine("Mine blocks for resources")
            TipLine("Stand on a shrine for 3s to activate it")
            TipLine("Pick 1 of 3 upgrade orbs per shrine")
            TipLine("Unlock double jump & jetpack for mobility")
            TipLine("Watch out for golems, bats, and slimes!")

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedButton(
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
                modifier = Modifier.width(160.dp).height(44.dp),
                border = BorderStroke(2.dp, Color.White),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    text = "Back",
                    fontFamily = pixelFont(),
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = pixelFont(),
        fontSize = 20.sp,
        color = MenuColors.SECTION_TITLE,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ControlLine(key: String, action: String) {
    Text(
        text = "$key  -  $action",
        fontFamily = pixelFont(),
        fontSize = 14.sp,
        color = Color.LightGray,
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun TipLine(text: String) {
    Text(
        text = "- $text",
        fontFamily = pixelFont(),
        fontSize = 14.sp,
        color = Color.LightGray,
    )
    Spacer(modifier = Modifier.height(4.dp))
}
