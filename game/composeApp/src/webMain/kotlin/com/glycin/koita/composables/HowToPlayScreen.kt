package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun HowToPlayScreen(gameState: GameState) {
    val compact = isCompact()

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
                fontSize = if (compact) 22.sp else 36.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(if (compact) 12.dp else 32.dp))

            if (compact) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        ControlsBlock(compact = true)
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        TipsBlock(compact = true)
                    }
                }
            } else {
                ControlsBlock(compact = false)
                Spacer(modifier = Modifier.height(24.dp))
                TipsBlock(compact = false)
            }

            Spacer(modifier = Modifier.height(if (compact) 16.dp else 32.dp))

            MenuOutlinedButton(
                text = "Back",
                onClick = { gameState.currentScreen = Screen.MAIN_MENU },
            )
        }
    }
}

@Composable
private fun ControlsBlock(compact: Boolean) {
    SectionTitle("Controls", compact)
    ControlLine("WASD / Arrows", "Move", compact)
    ControlLine("Space", "Jump", compact)
    ControlLine("Left Click", "Use weapon", compact)
    ControlLine("Right Click", "Build / Place", compact)
    ControlLine("1 - 3", "Select weapon", compact)
    ControlLine("ESC", "Pause", compact)
}

@Composable
private fun TipsBlock(compact: Boolean) {
    SectionTitle("Tips", compact)
    TipLine("Mine blocks for resources", compact)
    TipLine("Stand on a shrine for 3s to activate it", compact)
    TipLine("Pick 1 of 3 upgrade orbs per shrine", compact)
    TipLine("Unlock double jump & jetpack for mobility", compact)
    TipLine("Watch out for golems, bats, and slimes!", compact)
}

@Composable
private fun SectionTitle(text: String, compact: Boolean) {
    Text(
        text = text,
        fontFamily = pixelFont(),
        fontSize = if (compact) 14.sp else 20.sp,
        color = MenuColors.SECTION_TITLE,
    )
    Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))
}

@Composable
private fun ControlLine(key: String, action: String, compact: Boolean) {
    Text(
        text = "$key  -  $action",
        fontFamily = pixelFont(),
        fontSize = if (compact) 11.sp else 14.sp,
        color = Color.LightGray,
    )
    Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
}

@Composable
private fun TipLine(text: String, compact: Boolean) {
    Text(
        text = "- $text",
        fontFamily = pixelFont(),
        fontSize = if (compact) 11.sp else 14.sp,
        color = Color.LightGray,
    )
    Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
}
