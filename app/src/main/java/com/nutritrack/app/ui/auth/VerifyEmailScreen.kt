package com.nutritrack.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.nutritrack.app.R
import com.nutritrack.app.ui.theme.NutriTrackTheme

/**
 * Shown to signed-in users whose email Firebase does not report as verified. Re-checks silently
 * whenever the screen resumes (e.g. after the user taps the link in their mail app).
 */
@Composable
fun VerifyEmailScreen(
    email: String,
    state: VerifyEmailState,
    onCheckVerified: (silent: Boolean) -> Unit,
    onResend: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    LifecycleResumeEffect(Unit) {
        onCheckVerified(true)
        onPauseOrDispose { }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.verify_email_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.verify_email_sent_to),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.verify_email_instructions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            val feedback = state.error?.messageRes ?: when (state.message) {
                VerifyEmailMessage.NOT_VERIFIED_YET -> R.string.verify_email_not_verified_yet
                VerifyEmailMessage.EMAIL_SENT -> R.string.verify_email_sent_confirmation
                null -> null
            }
            if (feedback != null) {
                Text(
                    text = stringResource(feedback),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.message == VerifyEmailMessage.EMAIL_SENT) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            SubmitButton(
                text = R.string.verify_email_check,
                isSubmitting = state.isChecking,
                onClick = { onCheckVerified(false) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onResend,
                enabled = !state.isResending && state.resendCooldownSeconds == 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.resendCooldownSeconds > 0) {
                        stringResource(R.string.verify_email_resend_in, state.resendCooldownSeconds)
                    } else {
                        stringResource(R.string.verify_email_resend)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.account_sign_out))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VerifyEmailScreenPreview() {
    NutriTrackTheme {
        VerifyEmailScreen(
            email = "alex@example.com",
            state = VerifyEmailState(message = VerifyEmailMessage.EMAIL_SENT, resendCooldownSeconds = 24),
            onCheckVerified = {},
            onResend = {},
            onSignOut = {}
        )
    }
}
