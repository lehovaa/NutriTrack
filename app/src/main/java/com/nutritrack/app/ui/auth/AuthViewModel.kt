package com.nutritrack.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nutritrack.app.NutriTrackApplication
import com.nutritrack.app.data.auth.AuthError
import com.nutritrack.app.data.auth.AuthRepository
import com.nutritrack.app.data.auth.AuthResult
import com.nutritrack.app.data.auth.AuthState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Submission state shared by the Sign In and Create Account forms. */
data class AuthFormState(
    val isSubmitting: Boolean = false,
    val error: AuthError? = null
)

/** Feedback shown on the Verify Email screen. */
enum class VerifyEmailMessage { NOT_VERIFIED_YET, EMAIL_SENT }

data class VerifyEmailState(
    val isChecking: Boolean = false,
    val isResending: Boolean = false,
    /** Seconds until "Resend Email" is enabled again; 0 = enabled. */
    val resendCooldownSeconds: Int = 0,
    val message: VerifyEmailMessage? = null,
    val error: AuthError? = null
)

/**
 * Exposes the session state to the UI and runs sign-in, account-creation and email-verification
 * requests. Passwords are only forwarded to [AuthRepository]; they are never kept in state here.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    val authState: StateFlow<AuthState> = repository.authState

    private val _formState = MutableStateFlow(AuthFormState())
    val formState: StateFlow<AuthFormState> = _formState.asStateFlow()

    private val _verifyState = MutableStateFlow(VerifyEmailState())
    val verifyState: StateFlow<VerifyEmailState> = _verifyState.asStateFlow()
    private var cooldownJob: Job? = null
    private var silentCheckRunning = false

    fun signIn(email: String, password: String) = submit {
        repository.signIn(email.trim(), password)
    }

    fun createAccount(name: String, email: String, password: String, emailLanguageTag: String) = submit {
        repository.createAccount(name.trim(), email.trim(), password, emailLanguageTag)
    }

    /**
     * Asks Firebase whether the email is verified now. If it is, the auth state updates and
     * routing moves on by itself. [silent] checks (on screen open/resume) never show "not yet".
     */
    fun checkEmailVerified(silent: Boolean) {
        if (_verifyState.value.isChecking || (silent && silentCheckRunning)) return
        // Silent checks keep any visible feedback (e.g. "email sent") untouched.
        if (!silent) _verifyState.update { it.copy(isChecking = true, message = null, error = null) }
        if (silent) silentCheckRunning = true
        viewModelScope.launch {
            val result = repository.reloadCurrentUser()
            if (silent) silentCheckRunning = false
            _verifyState.update { state ->
                when {
                    silent -> state // routing reacts to a verified account by itself
                    result is AuthResult.Failure -> state.copy(isChecking = false, error = result.error)
                    (result as AuthResult.Success).account.emailVerified -> state.copy(isChecking = false)
                    else -> state.copy(isChecking = false, message = VerifyEmailMessage.NOT_VERIFIED_YET)
                }
            }
        }
    }

    fun resendVerificationEmail(languageTag: String) {
        val state = _verifyState.value
        if (state.isResending || state.resendCooldownSeconds > 0) return // prevent rapid repeats
        _verifyState.update { it.copy(isResending = true, message = null, error = null) }
        viewModelScope.launch {
            when (val result = repository.sendVerificationEmail(languageTag)) {
                is AuthResult.Success -> {
                    _verifyState.update {
                        it.copy(isResending = false, message = VerifyEmailMessage.EMAIL_SENT)
                    }
                    startResendCooldown()
                }
                is AuthResult.Failure -> _verifyState.update {
                    it.copy(isResending = false, error = result.error)
                }
            }
        }
    }

    fun signOut() {
        cooldownJob?.cancel()
        _verifyState.value = VerifyEmailState()
        repository.signOut()
    }

    fun clearError() {
        _formState.update { it.copy(error = null) }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 1) {
                _verifyState.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1_000)
            }
            _verifyState.update { it.copy(resendCooldownSeconds = 0) }
        }
    }

    private fun submit(request: suspend () -> AuthResult) {
        if (_formState.value.isSubmitting) return // ignore repeated taps
        _formState.value = AuthFormState(isSubmitting = true)
        viewModelScope.launch {
            // On success the auth state changes and routing moves on; only failures need showing.
            val result = request()
            _formState.value = AuthFormState(error = (result as? AuthResult.Failure)?.error)
        }
    }

    companion object {
        private const val RESEND_COOLDOWN_SECONDS = 30

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NutriTrackApplication
                AuthViewModel(app.container.authRepository)
            }
        }
    }
}
