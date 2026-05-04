package com.glycin.koita.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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

private const val NAME_MAX_LENGTH = 16
private const val EMAIL_MAX_LENGTH = 255

private fun sanitizeName(input: String): String =
    input.filter { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == ' ' || it == '_' }
        .take(NAME_MAX_LENGTH)

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

    val compact = isCompact()
    val fieldFontSize = if (compact) 12.sp else 16.sp
    val fieldWidth = if (compact) 240.dp else 320.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "FINAL SCORE",
            fontFamily = pixelFont(),
            fontSize = if (compact) 12.sp else 18.sp,
            color = MenuColors.SECTION_TITLE,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "$score",
            fontFamily = pixelFont(),
            fontSize = if (compact) 22.sp else 40.sp,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 20.dp))

        SubmissionField(
            value = name,
            onValueChange = { name = sanitizeName(it) },
            placeholder = "Name",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(if (compact) 4.dp else 8.dp))

        SubmissionField(
            value = email,
            onValueChange = {
                if (it.length <= EMAIL_MAX_LENGTH) email = it
            },
            placeholder = "Email (optional)",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(if (compact) 8.dp else 16.dp))

        when {
            hasError -> Text(
                text = "Could not submit score",
                fontFamily = pixelFont(),
                fontSize = fieldFontSize,
                color = MenuColors.ERROR_TEXT,
            )
            submitting -> Text(
                text = "Submitting...",
                fontFamily = pixelFont(),
                fontSize = fieldFontSize,
                color = Color.LightGray,
            )
            else -> Spacer(modifier = Modifier.height(if (compact) 12.dp else 20.dp))
        }

        Spacer(modifier = Modifier.height(if (compact) 6.dp else 12.dp))

        MenuOutlinedButton(
            text = "Submit",
            onClick = {
                val trimmedName = name.trim()
                val trimmedEmail = email.trim()
                if (trimmedName.isEmpty() || submitting) return@MenuOutlinedButton
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
            width = fieldWidth,
        )
    }
}

@Composable
private fun SubmissionField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
) {
    val compact = isCompact()
    val fontSize = if (compact) 12.sp else 16.sp
    val width = if (compact) 240.dp else 320.dp

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = pixelFont(),
            fontSize = fontSize,
            color = Color.White,
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = pixelFont(),
                fontSize = fontSize,
                color = Color.LightGray,
            )
        },
        modifier = Modifier.width(width),
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
