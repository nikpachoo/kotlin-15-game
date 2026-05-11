package com.glycin.koita.ui_composables.info

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.util.formatScore
import com.glycin.koita.util.formatTime

@Composable
fun ScoreReadout(
    score: Int,
    elapsedSeconds: Int?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = score.formatScore(),
            fontFamily = pixelFont(),
            fontSize = 28.sp,
            color = HudColors.SCORE_PINK,
        )
        if (elapsedSeconds != null) {
            Text(
                text = elapsedSeconds.formatTime(),
                fontFamily = pixelFont(),
                fontSize = 14.sp,
                color = Color.LightGray,
            )
        }
    }
}
