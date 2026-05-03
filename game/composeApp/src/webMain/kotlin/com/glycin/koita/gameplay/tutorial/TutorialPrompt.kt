package com.glycin.koita.gameplay.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import com.glycin.koita.ui.pixelFont

@Composable
fun BoxScope.TutorialPrompt(state: TutorialState) {
    if (state.promptText.isEmpty()) return

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 48.dp)
            .widthIn(max = 700.dp)
            .heightIn(min = 140.dp)
            .background(TutorialColors.PROMPT_BACKGROUND)
            .border(2.dp, MenuColors.MAIN_BACKGROUND_LIGHT)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (!state.isCompleted) {
            Text(
                text = "STEP ${state.currentStepIndex + 1} / ${state.totalSteps}",
                fontFamily = pixelFont(),
                fontSize = 12.sp,
                color = MenuColors.SECTION_TITLE,
            )
        }
        Text(
            text = state.promptText,
            fontFamily = pixelFont(),
            fontSize = 18.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}
