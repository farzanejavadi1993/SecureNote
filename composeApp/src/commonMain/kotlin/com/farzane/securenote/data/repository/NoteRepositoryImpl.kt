package com.farzane.securenote.data.repository

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.data.local.dao.NoteDao
import com.farzane.securenote.data.mapper.toDomain
import com.farzane.securenote.data.mapper.toEntity
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.collections.map

class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<Resource<List<Note>>> {
        return dao.getAllNotes()
            .map { entities ->
                val notes = entities.map { it.toDomain() }
                Resource.Success(notes) as Resource<List<Note>>
            }
            .catch { e ->
                emit(Resource.Error("Failed to load notes: ${e.message}", e))
            }
    }

    override suspend fun insertNote(note: Note): Resource<Unit> {
        return try {
            dao.insertNote(note.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Could not insert note: ${e.message}", e)
        }
    }

    override suspend fun deleteNote(id: Long): Resource<Unit> {
        return try {
            dao.deleteNoteById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Could not delete note: ${e.message}", e)
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return try {
            dao.getNoteById(id)?.toDomain()
        } catch (e: Exception) {
            null
        }
    }
}
