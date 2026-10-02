package com.nutritrack.app.data.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.model.Sex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** NutritionProfile storage, one profile per authenticated account (Firebase uid). */
interface NutritionProfileRepository {
    /** The saved profile for [uid], or null if that account has none yet. */
    fun observeProfile(uid: String): Flow<NutritionProfile?>

    suspend fun saveProfile(uid: String, profile: NutritionProfile)
}

private val Context.nutritionProfileStore: DataStore<Preferences> by preferencesDataStore(
    name = "nutrition_profiles"
)

/** The app's single profile store (file: files/datastore/nutrition_profiles.preferences_pb). */
fun Context.nutritionProfileDataStore(): DataStore<Preferences> = applicationContext.nutritionProfileStore

/**
 * LOCAL-ONLY persistence (DataStore Preferences) on this device. Not synced between devices and
 * excluded from Android backups; cloud sync comes later. Keys are prefixed with the uid, so each
 * account only ever reads its own profile. Signing out leaves the saved profile in place.
 */
class DataStoreNutritionProfileRepository(
    private val store: DataStore<Preferences>
) : NutritionProfileRepository {

    override fun observeProfile(uid: String): Flow<NutritionProfile?> {
        val keys = Keys(uid)
        return store.data
            .map { prefs -> keys.read(prefs) }
            .distinctUntilChanged()
    }

    override suspend fun saveProfile(uid: String, profile: NutritionProfile) {
        val keys = Keys(uid)
        store.edit { prefs ->
            prefs[keys.dateOfBirth] = profile.dateOfBirth.toEpochDay()
            prefs[keys.sex] = profile.sexForNutritionCalculations.name
            prefs[keys.weightKg] = profile.weightKg
            prefs[keys.heightCm] = profile.heightCm
            prefs[keys.activityLevel] = profile.activityLevel.name
            prefs[keys.pregnant] = profile.pregnant
            prefs[keys.breastfeeding] = profile.breastfeeding
        }
    }

    private class Keys(uid: String) {
        private val prefix = "profile.$uid."
        val dateOfBirth = longPreferencesKey(prefix + "dateOfBirth")
        val sex = stringPreferencesKey(prefix + "sexForNutritionCalculations")
        val weightKg = doublePreferencesKey(prefix + "weightKg")
        val heightCm = doublePreferencesKey(prefix + "heightCm")
        val activityLevel = stringPreferencesKey(prefix + "activityLevel")
        val pregnant = booleanPreferencesKey(prefix + "pregnant")
        val breastfeeding = booleanPreferencesKey(prefix + "breastfeeding")

        /** Returns null if any required field is missing or no longer valid (e.g. a renamed enum). */
        fun read(prefs: Preferences): NutritionProfile? {
            val sex = prefs[sex]?.let { name -> Sex.entries.firstOrNull { it.name == name } }
            val activity = prefs[activityLevel]?.let { name ->
                ActivityLevel.entries.firstOrNull { it.name == name }
            }
            return NutritionProfile(
                dateOfBirth = LocalDate.ofEpochDay(prefs[dateOfBirth] ?: return null),
                sexForNutritionCalculations = sex ?: return null,
                weightKg = prefs[weightKg] ?: return null,
                heightCm = prefs[heightCm] ?: return null,
                activityLevel = activity ?: return null,
                pregnant = prefs[pregnant] ?: false,
                breastfeeding = prefs[breastfeeding] ?: false
            )
        }
    }
}
