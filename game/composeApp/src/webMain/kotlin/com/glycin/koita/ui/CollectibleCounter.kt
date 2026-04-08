package com.glycin.koita.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CollectibleCounter(
    collectableCount: Int,
    offsetX: Dp,
    offsetY: Dp,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.offset(x = offsetX, y = offsetY)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(Color.Gray, shape = CircleShape)
        )

        Text(
            text = "x$collectableCount",
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
        )
    }
}