package com.farzane.securenote.domain.usecase

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<Resource<List<Note>>> {
        return repository.getAllNotes()
    }
}