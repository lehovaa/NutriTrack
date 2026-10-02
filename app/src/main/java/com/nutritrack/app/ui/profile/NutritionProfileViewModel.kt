package com.nutritrack.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nutritrack.app.NutriTrackApplication
import com.nutritrack.app.data.auth.AuthRepository
import com.nutritrack.app.data.auth.AuthState
import com.nutritrack.app.data.profile.NutritionProfileRepository
import com.nutritrack.app.model.NutritionProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The signed-in account's NutritionProfile as it loads from storage. */
sealed interface ProfileLoadState {
    data object Loading : ProfileLoadState

    /** [uid] identifies whose profile this is; [profile] is null if they haven't set one up. */
    data class Loaded(val uid: String, val profile: NutritionProfile?) : ProfileLoadState
}

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionProfileViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: NutritionProfileRepository
) : ViewModel() {

    /** Follows the signed-in uid, so each account only ever sees its own profile. */
    val profileState: StateFlow<ProfileLoadState> = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.account?.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(ProfileLoadState.Loading)
            } else {
                profileRepository.observeProfile(uid)
                    .map<NutritionProfile?, ProfileLoadState> { ProfileLoadState.Loaded(uid, it) }
                    .onStart { emit(ProfileLoadState.Loading) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProfileLoadState.Loading)

    fun saveProfile(profile: NutritionProfile) {
        val uid = (authRepository.authState.value as? AuthState.SignedIn)?.account?.uid ?: return
        viewModelScope.launch { profileRepository.saveProfile(uid, profile) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as NutriTrackApplication).container
                NutritionProfileViewModel(container.authRepository, container.nutritionProfileRepository)
            }
        }
    }
}
