package com.glycin.koita.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.pixeloid_mono
import org.jetbrains.compose.resources.Font

@Composable
fun pixelFont(): FontFamily {
    return FontFamily(
        Font(
            resource = Res.font.pixeloid_mono,
            weight = FontWeight.Normal,
            style = FontStyle.Normal,
        )
    )
}