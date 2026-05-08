package com.glycin.koita.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.GameState
import com.glycin.koita.gameplay.Screen
import com.glycin.koita.rest.ApiClient
import com.glycin.koita.rest.HighscoreEntry
import com.glycin.koita.rest.HighscoresResponse
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.util.formatScore

private const val TOP_SLOTS = 10

private sealed interface HighscoresUiState {
    data object Loading : HighscoresUiState
    data object Error : HighscoresUiState
    data class Loaded(val response: HighscoresResponse) : HighscoresUiState
}

@Composable
fun HighscoresScreen(gameState: GameState) {
    var uiState by remember { mutableStateOf<HighscoresUiState>(HighscoresUiState.Loading) }

    LaunchedEffect(Unit) {
        uiState = try {
            HighscoresUiState.Loaded(ApiClient.getHighscores())
        } catch (_: Exception) {
            HighscoresUiState.Error
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(rememberMenuBackgroundBrush()),
    ) {
        val compact = isCompact()
        val backTabSize = compactOr(40.dp, 56.dp)
        val panelGap = compactOr(8.dp, 12.dp)
        val panelHeight = maxHeight * compactOr(0.9f, 0.85f)
        val maxPanelWidth = if (compact) {
            maxWidth * 0.95f - backTabSize - panelGap
        } else {
            540.dp
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(compactOr(8.dp, 16.dp)),
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(panelGap),
            ) {
                BackTab(
                    boxSize = backTabSize,
                    onClick = { gameState.currentScreen = Screen.MAIN_MENU },
                )
                HighscorePanel(
                    width = maxPanelWidth,
                    height = panelHeight,
                ) {
                    when (val state = uiState) {
                        HighscoresUiState.Loading -> CenteredMessage("Loading...", color = Color.White)
                        HighscoresUiState.Error -> CenteredMessage(
                            text = "Could not load highscores",
                            color = MenuColors.ERROR_TEXT,
                        )
                        is HighscoresUiState.Loaded -> HighscoreList(state.response)
                    }
                }
            }
            (uiState as? HighscoresUiState.Loaded)?.let {
                Text(
                    text = "${it.response.totalEntries.formatScore()} total scores",
                    fontFamily = pixelFont(),
                    fontSize = compactOr(14.sp, 18.sp),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun HighscorePanel(
    width: Dp,
    height: Dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(height)
            .background(MenuColors.SIDEBAR),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = compactOr(24.dp, 48.dp),
                    vertical = compactOr(20.dp, 36.dp),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
            Text(
                text = "HIGHSCORES",
                fontFamily = pixelFont(),
                fontSize = compactOr(20.sp, 36.sp),
                color = Color.White,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            content = content,
        )
    }
}

@Composable
private fun ColumnScope.HighscoreList(response: HighscoresResponse) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    repeat(TOP_SLOTS) { i ->
        val entry = response.top.getOrNull(i)
        HighscoreRow(
            rank = entry?.rank ?: (i + 1),
            entry = entry,
            modifier = rowModifier,
        )
    }
    response.userEntry?.let { userEntry ->
        HighscoreRow(
            rank = userEntry.rank,
            entry = userEntry,
            modifier = rowModifier,
        )
    }
}

@Composable
private fun HighscoreRow(rank: Int, entry: HighscoreEntry?, modifier: Modifier) {
    val isTop3 = rank <= 3
    val rowBackground = if (isTop3) Color.White else Color.White.copy(alpha = 0.25f)
    val textColor = if (isTop3) Color.Black else Color.Black.copy(alpha = 0.7f)
    val medalColor = when (rank) {
        1 -> MenuColors.RANK_GOLD
        2 -> MenuColors.RANK_SILVER
        3 -> MenuColors.RANK_BRONZE
        else -> null
    }
    val rowFontSize = compactOr(14.sp, 20.sp)
    val medalSize = compactOr(14.dp, 20.dp)
    val rowHorizontalPadding = compactOr(20.dp, 36.dp)
    val rankColumnWidth = compactOr(36.dp, 56.dp)

    Row(
        modifier = modifier
            .background(rowBackground)
            .padding(horizontal = rowHorizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.width(rankColumnWidth),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (medalColor != null) {
                Box(
                    modifier = Modifier
                        .size(medalSize)
                        .background(medalColor, CircleShape),
                )
            } else {
                Text(
                    text = "#$rank",
                    fontFamily = pixelFont(),
                    fontSize = rowFontSize,
                    color = textColor,
                )
            }
        }
        Text(
            text = entry?.name.orEmpty(),
            fontFamily = pixelFont(),
            fontSize = rowFontSize,
            color = textColor,
            modifier = Modifier
                .weight(1f)
                .padding(start = compactOr(10.dp, 18.dp)),
        )
        Text(
            text = entry?.score?.formatScore().orEmpty(),
            fontFamily = pixelFont(),
            fontSize = rowFontSize,
            color = textColor,
        )
    }
}

@Composable
private fun ColumnScope.CenteredMessage(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = compactOr(16.sp, 22.sp),
            color = color,
        )
    }
}

@Composable
private fun BackTab(boxSize: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .background(MenuColors.SIDEBAR)
            .clickable(onClick = onClick)
            .padding(compactOr(10.dp, 14.dp))
            .drawWithCache {
                val w = size.width
                val h = size.height
                val arrowPath = Path().apply {
                    moveTo(0f, h * 0.5f)
                    lineTo(w * 0.45f, h * 0.1f)
                    lineTo(w * 0.45f, h * 0.35f)
                    lineTo(w, h * 0.35f)
                    lineTo(w, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.65f)
                    lineTo(w * 0.45f, h * 0.9f)
                    close()
                }
                onDrawBehind { drawPath(arrowPath, Color.White) }
            },
    )
}
