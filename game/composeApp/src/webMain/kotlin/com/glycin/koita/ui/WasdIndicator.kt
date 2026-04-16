package com.glycin.koita.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WasdIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        KeyBox("W")
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KeyBox("A")
            KeyBox("S")
            KeyBox("D")
        }
    }
}

@Composable
private fun KeyBox(label: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(36.dp)
            .border(1.dp, Color.Gray),
    ) {
        Text(
            text = label,
            fontFamily = pixelFont(),
            fontSize = 14.sp,
            color = Color.White,
        )
    }
}
