package com.nutritrack.app.model

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageTest {

    @Test
    fun fromTagAcceptsLanguageAndRegionTags() {
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromTag("ru"))
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromTag("ru-RU"))
        assertEquals(AppLanguage.RUSSIAN, AppLanguage.fromTag("ru_UA"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en-US"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("EN"))
    }

    @Test
    fun fromTagReturnsNullForUnsupportedLanguages() {
        assertNull(AppLanguage.fromTag("de"))
        assertNull(AppLanguage.fromTag(""))
        assertNull(AppLanguage.fromTag(null))
    }

    @Test
    fun supportedTagsMatchResourceFolders() {
        assertArrayEquals(arrayOf("en", "ru"), AppLanguage.supportedTags)
    }
}
