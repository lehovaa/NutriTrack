package com.nutritrack.app.ui.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.model.ActivityLevel
import com.nutritrack.app.model.Sex
import com.nutritrack.app.model.NutritionProfile
import com.nutritrack.app.model.ageInYears
import com.nutritrack.app.ui.theme.NutriTrackTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Profile form used both for first-time setup ([initialProfile] == null) and for editing.
 * In setup mode there is no back arrow, since there is nothing usable to go back to yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    initialProfile: NutritionProfile?,
    onSave: (NutritionProfile) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isInitialSetup = initialProfile == null

    val today = remember { LocalDate.now() }

    var dateOfBirth by rememberSaveable { mutableStateOf(initialProfile?.dateOfBirth) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var sex by rememberSaveable { mutableStateOf(initialProfile?.sexForNutritionCalculations) }
    // Raw text is kept so the user can type freely; it is parsed only on validation.
    var weightText by rememberSaveable {
        mutableStateOf(initialProfile?.weightKg?.let(::formatMeasurement).orEmpty())
    }
    var heightText by rememberSaveable {
        mutableStateOf(initialProfile?.heightCm?.let(::formatMeasurement).orEmpty())
    }
    var activityLevel by rememberSaveable { mutableStateOf(initialProfile?.activityLevel) }
    var pregnant by rememberSaveable { mutableStateOf(initialProfile?.pregnant ?: false) }
    var breastfeeding by rememberSaveable { mutableStateOf(initialProfile?.breastfeeding ?: false) }
    // Errors are only shown after the first save attempt, then update live as the user fixes them.
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val form = ProfileForm(dateOfBirth, sex, weightText, heightText, activityLevel, pregnant, breastfeeding)
    val errors = if (showErrors) form.validate(today) else ProfileErrors()

    if (showDatePicker) {
        DateOfBirthPickerDialog(
            initialDate = dateOfBirth,
            today = today,
            onConfirm = { picked ->
                dateOfBirth = picked
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isInitialSetup) R.string.profile_setup_title else R.string.profile_title
                        )
                    )
                },
                navigationIcon = {
                    if (!isInitialSetup) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painter = painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.profile_back)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.profile_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            DateOfBirthField(
                dateOfBirth = dateOfBirth,
                today = today,
                error = errors.dateOfBirth,
                onClick = { showDatePicker = true }
            )

            FieldGroup(label = R.string.profile_sex_label, error = errors.sex) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    Sex.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = sex == option,
                            onClick = { sex = option },
                            shape = SegmentedButtonDefaults.itemShape(index, Sex.entries.size)
                        ) {
                            Text(stringResource(option.labelRes))
                        }
                    }
                }
            }

            NumberField(
                value = weightText,
                onValueChange = { weightText = it },
                label = R.string.profile_weight,
                suffix = R.string.unit_kg,
                error = errors.weight,
                keyboardType = KeyboardType.Decimal
            )

            NumberField(
                value = heightText,
                onValueChange = { heightText = it },
                label = R.string.profile_height,
                suffix = R.string.unit_cm,
                error = errors.height,
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            )

            FieldGroup(label = R.string.profile_activity_label, error = errors.activityLevel) {
                Column(modifier = Modifier.selectableGroup()) {
                    ActivityLevel.entries.forEach { option ->
                        RadioRow(
                            text = stringResource(option.labelRes),
                            description = stringResource(option.descriptionRes),
                            selected = activityLevel == option,
                            onClick = { activityLevel = option }
                        )
                    }
                }
            }

            if (sex == Sex.FEMALE) {
                FieldGroup(label = R.string.profile_female_options, error = null) {
                    SwitchRow(
                        text = stringResource(R.string.profile_pregnant),
                        checked = pregnant,
                        onCheckedChange = { pregnant = it }
                    )
                    SwitchRow(
                        text = stringResource(R.string.profile_breastfeeding),
                        checked = breastfeeding,
                        onCheckedChange = { breastfeeding = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showErrors = true
                    form.toProfileOrNull(today)?.let(onSave)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(
                        if (isInitialSetup) R.string.profile_save else R.string.profile_save_changes
                    )
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes label: Int,
    @StringRes suffix: Int,
    @StringRes error: Int?,
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        suffix = { Text(stringResource(suffix)) },
        isError = error != null,
        supportingText = error?.let { { Text(stringResource(it)) } },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun FieldGroup(
    @StringRes label: Int,
    @StringRes error: Int?,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.titleSmall
        )
        content()
        if (error != null) {
            Text(
                text = stringResource(error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun RadioRow(text: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(text = text, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Read-only field showing the chosen date; tapping it (or the icon) opens the date picker. */
@Composable
private fun DateOfBirthField(
    dateOfBirth: LocalDate?,
    today: LocalDate,
    @StringRes error: Int?,
    onClick: () -> Unit
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Release) currentOnClick()
        }
    }
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale)
    }
    val age = dateOfBirth?.takeIf { !it.isAfter(today) }?.let { ageInYears(it, today) }

    OutlinedTextField(
        value = dateOfBirth?.format(formatter).orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.profile_dob)) },
        placeholder = { Text(stringResource(R.string.profile_dob_placeholder)) },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = stringResource(R.string.profile_dob_select)
                )
            }
        },
        isError = error != null,
        supportingText = when {
            error != null -> { { Text(stringResource(error)) } }
            age != null -> { { Text(pluralStringResource(R.plurals.profile_age_years, age, age)) } }
            else -> null
        },
        singleLine = true,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPickerDialog(
    initialDate: LocalDate?,
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // DatePicker works in UTC-midnight milliseconds.
    val todayMillis = today.toUtcMillis()
    val earliest = today.minusYears(NutritionProfile.SUPPORTED_AGE_RANGE.last.toLong())
    val earliestMillis = earliest.toUtcMillis()
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.toUtcMillis(),
        yearRange = earliest.year..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) =
                utcTimeMillis in earliestMillis..todayMillis

            override fun isSelectableYear(year: Int) = year in earliest.year..today.year
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onConfirm(it.utcMillisToLocalDate()) } },
                enabled = state.selectedDateMillis != null
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.utcMillisToLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
private fun SwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileSetupPreview() {
    NutriTrackTheme {
        ProfileScreen(initialProfile = null, onSave = {}, onBack = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileEditPreview() {
    NutriTrackTheme {
        ProfileScreen(
            initialProfile = NutritionProfile(
                dateOfBirth = LocalDate.of(1994, 6, 14),
                sexForNutritionCalculations = Sex.FEMALE,
                weightKg = 62.5,
                heightCm = 168.0,
                activityLevel = ActivityLevel.ACTIVE
            ),
            onSave = {},
            onBack = {}
        )
    }
}
