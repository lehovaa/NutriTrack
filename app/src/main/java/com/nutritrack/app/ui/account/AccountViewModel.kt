package com.nutritrack.app.ui.account

import android.net.Uri
import android.util.Log
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
import com.nutritrack.app.data.photo.ProfilePhotoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A pending change to the profile photo, applied only when the user saves. */
sealed interface PhotoEdit {
    data object Unchanged : PhotoEdit
    data class Replace(val source: Uri) : PhotoEdit
    data object Remove : PhotoEdit
}

/** State of the Edit Account form; [saved] tells the screen to close after a successful save. */
data class AccountEditState(
    val isSaving: Boolean = false,
    val error: AuthError? = null,
    val photoFailed: Boolean = false,
    val saved: Boolean = false
)

/** Account details the user can change: display name (Firebase) and profile photo. */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModel(
    private val authRepository: AuthRepository,
    private val photoRepository: ProfilePhotoRepository
) : ViewModel() {

    /** The signed-in account's photo, or null. Follows sign-in/out automatically. */
    val accountPhoto: StateFlow<Uri?> = authRepository.authState
        .flatMapLatest { state ->
            if (state is AuthState.SignedIn) photoRepository.observePhoto(state.account.uid) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _editState = MutableStateFlow(AccountEditState())
    val editState: StateFlow<AccountEditState> = _editState.asStateFlow()

    fun save(displayName: String, photoEdit: PhotoEdit) {
        if (_editState.value.isSaving) return
        val account = (authRepository.authState.value as? AuthState.SignedIn)?.account ?: return
        val name = displayName.trim()
        _editState.value = AccountEditState(isSaving = true)
        viewModelScope.launch {
            if (name != account.displayName) {
                val result = authRepository.updateDisplayName(name)
                if (result is AuthResult.Failure) {
                    _editState.value = AccountEditState(error = result.error)
                    return@launch
                }
            }
            val photoSaved = try {
                when (photoEdit) {
                    PhotoEdit.Unchanged -> Unit
                    is PhotoEdit.Replace -> photoRepository.setPhoto(account.uid, photoEdit.source)
                    PhotoEdit.Remove -> photoRepository.removePhoto(account.uid)
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Saving profile photo failed: ${e.javaClass.simpleName}")
                false
            }
            _editState.value = if (photoSaved) AccountEditState(saved = true) else AccountEditState(photoFailed = true)
        }
    }

    /** Resets the form, e.g. when the screen opens or after it has closed on success. */
    fun resetEdit() {
        _editState.value = AccountEditState()
    }

    companion object {
        private const val TAG = "AccountViewModel"

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as NutriTrackApplication).container
                AccountViewModel(container.authRepository, container.profilePhotoRepository)
            }
        }
    }
}
