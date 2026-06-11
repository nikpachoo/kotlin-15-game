package com.glycin.koita.ui_composables.main_menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.glycin.koita.ui_composables.compactOr
import koita.composeapp.generated.resources.Res
import koita.composeapp.generated.resources.github_white_icon
import org.jetbrains.compose.resources.painterResource

private const val GITHUB_URL = "https://github.com/JetBrains/kotlin-15-game"

@Composable
fun GithubCorner(modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = modifier
            .size(compactOr(108.dp, 144.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { uriHandler.openUri(GITHUB_URL) },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                close()
            }
            drawPath(triangle, color = Color.Black)
        }
        Image(
            painter = painterResource(Res.drawable.github_white_icon),
            contentDescription = "Github link",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = compactOr(15.dp, 21.dp), end = compactOr(15.dp, 21.dp))
                .size(compactOr(42.dp, 54.dp))
                .rotate(45f),
        )
    }
}
