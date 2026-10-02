package com.nutritrack.app.data.photo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Profile photo per account. The current implementation is device-local; a cloud-backed
 * implementation (upload + Firebase Auth photoUrl) can replace it without touching the UI.
 */
interface ProfilePhotoRepository {
    /** The account's photo as a displayable Uri, or null if none is set. */
    fun observePhoto(uid: String): Flow<Uri?>

    /** Copies [source] (e.g. a Photo Picker Uri) and makes it the account's photo. */
    suspend fun setPhoto(uid: String, source: Uri)

    suspend fun removePhoto(uid: String)
}

/**
 * Stores the photo in app-private storage on this device only. It is NOT synced across devices
 * and is excluded from Android backups (see backup_rules.xml / data_extraction_rules.xml).
 */
class LocalProfilePhotoRepository(private val context: Context) : ProfilePhotoRepository {

    private val directory = File(context.filesDir, DIRECTORY)

    /** uid -> current photo file (null = checked, none set). Absent key = not loaded yet. */
    private val photos = MutableStateFlow<Map<String, File?>>(emptyMap())

    override fun observePhoto(uid: String): Flow<Uri?> =
        photos
            .onStart { if (uid !in photos.value) load(uid) }
            .map { it[uid]?.let(Uri::fromFile) }

    override suspend fun setPhoto(uid: String, source: Uri) {
        val file = withContext(Dispatchers.IO) {
            directory.mkdirs()
            // A new name per photo so image caches never show the previous picture.
            val target = File(directory, "${fileKey(uid)}_${System.currentTimeMillis()}")
            val input = context.contentResolver.openInputStream(source)
                ?: error("Cannot open selected image")
            input.use { stream -> target.outputStream().use { stream.copyTo(it) } }
            filesFor(uid).filter { it != target }.forEach(File::delete)
            target
        }
        photos.update { it + (uid to file) }
    }

    override suspend fun removePhoto(uid: String) {
        withContext(Dispatchers.IO) { filesFor(uid).forEach(File::delete) }
        photos.update { it + (uid to null) }
    }

    private suspend fun load(uid: String) {
        val file = withContext(Dispatchers.IO) { filesFor(uid).maxByOrNull(File::lastModified) }
        photos.update { if (uid in it) it else it + (uid to file) }
    }

    private fun filesFor(uid: String): List<File> {
        val prefix = "${fileKey(uid)}_"
        return directory.listFiles()?.filter { it.name.startsWith(prefix) }.orEmpty()
    }

    // Firebase uids are alphanumeric, but never trust input used in a file name.
    private fun fileKey(uid: String) = uid.replace(Regex("[^A-Za-z0-9]"), "_")

    private companion object {
        const val DIRECTORY = "profile_photos"
    }
}
