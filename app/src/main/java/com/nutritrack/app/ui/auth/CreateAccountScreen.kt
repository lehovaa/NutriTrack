package com.nutritrack.app.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.ui.theme.NutriTrackTheme

@Composable
fun CreateAccountScreen(
    formState: AuthFormState,
    onCreateAccount: (name: String, email: String, password: String) -> Unit,
    onSignInClick: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onClearError() } // don't show an error left over from another form

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords use remember (not rememberSaveable) so they are never written to saved state.
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val errors = if (showErrors) {
        validateCreateAccount(name, email, password, confirmPassword)
    } else {
        CreateAccountErrors()
    }
    val enabled = !formState.isSubmitting

    AuthFormScaffold(title = R.string.auth_create_account, onBack = onBack, modifier = modifier) {
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = R.string.auth_name,
            error = errors.name,
            enabled = enabled,
            contentType = ContentType.PersonFullName,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words
        )
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = R.string.auth_email,
            error = errors.email,
            enabled = enabled,
            contentType = ContentType.EmailAddress,
            keyboardType = KeyboardType.Email
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = R.string.auth_password,
            error = errors.password,
            enabled = enabled,
            contentType = ContentType.NewPassword,
            imeAction = ImeAction.Next
        )
        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = R.string.auth_confirm_password,
            error = errors.confirmPassword,
            enabled = enabled,
            contentType = ContentType.NewPassword,
            imeAction = ImeAction.Done
        )
        formState.error?.let { FormErrorMessage(it.messageRes) }
        Spacer(modifier = Modifier.height(4.dp))
        SubmitButton(text = R.string.auth_create_account, isSubmitting = formState.isSubmitting) {
            showErrors = true
            if (!validateCreateAccount(name, email, password, confirmPassword).hasErrors) {
                onCreateAccount(name, email, password)
            }
        }
        SwitchFormLink(
            text = R.string.auth_have_account_sign_in,
            enabled = enabled,
            onClick = onSignInClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CreateAccountScreenPreview() {
    NutriTrackTheme {
        CreateAccountScreen(
            formState = AuthFormState(),
            onCreateAccount = { _, _, _ -> },
            onSignInClick = {},
            onBack = {},
            onClearError = {}
        )
    }
}
