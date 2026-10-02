package com.nutritrack.app.model

/** Languages the app UI is translated into. [tag] is the BCP 47 language tag. */
enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    RUSSIAN("ru");

    companion object {
        val supportedTags: Array<String> = entries.map { it.tag }.toTypedArray()

        /** Maps a locale tag such as "ru" or "ru-RU" to a supported language, or null. */
        fun fromTag(tag: String?): AppLanguage? {
            val language = tag?.substringBefore('-')?.substringBefore('_')?.lowercase() ?: return null
            return entries.firstOrNull { it.tag == language }
        }
    }
}
