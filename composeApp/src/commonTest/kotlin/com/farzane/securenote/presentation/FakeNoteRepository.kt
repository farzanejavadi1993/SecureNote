package com.farzane.securenote.presentation

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNoteRepository : NoteRepository {

    // In-memory data store
    private val notesMap = MutableStateFlow<Map<Long, Note>>(emptyMap())

    override fun getAllNotes(): Flow<Resource<List<Note>>> {
        return notesMap.map { Resource.Success(it.values.toList().reversed()) }
    }

    override suspend fun addNote(note: Note): Resource<Unit> {
        val id = note.id ?: ((notesMap.value.keys.maxOrNull() ?: 0L) + 1L)
        val noteWithId = note.copy(id = id)
        
        val currentMap = notesMap.value.toMutableMap()
        currentMap[id] = noteWithId
        notesMap.value = currentMap
        
        return Resource.Success(Unit)
    }

    override suspend fun deleteNote(id: Long): Resource<Unit> {
        val currentMap = notesMap.value.toMutableMap()
        currentMap.remove(id)
        notesMap.value = currentMap
        return Resource.Success(Unit)
    }

    override suspend fun getNoteById(id: Long): Note? {
        return notesMap.value[id]
    }

    // Helper method for tests to seed data
    fun addNoteSync(note: Note) {
        val currentMap = notesMap.value.toMutableMap()
        val id = note.id ?: 1L
        currentMap[id] = note
        notesMap.value = currentMap
    }
}
