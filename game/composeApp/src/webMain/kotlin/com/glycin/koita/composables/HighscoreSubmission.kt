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
import com.glycin.koita.rest.HighscoresResponse
import com.glycin.koita.ui.pixelFont
import com.glycin.koita.util.formatScore
import kotlinx.coroutines.launch

private const val NAME_MAX_LENGTH = 16
private const val EMAIL_MAX_LENGTH = 255

private fun sanitizeName(input: String): String =
    input.filter { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == ' ' || it == '_' }
        .take(NAME_MAX_LENGTH)

@Composable
private fun fieldWidth() = compactOr(240.dp, 320.dp)

@Composable
private fun fieldFontSize() = compactOr(12.sp, 16.sp)

@Composable
fun HighscoreSubmission(
    score: Int,
    onSubmitted: (HighscoresResponse) -> Unit,
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
            fontSize = compactOr(12.sp, 18.sp),
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = score.formatScore(),
            fontFamily = pixelFont(),
            fontSize = compactOr(22.sp, 40.sp),
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(compactOr(8.dp, 20.dp)))

        SubmissionField(
            value = name,
            onValueChange = { name = sanitizeName(it) },
            placeholder = "Name",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(compactOr(4.dp, 8.dp)))

        SubmissionField(
            value = email,
            onValueChange = {
                if (it.length <= EMAIL_MAX_LENGTH) email = it
            },
            placeholder = "Email (optional)",
            enabled = !submitting,
        )

        Spacer(modifier = Modifier.height(compactOr(8.dp, 16.dp)))

        when {
            hasError -> Text(
                text = "Could not submit score",
                fontFamily = pixelFont(),
                fontSize = fieldFontSize(),
                color = Color.White,
            )
            submitting -> Text(
                text = "Submitting...",
                fontFamily = pixelFont(),
                fontSize = fieldFontSize(),
                color = Color.White,
            )
            else -> Spacer(modifier = Modifier.height(compactOr(12.dp, 20.dp)))
        }

        Spacer(modifier = Modifier.height(compactOr(6.dp, 12.dp)))

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
                        val response = ApiClient.createUser(
                            name = trimmedName,
                            score = score,
                            email = trimmedEmail.ifBlank { null },
                        )
                        onSubmitted(response)
                    } catch (_: Exception) {
                        hasError = true
                        submitting = false
                    }
                }
            },
            enabled = name.trim().isNotEmpty() && !submitting,
            width = fieldWidth(),
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
    val fontSize = fieldFontSize()

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
        modifier = Modifier.width(fieldWidth()),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MenuColors.INPUT_FIELD_FILL,
            unfocusedContainerColor = MenuColors.INPUT_FIELD_FILL,
            disabledContainerColor = MenuColors.INPUT_FIELD_FILL,
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White,
            disabledBorderColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            cursorColor = Color.White,
        ),
    )
}
