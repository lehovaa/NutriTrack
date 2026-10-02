package com.nutritrack.app.ui.auth

import com.nutritrack.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidationTest {

    @Test
    fun emailShapeCheck() {
        assertTrue(isValidLookingEmail("alex@example.com"))
        assertTrue(isValidLookingEmail("  alex.smith+test@mail.example.org  "))
        assertFalse(isValidLookingEmail("alex"))
        assertFalse(isValidLookingEmail("alex@example"))
        assertFalse(isValidLookingEmail("alex @example.com"))
        assertFalse(isValidLookingEmail("@example.com"))
    }

    @Test
    fun validCreateAccountFormHasNoErrors() {
        assertFalse(validateCreateAccount("Alex", "alex@example.com", "secret1", "secret1").hasErrors)
    }

    @Test
    fun createAccountRequiresEveryField() {
        val errors = validateCreateAccount("   ", "", "", "")
        assertEquals(R.string.auth_error_name_required, errors.name)
        assertEquals(R.string.auth_error_email_required, errors.email)
        assertEquals(R.string.auth_error_password_required, errors.password)
        assertEquals(R.string.auth_error_confirm_password_required, errors.confirmPassword)
    }

    @Test
    fun createAccountRejectsMismatchedPasswordsAndBadEmail() {
        val errors = validateCreateAccount("Alex", "alex@", "secret1", "secret2")
        assertEquals(R.string.auth_error_email_invalid, errors.email)
        assertNull(errors.password)
        assertEquals(R.string.auth_error_passwords_do_not_match, errors.confirmPassword)
    }

    @Test
    fun signInRequiresEmailAndPassword() {
        val errors = validateSignIn("", "")
        assertEquals(R.string.auth_error_email_required, errors.email)
        assertEquals(R.string.auth_error_password_required, errors.password)
        assertFalse(validateSignIn("alex@example.com", "x").hasErrors)
    }
}
