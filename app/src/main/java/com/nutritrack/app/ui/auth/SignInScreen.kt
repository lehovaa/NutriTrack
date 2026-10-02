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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.data.auth.AuthError
import com.nutritrack.app.ui.theme.NutriTrackTheme

@Composable
fun SignInScreen(
    formState: AuthFormState,
    onSignIn: (email: String, password: String) -> Unit,
    onCreateAccountClick: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onClearError() } // don't show an error left over from another form

    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val errors = if (showErrors) validateSignIn(email, password) else SignInErrors()
    val enabled = !formState.isSubmitting

    AuthFormScaffold(title = R.string.auth_sign_in, onBack = onBack, modifier = modifier) {
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
            contentType = ContentType.Password,
            imeAction = ImeAction.Done
        )
        formState.error?.let { FormErrorMessage(it.messageRes) }
        Spacer(modifier = Modifier.height(4.dp))
        SubmitButton(text = R.string.auth_sign_in, isSubmitting = formState.isSubmitting) {
            showErrors = true
            if (!validateSignIn(email, password).hasErrors) onSignIn(email, password)
        }
        SwitchFormLink(
            text = R.string.auth_no_account_create,
            enabled = enabled,
            onClick = onCreateAccountClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    NutriTrackTheme {
        SignInScreen(
            formState = AuthFormState(error = AuthError.INVALID_CREDENTIALS),
            onSignIn = { _, _ -> },
            onCreateAccountClick = {},
            onBack = {},
            onClearError = {}
        )
    }
}
