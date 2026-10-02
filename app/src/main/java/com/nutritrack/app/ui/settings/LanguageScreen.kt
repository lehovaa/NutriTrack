package com.nutritrack.app.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.nutritrack.app.R
import com.nutritrack.app.model.AppLanguage
import com.nutritrack.app.ui.theme.NutriTrackTheme

/** Name of each language in that language itself, so users can always find their own. */
@get:StringRes
val AppLanguage.nativeNameRes: Int
    get() = when (this) {
        AppLanguage.ENGLISH -> R.string.language_english
        AppLanguage.RUSSIAN -> R.string.language_russian
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.profile_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .selectableGroup()
        ) {
            AppLanguage.entries.forEach { language ->
                val selected = language == currentLanguage
                ListItem(
                    headlineContent = { Text(stringResource(language.nativeNameRes)) },
                    leadingContent = { RadioButton(selected = selected, onClick = null) },
                    modifier = Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { if (!selected) onLanguageSelected(language) }
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LanguageScreenPreview() {
    NutriTrackTheme {
        LanguageScreen(currentLanguage = AppLanguage.ENGLISH, onLanguageSelected = {}, onBack = {})
    }
}
