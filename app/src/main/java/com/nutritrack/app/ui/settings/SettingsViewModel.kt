package com.nutritrack.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nutritrack.app.NutriTrackApplication
import com.nutritrack.app.data.settings.LanguageRepository
import com.nutritrack.app.model.AppLanguage

class SettingsViewModel(private val languageRepository: LanguageRepository) : ViewModel() {

    fun setLanguage(language: AppLanguage) {
        // The activity is recreated with the new locale; navigation and ViewModel state survive.
        languageRepository.setLanguage(language)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as NutriTrackApplication
                SettingsViewModel(app.container.languageRepository)
            }
        }
    }
}

/**
 * The language the UI is actually displayed in: the user's explicit choice if any, otherwise the
 * best match for the device languages (Russian device -> Russian, anything else -> English).
 */
@Composable
fun currentAppLanguage(): AppLanguage {
    val locales = LocalConfiguration.current.locales
    return AppLanguage.fromTag(locales.getFirstMatch(AppLanguage.supportedTags)?.language)
        ?: AppLanguage.ENGLISH
}
