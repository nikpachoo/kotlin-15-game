package com.glycin.koita.ui_composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.main_menu.KotlinLogo

enum class LayoutMode {
    NORMAL,
    COMPACT,
}

private const val COMPACT_HEIGHT_DP = 480
private const val COMPACT_WIDTH_DP = 700

val LocalLayoutMode = staticCompositionLocalOf { LayoutMode.NORMAL }

fun layoutModeFor(maxWidth: Dp, maxHeight: Dp): LayoutMode {
    return if (maxHeight.value < COMPACT_HEIGHT_DP || maxWidth.value < COMPACT_WIDTH_DP) {
        LayoutMode.COMPACT
    } else {
        LayoutMode.NORMAL
    }
}

@Composable
fun isCompact(): Boolean = LocalLayoutMode.current == LayoutMode.COMPACT

@Composable
fun <T> compactOr(compact: T, normal: T): T = if (isCompact()) compact else normal

internal data class MenuPanelLayout(
    val backTabSize: Dp,
    val panelGap: Dp,
    val panelWidth: Dp,
)

@Composable
internal fun BoxWithConstraintsScope.menuPanelLayout(
    normalPanelWidth: Dp = 540.dp,
    compactWidthRatio: Float = 0.95f,
): MenuPanelLayout {
    val backTabSize = compactOr(40.dp, 56.dp)
    val panelGap = compactOr(8.dp, 12.dp)
    val panelWidth = if (isCompact()) {
        maxWidth * compactWidthRatio - backTabSize - panelGap
    } else {
        normalPanelWidth
    }
    return MenuPanelLayout(backTabSize, panelGap, panelWidth)
}

@Composable
internal fun MenuHeader(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = Color.White,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
) {
    Row(
        modifier = modifier.padding(
            horizontal = compactOr(24.dp, 48.dp),
            vertical = compactOr(20.dp, 36.dp),
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement,
    ) {
        KotlinLogo(boxSize = compactOr(32.dp, 64.dp))
        Text(
            text = title,
            fontFamily = pixelFont(),
            fontSize = compactOr(28.sp, 56.sp),
            color = titleColor,
        )
    }
}

@Composable
fun MenuOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    width: Dp = 160.dp,
    containerColor: Color = Color.Transparent,
) {
    val compact = isCompact()
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.width(width).height(if (compact) 36.dp else 44.dp),
        border = BorderStroke(2.dp, Color.White),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContentColor = Color.White,
        ),
    ) {
        Text(
            text = text,
            fontFamily = pixelFont(),
            fontSize = if (compact) 14.sp else 16.sp,
        )
    }
}
