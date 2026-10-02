package com.nutritrack.app.ui.profile

import com.nutritrack.app.R
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.model.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ProfileFormTest {

    private val today = LocalDate.of(2026, 9, 29)

    private val validForm = ProfileForm(
        dateOfBirth = LocalDate.of(1974, 6, 14),
        sex = Sex.FEMALE,
        weightText = "62,5",
        heightText = "168",
        activityLevel = ActivityLevel.LOW_ACTIVE,
        pregnant = true,
        breastfeeding = false
    )

    @Test
    fun validFormProducesNutritionProfile() {
        assertFalse(validForm.validate(today).hasErrors)
        assertEquals(
            NutritionProfile(
                dateOfBirth = LocalDate.of(1974, 6, 14),
                sexForNutritionCalculations = Sex.FEMALE,
                weightKg = 62.5,
                heightCm = 168.0,
                activityLevel = ActivityLevel.LOW_ACTIVE,
                pregnant = true,
                breastfeeding = false
            ),
            validForm.toProfileOrNull(today)
        )
    }

    @Test
    fun maleProfileNeverStoresPregnancyFlags() {
        val profile = validForm.copy(sex = Sex.MALE, breastfeeding = true).toProfileOrNull(today)!!
        assertFalse(profile.pregnant)
        assertFalse(profile.breastfeeding)
    }

    @Test
    fun missingDateOfBirthIsRejected() {
        val form = validForm.copy(dateOfBirth = null)
        assertEquals(R.string.error_dob_required, form.validate(today).dateOfBirth)
        assertNull(form.toProfileOrNull(today))
    }

    @Test
    fun futureDateOfBirthIsRejected() {
        val form = validForm.copy(dateOfBirth = today.plusDays(1))
        assertEquals(R.string.error_dob_future, form.validate(today).dateOfBirth)
        assertNull(form.toProfileOrNull(today))
    }

    @Test
    fun agesOutsideSupportedRangeAreRejected() {
        val tooYoung = validForm.copy(dateOfBirth = today.minusYears(1).plusDays(1))
        val tooOld = validForm.copy(dateOfBirth = today.minusYears(121))
        assertEquals(R.string.error_dob_out_of_range, tooYoung.validate(today).dateOfBirth)
        assertEquals(R.string.error_dob_out_of_range, tooOld.validate(today).dateOfBirth)
        assertNull(validForm.copy(dateOfBirth = today.minusYears(1)).validate(today).dateOfBirth)
        assertNull(validForm.copy(dateOfBirth = today.minusYears(120)).validate(today).dateOfBirth)
    }
}
