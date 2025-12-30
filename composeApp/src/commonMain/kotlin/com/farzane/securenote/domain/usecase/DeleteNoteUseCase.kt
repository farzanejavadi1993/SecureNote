package com.farzane.securenote.domain.usecase

import com.farzane.securenote.domain.repository.NoteRepository
class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteNote(id)
    }
}
