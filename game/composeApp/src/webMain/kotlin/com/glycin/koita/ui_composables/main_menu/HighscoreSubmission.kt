package com.glycin.koita.ui_composables.main_menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glycin.koita.rest.ApiClient
import com.glycin.koita.rest.HighscoresResponse
import com.glycin.koita.ui_composables.MenuColors
import com.glycin.koita.ui_composables.MenuOutlinedButton
import com.glycin.koita.ui_composables.compactOr
import com.glycin.koita.ui_composables.pixelFont
import com.glycin.koita.util.formatScore
import kotlinx.coroutines.launch

private const val NAME_MAX_LENGTH = 16
private const val EMAIL_MAX_LENGTH = 255
private const val PRIVACY_NOTICE_URL = "https://www.jetbrains.com/legal/docs/privacy/privacy/"

@Composable
private fun privacyLinkStyles() = TextLinkStyles(
    style = SpanStyle(
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
        fontSize = compactOr(8.sp, 10.sp),
    ),
)

private fun sanitizeName(input: String): String =
    input.filter { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == ' ' || it == '_' }
        .take(NAME_MAX_LENGTH)

@Composable
private fun fieldWidth() = compactOr(240.dp, 320.dp)

@Composable
private fun fieldFontSize() = compactOr(12.sp, 16.sp)

@Composable
private fun helperFontSize() = compactOr(9.sp, 11.sp)

@Composable
fun HighscoreSubmission(
    score: Int,
    onSubmitted: (HighscoresResponse) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var nameInfoShown by remember { mutableStateOf(false) }
    var emailInfoShown by remember { mutableStateOf(false) }
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

        FieldWithInfo(
            active = nameInfoShown,
            onTap = { nameInfoShown = !nameInfoShown },
            field = {
                SubmissionField(
                    value = name,
                    onValueChange = { name = sanitizeName(it) },
                    placeholder = "Name",
                    enabled = !submitting,
                )
            },
        )

        Spacer(modifier = Modifier.height(compactOr(2.dp, 4.dp)))

        AnimatedVisibility(visible = nameInfoShown) {
            HelperText(
                text = "Your nickname will be shown publicly on the leaderboard. Please don't use your real name if you don't want it to appear publicly.",
            )
        }

        Spacer(modifier = Modifier.height(compactOr(8.dp, 12.dp)))

        FieldWithInfo(
            active = emailInfoShown,
            onTap = { emailInfoShown = !emailInfoShown },
            field = {
                SubmissionField(
                    value = email,
                    onValueChange = {
                        if (it.length <= EMAIL_MAX_LENGTH) email = it
                    },
                    placeholder = "Email (optional)",
                    enabled = !submitting,
                )
            },
        )

        Spacer(modifier = Modifier.height(compactOr(2.dp, 4.dp)))

        AnimatedVisibility(visible = emailInfoShown) {
            HelperText(
                text = "Your email won't be displayed publicly. We'll only use it to contact you if you qualify for a prize or to verify your submission.",
            )
        }

        Spacer(modifier = Modifier.height(compactOr(8.dp, 16.dp)))

        when {
            hasError -> Text(
                text = "Could not submit score.",
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

        SubmissionDisclaimer()

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
private fun FieldWithInfo(
    active: Boolean,
    onTap: () -> Unit,
    field: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        field()
        Spacer(modifier = Modifier.width(compactOr(4.dp, 6.dp)))
        InfoButton(active = active, onTap = onTap)
    }
}

@Composable
private fun InfoButton(active: Boolean, onTap: () -> Unit) {
    val size = compactOr(28.dp, 32.dp)
    val background = if (active) Color.White else Color.Transparent
    val glyphColor = if (active) Color.Black else Color.White

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(width = 1.dp, color = Color.White, shape = CircleShape)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?",
            fontFamily = pixelFont(),
            fontSize = compactOr(18.sp, 22.sp),
            color = glyphColor,
        )
    }
}

@Composable
private fun HelperText(text: String) {
    Text(
        text = text,
        fontFamily = FontFamily.SansSerif,
        fontSize = helperFontSize(),
        color = Color.White,
        textAlign = TextAlign.Start,
        modifier = Modifier.width(fieldWidth()),
    )
}

@Composable
private fun SubmissionDisclaimer() {
    val linkStyles = privacyLinkStyles()
    val annotated = remember(linkStyles) {
        buildAnnotatedString {
            append("By submitting your nickname and email, you agree to participate in the leaderboard. ")
            append("Your nickname and score may appear publicly on the leaderboard. ")
            append("We'll only use your email for prize-related contact and submission verification. See the ")
            withLink(LinkAnnotation.Url(url = PRIVACY_NOTICE_URL, styles = linkStyles)) {
                append("Privacy Notice")
            }
            append(" for details.")
        }
    }

    Text(
        text = annotated,
        fontFamily = FontFamily.SansSerif,
        fontSize = helperFontSize(),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(fieldWidth()),
    )
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
