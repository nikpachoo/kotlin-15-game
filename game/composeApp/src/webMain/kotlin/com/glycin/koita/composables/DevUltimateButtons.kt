package com.glycin.koita.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.gameplay.ultimates.UltimateId
import com.glycin.koita.gameplay.ultimates.UltimateManager

@Composable
fun DevUltimateButtons(
    ultimateManager: UltimateManager,
    focusRequester: FocusRequester,
) {
    Column(modifier = Modifier.padding(start = 20.dp, top = 200.dp)) {
        for (id in UltimateId.entries) {
            OutlinedButton(onClick = {
                ultimateManager.devUnlock(id)
                focusRequester.requestFocus()
            }) {
                Text(
                    text = "Unlock ${id.name}",
                    color = Color.White,
                    fontSize = 10.sp,
                )
            }
        }
    }
}
