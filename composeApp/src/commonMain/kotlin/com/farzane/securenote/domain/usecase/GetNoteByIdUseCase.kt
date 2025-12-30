package com.farzane.securenote.domain.usecase

import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository

class GetNoteByIdUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long): Note? {
        return repository.getNoteById(id)
    }
}
