package com.farzane.securenote.domain.repository

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note

interface NoteExporter {
    suspend fun exportNotes(notes: List<Note>): Resource<String> // Returns a status message
}
