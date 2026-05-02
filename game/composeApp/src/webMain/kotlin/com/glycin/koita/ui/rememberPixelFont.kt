package com.glycin.koita.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.pixeloid_mono
import org.jetbrains.compose.resources.Font

@Composable
fun pixelFont(): FontFamily {
    val font = Font(
        resource = Res.font.pixeloid_mono,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
    )
    return remember(font) { FontFamily(font) }
}