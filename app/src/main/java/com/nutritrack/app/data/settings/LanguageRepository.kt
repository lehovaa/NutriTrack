package com.nutritrack.app.data.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.nutritrack.app.model.AppLanguage

/**
 * App language preference. Separate from auth and NutritionProfile so it could later be synced
 * to the user's account without touching either.
 */
interface LanguageRepository {
    /** Applies [language] app-wide immediately and persists it across restarts. */
    fun setLanguage(language: AppLanguage)
}

/**
 * Uses Android's per-app locale API through AppCompat. On Android 13+ the system stores the
 * choice (and shows it in system settings); on older versions AppCompat stores it itself
 * (see the AppLocalesMetadataHolderService entry in the manifest).
 */
class AppCompatLanguageRepository : LanguageRepository {

    override fun setLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }
}
