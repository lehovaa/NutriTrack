package com.nutritrack.app.ui.profile

import androidx.annotation.StringRes
import com.nutritrack.app.R
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.model.Sex
import com.nutritrack.app.model.ageInYears
import java.time.LocalDate

/** Unvalidated profile form input as the user entered it. */
internal data class ProfileForm(
    val dateOfBirth: LocalDate?,
    val sex: Sex?,
    val weightText: String,
    val heightText: String,
    val activityLevel: ActivityLevel?,
    val pregnant: Boolean,
    val breastfeeding: Boolean
) {
    fun validate(today: LocalDate) = ProfileErrors(
        dateOfBirth = when {
            dateOfBirth == null -> R.string.error_dob_required
            dateOfBirth.isAfter(today) -> R.string.error_dob_future
            ageInYears(dateOfBirth, today) !in NutritionProfile.SUPPORTED_AGE_RANGE ->
                R.string.error_dob_out_of_range
            else -> null
        },
        sex = if (sex == null) R.string.error_sex_required else null,
        weight = when {
            weightText.isBlank() -> R.string.error_weight_required
            parsePositiveDecimal(weightText) == null -> R.string.error_weight_invalid
            else -> null
        },
        height = when {
            heightText.isBlank() -> R.string.error_height_required
            parsePositiveDecimal(heightText) == null -> R.string.error_height_invalid
            else -> null
        },
        activityLevel = if (activityLevel == null) R.string.error_activity_required else null
    )

    fun toProfileOrNull(today: LocalDate): NutritionProfile? {
        if (validate(today).hasErrors) return null
        val isFemale = sex == Sex.FEMALE
        return NutritionProfile(
            dateOfBirth = dateOfBirth!!,
            sexForNutritionCalculations = sex!!,
            weightKg = parsePositiveDecimal(weightText)!!,
            heightCm = parsePositiveDecimal(heightText)!!,
            activityLevel = activityLevel!!,
            pregnant = isFemale && pregnant,
            breastfeeding = isFemale && breastfeeding
        )
    }
}

internal data class ProfileErrors(
    @get:StringRes val dateOfBirth: Int? = null,
    @get:StringRes val sex: Int? = null,
    @get:StringRes val weight: Int? = null,
    @get:StringRes val height: Int? = null,
    @get:StringRes val activityLevel: Int? = null
) {
    val hasErrors: Boolean
        get() = listOf(dateOfBirth, sex, weight, height, activityLevel).any { it != null }
}

/** Accepts "70", "70.5" or "70,5"; returns null for anything that is not a finite number > 0. */
internal fun parsePositiveDecimal(text: String): Double? =
    text.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
