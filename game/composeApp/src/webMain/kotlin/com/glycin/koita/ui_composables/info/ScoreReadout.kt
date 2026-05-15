package com.glycin.koita.ui_composables.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.ModifierConfiguration
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.util.formatScore
import com.glycin.koita.util.formatTime
import com.glycin.koita.util.formatTwoDecimals

@Composable
fun ScoreReadout(
    score: Int,
    elapsedSeconds: Int?,
    heightBonus: Int,
    modifier: Modifier = Modifier,
) {
    val font = pixelFont()
    val combinedMultiplier = heightBonus + ModifierConfiguration.scoreMultiplier
    val multiplierLabel = remember(combinedMultiplier) { "×${combinedMultiplier.formatTwoDecimals()}" }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = score.formatScore(),
            fontFamily = font,
            fontSize = 28.sp,
            color = HudColors.SCORE_PINK,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (elapsedSeconds != null) {
                Text(
                    text = elapsedSeconds.formatTime(),
                    fontFamily = font,
                    fontSize = 14.sp,
                    color = Color.LightGray,
                )
            }
            Text(
                text = multiplierLabel,
                fontFamily = font,
                fontSize = 14.sp,
                color = HudColors.SCORE_PINK,
            )
        }
    }
}
