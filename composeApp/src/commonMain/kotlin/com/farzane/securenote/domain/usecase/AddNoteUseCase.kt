package com.farzane.securenote.domain.usecase

import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke( id: Long? = null,title: String, content: String) {
        if (title.isBlank() && content.isBlank()) {
            return // Don't save empty notes
        }
        val note = Note(
            id = id,
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        repository.insertNote(note)
    }
}