package com.nutritrack.app

import android.app.Application
import android.content.Context
import com.nutritrack.app.data.auth.AuthRepository
import com.nutritrack.app.data.auth.FirebaseAuthRepository
import com.nutritrack.app.data.photo.LocalProfilePhotoRepository
import com.nutritrack.app.data.photo.ProfilePhotoRepository
import com.nutritrack.app.data.profile.DataStoreNutritionProfileRepository
import com.nutritrack.app.data.profile.NutritionProfileRepository
import com.nutritrack.app.data.profile.nutritionProfileDataStore
import com.nutritrack.app.data.settings.AppCompatLanguageRepository
import com.nutritrack.app.data.settings.LanguageRepository

class NutriTrackApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

/**
 * Single instances of the app's repositories, shared by all ViewModels. Sharing matters:
 * e.g. a display-name update must reach the same auth state every screen observes.
 */
class AppContainer(context: Context) {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val languageRepository: LanguageRepository by lazy { AppCompatLanguageRepository() }
    val profilePhotoRepository: ProfilePhotoRepository by lazy {
        LocalProfilePhotoRepository(context.applicationContext)
    }
    val nutritionProfileRepository: NutritionProfileRepository by lazy {
        DataStoreNutritionProfileRepository(context.nutritionProfileDataStore())
    }
}
