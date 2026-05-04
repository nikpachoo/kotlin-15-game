package com.glycin.koita.gameplay.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.composables.MenuColors
import com.glycin.koita.composables.isCompact
import com.glycin.koita.core.Input
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.ui.uiPressable

@Composable
fun BoxScope.TutorialPrompt(state: TutorialState, input: Input) {
    if (state.promptText.isEmpty()) return

    val compact = isCompact()
    val anchor = if (compact) Alignment.TopCenter else Alignment.BottomCenter
    val edgePadding = if (compact) 12.dp else 48.dp

    Column(
        modifier = Modifier
            .align(anchor)
            .padding(
                top = if (compact) edgePadding else 0.dp,
                bottom = if (compact) 0.dp else edgePadding,
            )
            .widthIn(max = if (compact) 480.dp else 700.dp)
            .heightIn(min = if (compact) 80.dp else 140.dp)
            .background(TutorialColors.PROMPT_BACKGROUND)
            .border(2.dp, MenuColors.MAIN_BACKGROUND_LIGHT)
            .padding(
                horizontal = if (compact) 14.dp else 24.dp,
                vertical = if (compact) 8.dp else 14.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (!state.isCompleted) {
            Text(
                text = "STEP ${state.currentStepIndex + 1} / ${state.totalSteps}",
                fontFamily = pixelFont(),
                fontSize = if (compact) 9.sp else 12.sp,
                color = MenuColors.SECTION_TITLE,
            )
        }
        Text(
            text = state.promptText,
            fontFamily = pixelFont(),
            fontSize = if (compact) 13.sp else 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }

    if (state.awaitingContinue) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .background(TutorialColors.PROMPT_BACKGROUND)
                .border(2.dp, MenuColors.MAIN_BACKGROUND_LIGHT)
                .uiPressable(input, onTap = { state.continueRequested = true })
                .padding(
                    horizontal = if (compact) 20.dp else 32.dp,
                    vertical = if (compact) 10.dp else 16.dp,
                ),
        ) {
            Text(
                text = "CONTINUE",
                fontFamily = pixelFont(),
                fontSize = if (compact) 16.sp else 24.sp,
                color = Color.White,
            )
        }
    }
}
