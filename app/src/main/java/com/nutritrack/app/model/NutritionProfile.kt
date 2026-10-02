package com.nutritrack.app.model

import java.time.Clock
import java.time.LocalDate
import java.time.Period

enum class Sex { FEMALE, MALE }

/**
 * Physical activity level categories from the DRI Energy framework (PAL categories).
 *
 * The user-facing descriptions are qualitative guidance only. Any future calorie/EER calculation
 * must use the official age- and sex-specific DRI equations for the selected category, not
 * derive numbers from the descriptions. PAL values belong in the calculation layer, not the UI.
 */
enum class ActivityLevel {
    INACTIVE,
    LOW_ACTIVE,
    ACTIVE,
    VERY_ACTIVE
}

/**
 * Biological information used for nutrition reference calculations.
 *
 * Account data (email, credentials, auth user ID, language) intentionally does not belong here;
 * it will live in a separate account/settings layer.
 */
data class NutritionProfile(
    val dateOfBirth: LocalDate,
    val sexForNutritionCalculations: Sex,
    val weightKg: Double,
    val heightCm: Double,
    val activityLevel: ActivityLevel,
    // Only meaningful for FEMALE; always false otherwise.
    val pregnant: Boolean = false,
    val breastfeeding: Boolean = false
) {
    /** Age in completed years on [date]. Derived on demand; never stored. */
    fun ageOn(date: LocalDate): Int = ageInYears(dateOfBirth, date)

    /** Age in completed years today, according to [clock]. */
    fun currentAge(clock: Clock = Clock.systemDefaultZone()): Int = ageOn(LocalDate.now(clock))

    companion object {
        val SUPPORTED_AGE_RANGE = 1..120
    }
}

/**
 * Completed years between [dateOfBirth] and [today]; the age only increases on the birthday itself.
 * A 29 February birthday counts as reached on 1 March in non-leap years.
 */
fun ageInYears(dateOfBirth: LocalDate, today: LocalDate): Int {
    require(!dateOfBirth.isAfter(today)) { "dateOfBirth $dateOfBirth is after $today" }
    return Period.between(dateOfBirth, today).years
}
