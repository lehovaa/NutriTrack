package com.nutritrack.app.ui.account

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.model.Account
import com.nutritrack.app.model.AppLanguage
import com.nutritrack.app.ui.settings.nativeNameRes
import com.nutritrack.app.ui.theme.NutriTrackTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    account: Account,
    photo: Uri?,
    currentLanguage: AppLanguage,
    onEditAccountClick: () -> Unit,
    onEditNutritionProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_title)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            AccountAvatar(photo = photo, size = 88.dp)
            Text(
                text = account.displayName?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.account_no_name),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            account.email?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            VerificationStatusText(verified = account.emailVerified)
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    title = stringResource(R.string.account_edit_account),
                    onClick = onEditAccountClick
                )
                SettingsRow(
                    title = stringResource(R.string.account_edit_nutrition_profile),
                    onClick = onEditNutritionProfileClick
                )
                SettingsRow(
                    title = stringResource(R.string.language_title),
                    value = stringResource(currentLanguage.nativeNameRes),
                    onClick = onLanguageClick
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onSignOutClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.account_sign_out))
            }
        }
    }
}

/** Tappable settings-style row: title on the left, optional current value and a chevron on the right. */
@Composable
private fun SettingsRow(title: String, onClick: () -> Unit, value: String? = null) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null
                )
            }
        },
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
private fun AccountScreenPreview() {
    NutriTrackTheme {
        AccountScreen(
            account = Account(
                uid = "preview",
                displayName = "Alex Smith",
                email = "alex@example.com",
                emailVerified = true
            ),
            photo = null,
            currentLanguage = AppLanguage.ENGLISH,
            onEditAccountClick = {},
            onEditNutritionProfileClick = {},
            onLanguageClick = {},
            onSignOutClick = {},
            onBack = {}
        )
    }
}
