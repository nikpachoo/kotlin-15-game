package com.glycin.koita.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.rest.ApiClient
import com.glycin.koita.ui.pixelFont
import kotlinx.coroutines.launch

private const val NAME_MAX_LENGTH = 50
private const val EMAIL_MAX_LENGTH = 255
private val FIELD_WIDTH = 320.dp

@Composable
fun HighscoreSubmission(
    score: Int,
    onSubmitted: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "FINAL SCORE",
            fontFamily = pixelFont(),
            fontSize = 18.sp,
            color = MenuColors.SECTION_TITLE,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$score",
            fontFamily = pixelFont(),
            fontSize = 40.sp,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(20.dp))

        SubmissionField(
            value = name,
            onValueChange = {
                if (it.length <= NAME_MAX_LENGTH) name = it
            },
            placeholder = "Name",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SubmissionField(
            value = email,
            onValueChange = {
                if (it.length <= EMAIL_MAX_LENGTH) email = it
            },
            placeholder = "Email (optional)",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            hasError -> Text(
                text = "Could not submit score",
                fontFamily = pixelFont(),
                fontSize = 14.sp,
                color = MenuColors.ERROR_TEXT,
            )
            submitting -> Text(
                text = "Submitting...",
                fontFamily = pixelFont(),
                fontSize = 14.sp,
                color = Color.LightGray,
            )
            else -> Spacer(modifier = Modifier.height(20.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                val trimmedName = name.trim()
                val trimmedEmail = email.trim()
                if (trimmedName.isEmpty() || submitting) return@OutlinedButton
                submitting = true
                hasError = false
                scope.launch {
                    try {
                        ApiClient.createUser(
                            name = trimmedName,
                            score = score,
                            email = trimmedEmail.ifBlank { null },
                        )
                        onSubmitted()
                    } catch (_: Exception) {
                        hasError = true
                        submitting = false
                    }
                }
            },
            enabled = name.trim().isNotEmpty() && !submitting,
            modifier = Modifier.width(FIELD_WIDTH).height(44.dp),
            border = BorderStroke(2.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.Gray,
            ),
        ) {
            Text(
                text = "Submit",
                fontFamily = pixelFont(),
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun SubmissionField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = pixelFont(),
            fontSize = 16.sp,
            color = Color.White,
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = pixelFont(),
                fontSize = 16.sp,
                color = Color.LightGray,
            )
        },
        modifier = Modifier.width(FIELD_WIDTH),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MenuColors.INPUT_BACKGROUND,
            unfocusedContainerColor = MenuColors.INPUT_BACKGROUND,
            disabledContainerColor = MenuColors.INPUT_BACKGROUND,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.Gray,
            disabledBorderColor = Color.DarkGray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.LightGray,
            cursorColor = Color.White,
        ),
    )
}
