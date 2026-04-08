package com.glycin.koita.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun HotkeyBar(
    offsetX: Dp,
    offsetY: Dp,
    selectedIndex: Int,
    items: List<Pair<DrawableResource, DrawableResource>>,
) {
    Column(
        modifier = Modifier.offset(x = offsetX, y = offsetY),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${index + 1}",
                    fontFamily = pixelFont(),
                    fontSize = 24.sp,
                    color = Color.White,
                    modifier = Modifier.width(24.dp)
                )

                Box(
                    modifier = Modifier.size(64.dp)
                ) {
                    val drawable = if(selectedIndex == index) item.second else item.first
                    Image(
                        painter = painterResource(drawable),
                        contentDescription = "Hotkey ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}