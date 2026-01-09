package com.farzane.securenote.domain.usecase

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository

class AddNoteUseCase(private val repository: NoteRepository) {

    suspend operator fun invoke(
        id: Long? = null,
        title: String,
        content: String,
        ): Resource<Unit> {

        if (title.isBlank() || content.isBlank()) {
            return Resource.Error("Title and content cannot be empty")
        }
        val note = Note(
            id = id,
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        return repository.addNote(note)
    }
}
