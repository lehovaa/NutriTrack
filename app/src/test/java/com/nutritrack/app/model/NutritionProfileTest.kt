package com.nutritrack.app.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class NutritionProfileTest {

    private val dob = LocalDate.of(1974, 6, 14)

    @Test
    fun ageIncreasesExactlyOnTheBirthday() {
        assertEquals(51, ageInYears(dob, LocalDate.of(2026, 6, 13)))
        assertEquals(52, ageInYears(dob, LocalDate.of(2026, 6, 14)))
        assertEquals(52, ageInYears(dob, LocalDate.of(2026, 6, 15)))
        assertEquals(52, ageInYears(dob, LocalDate.of(2026, 9, 29)))
    }

    @Test
    fun ageAcrossYearBoundary() {
        val newYearsEve = LocalDate.of(2000, 12, 31)
        assertEquals(25, ageInYears(newYearsEve, LocalDate.of(2026, 12, 30)))
        assertEquals(26, ageInYears(newYearsEve, LocalDate.of(2026, 12, 31)))
        assertEquals(26, ageInYears(newYearsEve, LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun leapDayBirthdayIsReachedOnFirstOfMarchInNonLeapYears() {
        val leapDay = LocalDate.of(2000, 2, 29)
        assertEquals(24, ageInYears(leapDay, LocalDate.of(2025, 2, 28)))
        assertEquals(25, ageInYears(leapDay, LocalDate.of(2025, 3, 1)))
        assertEquals(27, ageInYears(leapDay, LocalDate.of(2028, 2, 28)))
        assertEquals(28, ageInYears(leapDay, LocalDate.of(2028, 2, 29)))
    }

    @Test
    fun ageIsZeroOnTheDayOfBirth() {
        assertEquals(0, ageInYears(dob, dob))
    }

    @Test(expected = IllegalArgumentException::class)
    fun futureDateOfBirthIsRejected() {
        ageInYears(LocalDate.of(2030, 1, 1), LocalDate.of(2026, 9, 29))
    }

    @Test
    fun currentAgeUsesTheGivenClock() {
        val profile = NutritionProfile(
            dateOfBirth = dob,
            sexForNutritionCalculations = Sex.FEMALE,
            weightKg = 60.0,
            heightCm = 165.0,
            activityLevel = ActivityLevel.ACTIVE
        )
        val dayBefore = Clock.fixed(
            LocalDate.of(2026, 6, 13).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC
        )
        val birthday = Clock.fixed(
            LocalDate.of(2026, 6, 14).atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC
        )
        assertEquals(51, profile.currentAge(dayBefore))
        assertEquals(52, profile.currentAge(birthday))
    }
}
