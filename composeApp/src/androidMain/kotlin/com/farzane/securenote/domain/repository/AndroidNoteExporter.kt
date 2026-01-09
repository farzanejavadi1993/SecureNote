package com.farzane.securenote.domain.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AndroidNoteExporter(private val context: Context) : NoteExporter {
    override suspend fun exportNotes(notes: List<Note>): Resource<String> =
        withContext(Dispatchers.IO) {
            val fileName = "notes_backup_${System.currentTimeMillis()}.txt"


            val content = buildString {
                append("--- My Secure Notes ---\n\n")
                notes.forEach { note ->
                    append("Title: ${note.title}\n")
                    append("Content:\n${note.content}\n")
                    append("---------------------------\n\n")
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri = resolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    ) ?: return@withContext Resource.Error(
                        "Failed to create file in Downloads"
                    )


                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    return@withContext Resource.Success("Saved to Downloads folder")

                } else {
                    val downloadsDir =
                        Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                    val file = File(downloadsDir, fileName)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    return@withContext Resource.Success("Saved to ${file.absolutePath}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext Resource.Error(
                    "Export failed: ${e.message}",
                    e
                )
            }
        }
}