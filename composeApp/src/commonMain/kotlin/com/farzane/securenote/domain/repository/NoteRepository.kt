package com.farzane.securenote.domain.repository

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<Resource<List<Note>>>
    suspend fun insertNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun getNoteById(id: Long): Note?
}
