package com.nutritrack.app.data.profile

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.model.Sex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate

class DataStoreNutritionProfileRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var repository: DataStoreNutritionProfileRepository

    private val alice = NutritionProfile(
        dateOfBirth = LocalDate.of(1990, 3, 8),
        sexForNutritionCalculations = Sex.FEMALE,
        weightKg = 62.5,
        heightCm = 168.0,
        activityLevel = ActivityLevel.LOW_ACTIVE,
        pregnant = true,
        breastfeeding = false
    )

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val store = PreferenceDataStoreFactory.create(scope = scope) {
            folder.newFile("profiles.preferences_pb").also { it.delete() }
        }
        repository = DataStoreNutritionProfileRepository(store)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun noProfileUntilSaved() = runBlocking {
        assertNull(repository.observeProfile("uid-a").first())
    }

    @Test
    fun savedProfileRoundTripsAllFields() = runBlocking {
        repository.saveProfile("uid-a", alice)
        assertEquals(alice, repository.observeProfile("uid-a").first())
    }

    @Test
    fun accountsNeverSeeEachOthersProfile() = runBlocking {
        repository.saveProfile("uid-a", alice)
        assertNull(repository.observeProfile("uid-b").first())

        val bob = alice.copy(sexForNutritionCalculations = Sex.MALE, pregnant = false, weightKg = 80.0)
        repository.saveProfile("uid-b", bob)
        assertEquals(alice, repository.observeProfile("uid-a").first())
        assertEquals(bob, repository.observeProfile("uid-b").first())
    }

    @Test
    fun savingAgainReplacesTheProfile() = runBlocking {
        repository.saveProfile("uid-a", alice)
        val updated = alice.copy(weightKg = 60.0, activityLevel = ActivityLevel.VERY_ACTIVE)
        repository.saveProfile("uid-a", updated)
        assertEquals(updated, repository.observeProfile("uid-a").first())
    }
}
