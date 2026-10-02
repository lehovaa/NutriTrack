package com.nutritrack.app.data.auth

import com.nutritrack.app.model.Account
import kotlinx.coroutines.flow.StateFlow

/**
 * Authentication boundary for the app. UI and ViewModels depend on this interface only;
 * [FirebaseAuthRepository] is the production implementation.
 *
 * Passwords are passed straight through to the provider and never stored by the app.
 */
interface AuthRepository {
    /** Current session state. Starts as [AuthState.Loading] until the provider restores its session. */
    val authState: StateFlow<AuthState>

    /**
     * Creates the account, sets its display name and sends a verification email in
     * [emailLanguageTag] (e.g. "ru"). On success the user is signed in but not yet verified.
     */
    suspend fun createAccount(
        displayName: String,
        email: String,
        password: String,
        emailLanguageTag: String
    ): AuthResult

    suspend fun signIn(email: String, password: String): AuthResult

    /** Updates the signed-in user's display name at the provider (the source of truth). */
    suspend fun updateDisplayName(displayName: String): AuthResult

    /** Re-fetches the signed-in user from the provider, e.g. to pick up email verification. */
    suspend fun reloadCurrentUser(): AuthResult

    /** Sends (or re-sends) the verification email in [languageTag]. */
    suspend fun sendVerificationEmail(languageTag: String): AuthResult

    fun signOut()
}

sealed interface AuthState {
    /** The provider has not yet reported whether a session exists. */
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val account: Account) : AuthState
}

sealed interface AuthResult {
    data class Success(val account: Account) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/** Provider-independent error categories, so the UI can show localized messages. */
enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_ALREADY_IN_USE,
    INVALID_EMAIL,
    WEAK_PASSWORD,
    ACCOUNT_DISABLED,
    NETWORK,
    TOO_MANY_ATTEMPTS,
    UNKNOWN
}
