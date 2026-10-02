package com.nutritrack.app.data.auth

import com.nutritrack.app.BuildConfig
import com.nutritrack.app.model.Account

object AuthPolicy {
    /**
     * DEBUG ONLY: email verification gate is bypassed while developing the main application UI.
     * Release builds always require a verified email. Verification emails and the Verify Email
     * screen are unchanged; set this back to `true` to restore the gate in debug builds too.
     */
    val requireEmailVerification: Boolean = !BuildConfig.DEBUG

    /** Whether [account] may enter the main app, or must verify its email first. */
    fun canEnterApp(account: Account): Boolean = account.emailVerified || !requireEmailVerification
}
