package com.nutritrack.app.model

/**
 * The signed-in user's identity, as reported by the authentication provider (the source of truth).
 *
 * Deliberately separate from [NutritionProfile]. Never holds passwords or auth tokens;
 * those stay inside the authentication provider's SDK.
 */
data class Account(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val emailVerified: Boolean = false,
    /** Remote photo URL stored at the provider, if any. Local-only photos are not stored here. */
    val photoUrl: String? = null
)
