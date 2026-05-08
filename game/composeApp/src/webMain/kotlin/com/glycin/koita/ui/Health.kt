package com.glycin.koita.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.composables.isCompact
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.heart
import org.jetbrains.compose.resources.painterResource

private const val ICONS_THRESHOLD = 5
private const val FADED_ALPHA = 0.3f
private val PANEL_CORNER = 4.dp

@Composable
fun Health(
    currentHp: Int,
    maxHp: Int,
    modifier: Modifier = Modifier,
) {
    val compact = isCompact()
    val shape = remember(PANEL_CORNER) { RoundedCornerShape(PANEL_CORNER) }

    Row(
        modifier = modifier
            .background(HudColors.LIVES_PANEL_BACKGROUND, shape)
            .padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
    ) {
        if (currentHp > ICONS_THRESHOLD) {
            HeartImage(size = if (compact) 20.dp else 28.dp)
            Text(
                text = "$currentHp/$maxHp",
                fontFamily = pixelFont(),
                fontSize = if (compact) 16.sp else 22.sp,
                color = HudColors.LIVES_TEXT_COLOR,
            )
        } else {
            val heartSize = if (compact) 22.dp else 32.dp
            for (i in 0..<ICONS_THRESHOLD) {
                HeartImage(
                    size = heartSize,
                    alpha = if (i < currentHp) 1f else FADED_ALPHA,
                )
            }
        }
    }
}

@Composable
private fun HeartImage(
    size: Dp,
    alpha: Float = 1f,
) {
    Image(
        painter = painterResource(Res.drawable.heart),
        contentDescription = "Heart",
        modifier = Modifier.size(size),
        alpha = alpha,
    )
}
