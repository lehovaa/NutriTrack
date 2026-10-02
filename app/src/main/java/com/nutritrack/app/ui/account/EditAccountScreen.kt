package com.nutritrack.app.ui.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.model.Account
import com.nutritrack.app.ui.auth.AuthFormScaffold
import com.nutritrack.app.ui.auth.FormErrorMessage
import com.nutritrack.app.ui.auth.SubmitButton
import com.nutritrack.app.ui.auth.displayNameError
import com.nutritrack.app.ui.auth.messageRes
import com.nutritrack.app.ui.theme.NutriTrackTheme

/**
 * Edits the display name (Firebase) and the profile photo (device-local for now). The email is
 * read-only. Changes are applied together when the user taps Save Changes.
 */
@Composable
fun EditAccountScreen(
    account: Account,
    currentPhoto: Uri?,
    editState: AccountEditState,
    onSave: (displayName: String, photoEdit: PhotoEdit) -> Unit,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf(account.displayName.orEmpty()) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    var pickedPhoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    var photoRemoved by rememberSaveable { mutableStateOf(false) }
    val nameError = if (showErrors) displayNameError(name) else null
    val previewPhoto = when {
        pickedPhoto != null -> pickedPhoto
        photoRemoved -> null
        else -> currentPhoto
    }
    val enabled = !editState.isSaving

    // Android Photo Picker: no storage permission needed; backported to older versions.
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedPhoto = uri
            photoRemoved = false
        }
    }
    val openPicker = {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    LaunchedEffect(editState.saved) {
        if (editState.saved) onSaved()
    }

    AuthFormScaffold(title = R.string.edit_account_title, onBack = onBack, modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AccountAvatar(
                photo = previewPhoto,
                size = 96.dp,
                contentDescription = stringResource(R.string.edit_account_change_photo),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = enabled, role = Role.Button, onClick = openPicker)
            )
            Row {
                TextButton(onClick = openPicker, enabled = enabled) {
                    Text(stringResource(R.string.edit_account_change_photo))
                }
                if (previewPhoto != null) {
                    TextButton(
                        onClick = {
                            pickedPhoto = null
                            photoRemoved = true
                        },
                        enabled = enabled
                    ) {
                        Text(stringResource(R.string.edit_account_remove_photo))
                    }
                }
            }
            Text(
                text = stringResource(R.string.edit_account_photo_local_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.edit_account_display_name)) },
            isError = nameError != null,
            supportingText = nameError?.let { { Text(stringResource(it)) } },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = account.email.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.auth_email)) },
            trailingIcon = { VerificationStatusText(verified = account.emailVerified) },
            supportingText = { Text(stringResource(R.string.edit_account_email_read_only)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        editState.error?.let { FormErrorMessage(it.messageRes) }
        if (editState.photoFailed) FormErrorMessage(R.string.edit_account_photo_failed)
        Spacer(modifier = Modifier.height(4.dp))
        SubmitButton(text = R.string.profile_save_changes, isSubmitting = editState.isSaving) {
            showErrors = true
            if (displayNameError(name) == null) {
                val photoEdit = when {
                    pickedPhoto != null -> PhotoEdit.Replace(pickedPhoto!!)
                    photoRemoved -> PhotoEdit.Remove
                    else -> PhotoEdit.Unchanged
                }
                onSave(name, photoEdit)
            }
        }
    }
}

/** "Verified" / "Not verified" label for the account email. */
@Composable
fun VerificationStatusText(verified: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(
            if (verified) R.string.account_email_verified else R.string.account_email_not_verified
        ),
        style = MaterialTheme.typography.labelMedium,
        color = if (verified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun EditAccountScreenPreview() {
    NutriTrackTheme {
        EditAccountScreen(
            account = Account(
                uid = "preview",
                displayName = "Alex Smith",
                email = "alex@example.com",
                emailVerified = true
            ),
            currentPhoto = null,
            editState = AccountEditState(),
            onSave = { _, _ -> },
            onSaved = {},
            onBack = {}
        )
    }
}
