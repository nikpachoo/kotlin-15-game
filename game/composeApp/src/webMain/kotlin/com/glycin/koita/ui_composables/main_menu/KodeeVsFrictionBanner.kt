package com.glycin.koita.ui_composables.main_menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.pixelFont

@Composable
internal fun KodeeVsFrictionBanner(modifier: Modifier = Modifier) {
    val titleSize = compactOr(12.sp, 24.sp)
    val vsSize = compactOr(7.sp, 13.sp)
    val rowGap = compactOr(1.dp, 2.dp)
    val sparkWidth = compactOr(14.dp, 28.dp)
    val sparkHeight = compactOr(1.dp, 2.dp)
    val font = pixelFont()
    val sparkModifier = Modifier
        .width(sparkWidth)
        .height(sparkHeight)
        .background(Color.White)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(rowGap),
    ) {
        Text(
            text = "KODEE",
            fontFamily = font,
            fontSize = titleSize,
            lineHeight = titleSize,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = sparkModifier)
            Text(
                text = "vs",
                fontFamily = font,
                fontSize = vsSize,
                lineHeight = vsSize,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Box(modifier = sparkModifier)
        }
        Text(
            text = "FRICTION",
            fontFamily = font,
            fontSize = titleSize,
            lineHeight = titleSize,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
