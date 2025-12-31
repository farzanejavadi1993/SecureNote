package com.farzane.securenote.data.repository

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class DesktopNoteExporter : NoteExporter {
    override suspend fun exportNotes(notes: List<Note>):
            Resource<String> = withContext(Dispatchers.IO) {
        val fileName = "notes_backup_${System.currentTimeMillis()}.txt"
        val content = buildString {
            append("--- My Secure Notes ---\n\n")
            notes.forEach { note ->
                append("Title: ${note.title}\n")
                append("Date: ${java.time.Instant.ofEpochMilli(note.timestamp)}\n")
                append("Content:\n${note.content}\n")
                append("---------------------------\n\n")
            }
        }

        val fileChooser = JFileChooser()
        fileChooser.dialogTitle = "Export Notes"
        fileChooser.selectedFile = File(fileName)
        fileChooser.fileFilter = FileNameExtensionFilter("Text Files", "txt")

        val userSelection = fileChooser.showSaveDialog(null)

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            var file = fileChooser.selectedFile
            if (!file.name.endsWith(".txt")) {
                file = File(file.absolutePath + ".txt")
            }

            try {
                file.writeText(content)
                return@withContext Resource.Success(
                    "Exported successfully to ${file.absolutePath}"
                )
            } catch (e: Exception) {
                return@withContext Resource.Error(
                    "Failed to write file: ${e.message}", e
                )
            }
        } else {
            return@withContext Resource.Error("Export cancelled")
        }
    }
}
