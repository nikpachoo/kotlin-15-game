package com.glycin.koita.gameplay.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.isCompact
import com.glycin.koita.core.Input
import com.glycin.koita.ui_composables.input.ArrowChip
import com.glycin.koita.ui_composables.HudColors
import com.glycin.koita.ui_composables.pixelFont

private val BAR_CORNER_RADIUS = 2.dp
private val BAR_BORDER = 2.dp

@Composable
fun BoxScope.TutorialPrompt(state: TutorialState, input: Input) {
    if (state.promptText.isEmpty()) return

    val compact = isCompact()
    val maxWidth = if (compact) 480.dp else 700.dp
    val edgePadding = if (compact) 12.dp else 24.dp
    val horizontalPadding = if (compact) 12.dp else 18.dp
    val verticalPadding = if (compact) 8.dp else 12.dp
    val rowSpacing = if (compact) 8.dp else 12.dp
    val columnSpacing = if (compact) 2.dp else 4.dp
    val stepFontSize = if (compact) 9.sp else 12.sp
    val bodyFontSize = if (compact) 12.sp else 15.sp
    val arrowSize = if (compact) 40.dp else 48.dp
    val shape = remember { RoundedCornerShape(BAR_CORNER_RADIUS) }

    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(edgePadding)
            .widthIn(max = maxWidth)
            .background(HudColors.BUTTON_IDLE, shape)
            .border(BAR_BORDER, HudColors.BUTTON_BORDER, shape)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(rowSpacing),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(columnSpacing),
        ) {
            if (!state.isCompleted) {
                Text(
                    text = "STEP ${state.currentStepIndex + 1}/${state.totalSteps}",
                    fontFamily = pixelFont(),
                    fontSize = stepFontSize,
                    color = MenuColors.SECTION_TITLE,
                )
            }
            Text(
                text = state.promptText,
                fontFamily = pixelFont(),
                fontSize = bodyFontSize,
                color = Color.White,
            )
        }
        ArrowChip(
            text = ">",
            input = input,
            key = "tutorial_continue",
            enabled = state.awaitingContinue,
            size = arrowSize,
            onTap = { state.continueRequested = true },
        )
    }
}
