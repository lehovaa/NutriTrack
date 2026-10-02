package com.nutritrack.app.ui.auth

import androidx.annotation.StringRes
import com.nutritrack.app.R
import com.nutritrack.app.data.auth.AuthError

// Deliberately loose: catches obvious typos; Firebase remains the authority on validity.
private val EMAIL_PATTERN = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

internal fun isValidLookingEmail(email: String): Boolean = EMAIL_PATTERN.matches(email.trim())

internal data class CreateAccountErrors(
    @get:StringRes val name: Int? = null,
    @get:StringRes val email: Int? = null,
    @get:StringRes val password: Int? = null,
    @get:StringRes val confirmPassword: Int? = null
) {
    val hasErrors: Boolean
        get() = listOf(name, email, password, confirmPassword).any { it != null }
}

internal fun validateCreateAccount(
    name: String,
    email: String,
    password: String,
    confirmPassword: String
) = CreateAccountErrors(
    name = displayNameError(name),
    email = emailError(email),
    password = if (password.isEmpty()) R.string.auth_error_password_required else null,
    confirmPassword = when {
        confirmPassword.isEmpty() -> R.string.auth_error_confirm_password_required
        confirmPassword != password -> R.string.auth_error_passwords_do_not_match
        else -> null
    }
)

@StringRes
internal fun displayNameError(name: String): Int? =
    if (name.isBlank()) R.string.auth_error_name_required else null

internal data class SignInErrors(
    @get:StringRes val email: Int? = null,
    @get:StringRes val password: Int? = null
) {
    val hasErrors: Boolean
        get() = email != null || password != null
}

internal fun validateSignIn(email: String, password: String) = SignInErrors(
    email = emailError(email),
    password = if (password.isEmpty()) R.string.auth_error_password_required else null
)

@StringRes
private fun emailError(email: String): Int? = when {
    email.isBlank() -> R.string.auth_error_email_required
    !isValidLookingEmail(email) -> R.string.auth_error_email_invalid
    else -> null
}

@get:StringRes
internal val AuthError.messageRes: Int
    get() = when (this) {
        AuthError.INVALID_CREDENTIALS -> R.string.auth_error_invalid_credentials
        AuthError.EMAIL_ALREADY_IN_USE -> R.string.auth_error_email_in_use
        AuthError.INVALID_EMAIL -> R.string.auth_error_email_invalid
        AuthError.WEAK_PASSWORD -> R.string.auth_error_weak_password
        AuthError.ACCOUNT_DISABLED -> R.string.auth_error_account_disabled
        AuthError.NETWORK -> R.string.auth_error_network
        AuthError.TOO_MANY_ATTEMPTS -> R.string.auth_error_too_many_attempts
        AuthError.UNKNOWN -> R.string.auth_error_unknown
    }
