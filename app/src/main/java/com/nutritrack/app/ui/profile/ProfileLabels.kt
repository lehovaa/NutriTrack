package com.nutritrack.app.ui.profile

import androidx.annotation.StringRes
import com.nutritrack.app.R
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.Sex
import java.text.DecimalFormat

@get:StringRes
val Sex.labelRes: Int
    get() = when (this) {
        Sex.FEMALE -> R.string.sex_female
        Sex.MALE -> R.string.sex_male
    }

@get:StringRes
val ActivityLevel.labelRes: Int
    get() = when (this) {
        ActivityLevel.INACTIVE -> R.string.activity_inactive
        ActivityLevel.LOW_ACTIVE -> R.string.activity_low_active
        ActivityLevel.ACTIVE -> R.string.activity_active
        ActivityLevel.VERY_ACTIVE -> R.string.activity_very_active
    }

@get:StringRes
val ActivityLevel.descriptionRes: Int
    get() = when (this) {
        ActivityLevel.INACTIVE -> R.string.activity_inactive_description
        ActivityLevel.LOW_ACTIVE -> R.string.activity_low_active_description
        ActivityLevel.ACTIVE -> R.string.activity_active_description
        ActivityLevel.VERY_ACTIVE -> R.string.activity_very_active_description
    }

/** Formats a measurement without trailing zeros, e.g. 70.0 -> "70", 70.25 -> "70.25". */
fun formatMeasurement(value: Double): String = DecimalFormat("0.##").format(value)
