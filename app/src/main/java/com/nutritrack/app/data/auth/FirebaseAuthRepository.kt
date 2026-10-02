package com.nutritrack.app.data.auth

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.nutritrack.app.model.Account
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * [AuthRepository] backed by Firebase Authentication. Firebase persists and restores the session
 * itself; this class never stores passwords or tokens.
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Firebase calls this once on registration with the restored session, then on every change.
    // The listener lives as long as the app process, like the FirebaseAuth singleton itself.
    private val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _authState.value = firebaseAuth.currentUser?.let { AuthState.SignedIn(it.toAccount()) }
            ?: AuthState.SignedOut
    }

    init {
        auth.addAuthStateListener(listener)
    }

    override suspend fun createAccount(
        displayName: String,
        email: String,
        password: String,
        emailLanguageTag: String
    ): AuthResult = runAuth {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Firebase returned no user after account creation")
        try {
            user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The account exists and is signed in; a missing display name is not worth failing for.
            Log.w(TAG, "Account created but setting display name failed: ${e.javaClass.simpleName}")
        }
        try {
            auth.setLanguageCode(emailLanguageTag)
            user.sendEmailVerification().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The Verify Email screen offers "Resend Email", so don't fail account creation.
            Log.w(TAG, "Account created but sending verification email failed: ${e.javaClass.simpleName}")
        }
        // The auth-state listener fired before the display name was set, so publish the update.
        val account = user.toAccount()
        _authState.value = AuthState.SignedIn(account)
        account
    }

    override suspend fun signIn(email: String, password: String): AuthResult = runAuth {
        val user = auth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Firebase returned no user after sign-in")
        user.toAccount()
    }

    override suspend fun updateDisplayName(displayName: String): AuthResult = runAuth {
        val user = auth.currentUser ?: error("No signed-in user")
        user.updateProfile(userProfileChangeRequest { this.displayName = displayName }).await()
        // Profile updates don't trigger the auth-state listener, so publish the change ourselves.
        val account = user.toAccount()
        _authState.value = AuthState.SignedIn(account)
        account
    }

    override suspend fun reloadCurrentUser(): AuthResult = runAuth {
        val user = auth.currentUser ?: error("No signed-in user")
        user.reload().await()
        // Reloading doesn't trigger the auth-state listener, so publish the refreshed user.
        val account = user.toAccount()
        _authState.value = AuthState.SignedIn(account)
        account
    }

    override suspend fun sendVerificationEmail(languageTag: String): AuthResult = runAuth {
        val user = auth.currentUser ?: error("No signed-in user")
        auth.setLanguageCode(languageTag)
        user.sendEmailVerification().await()
        user.toAccount()
    }

    override fun signOut() {
        auth.signOut()
    }

    private suspend fun runAuth(block: suspend () -> Account): AuthResult =
        try {
            AuthResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }

    private companion object {
        const val TAG = "FirebaseAuthRepository"
    }
}

private fun FirebaseUser.toAccount() = Account(
    uid = uid,
    displayName = displayName,
    email = email,
    emailVerified = isEmailVerified,
    photoUrl = photoUrl?.toString()
)

private fun Exception.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
    is FirebaseAuthInvalidCredentialsException ->
        if (errorCode == "ERROR_INVALID_EMAIL") AuthError.INVALID_EMAIL else AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException ->
        if (errorCode == "ERROR_USER_DISABLED") AuthError.ACCOUNT_DISABLED else AuthError.INVALID_CREDENTIALS
    is FirebaseNetworkException -> AuthError.NETWORK
    is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_ATTEMPTS
    else -> {
        // Class name only: messages are not shown to users, and nothing here contains the password.
        Log.w("FirebaseAuthRepository", "Unmapped auth failure: ${javaClass.name}")
        AuthError.UNKNOWN
    }
}

/** Minimal Task -> coroutine bridge, so no extra coroutines-play-services dependency is needed. */
private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    addOnCompleteListener { task ->
        val exception = task.exception
        when {
            exception != null -> cont.resumeWithException(exception)
            task.isCanceled -> cont.cancel()
            else -> cont.resume(task.result)
        }
    }
}
