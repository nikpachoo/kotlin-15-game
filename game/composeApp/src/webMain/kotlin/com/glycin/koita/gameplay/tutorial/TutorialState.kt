package com.glycin.koita.gameplay.tutorial

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class TutorialState(val totalSteps: Int) {
    var currentStepIndex by mutableIntStateOf(0)
    var promptText by mutableStateOf("")
    var isCompleted by mutableStateOf(false)
    var awaitingContinue by mutableStateOf(false)
    var continueRequested by mutableStateOf(false)
}
