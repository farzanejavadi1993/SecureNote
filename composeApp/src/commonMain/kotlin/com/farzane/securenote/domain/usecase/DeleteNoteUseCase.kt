package com.farzane.securenote.domain.usecase

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.repository.NoteRepository
class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long): Resource<Unit> {
        return repository.deleteNote(id)
    }
}