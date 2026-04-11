package com.glycin.koita.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.rest.ApiClient
import com.glycin.koita.rest.HighscoreEntry
import com.glycin.koita.ui.pixelFont

@Composable
fun HighscoresScreen(gameState: GameState) {
    var highscores by remember { mutableStateOf<List<HighscoreEntry>?>(null) }
    var error by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        error = false
        try {
            highscores = ApiClient.getHighscores()
        } catch (_: Exception) {
            error = true
        }
        loading = false
    }

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
                text = "HIGHSCORES",
                fontFamily = pixelFont(),
                fontSize = 36.sp,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                loading -> {
                    Text(
                        text = "Loading...",
                        fontFamily = pixelFont(),
                        fontSize = 16.sp,
                        color = Color.LightGray,
                    )
                }
                error -> {
                    Text(
                        text = "Could not load highscores",
                        fontFamily = pixelFont(),
                        fontSize = 16.sp,
                        color = MenuColors.ERROR_TEXT,
                    )
                }
                else -> {
                    val entries = highscores.orEmpty()
                    if (entries.isEmpty()) {
                        Text(
                            text = "No highscores yet",
                            fontFamily = pixelFont(),
                            fontSize = 16.sp,
                            color = Color.LightGray,
                        )
                    } else {
                        entries.forEachIndexed { index, entry ->
                            HighscoreRow(rank = index + 1, entry = entry)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

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
private fun HighscoreRow(rank: Int, entry: HighscoreEntry) {
    Row(
        modifier = Modifier.width(300.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "#$rank  ${entry.name}",
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = when (rank) {
                1 -> MenuColors.RANK_GOLD
                2 -> MenuColors.RANK_SILVER
                3 -> MenuColors.RANK_BRONZE
                else -> Color.White
            },
        )
        Text(
            text = "${entry.score}",
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = Color.White,
        )
    }
}
