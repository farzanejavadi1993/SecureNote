package com.farzane.securenote.data.repository

import com.farzane.securenote.core.util.EncryptionHelper
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
                val notes = entities.map { entity ->
                    val domainNote = entity.toDomain()
                    // 1. DECRYPT: Database -> Domain
                    domainNote.copy(
                        title = EncryptionHelper.encryptDecrypt(domainNote.title),
                        content = EncryptionHelper.encryptDecrypt(domainNote.content)
                    )
                }
                Resource.Success(notes) as Resource<List<Note>>
            }
            .catch { e ->
                emit(Resource.Error("Failed to load notes: ${e.message}", e))
            }
    }

    override suspend fun insertNote(note: Note): Resource<Unit> {
        return try {
            // 2. ENCRYPT: Domain -> Database
            val encryptedNote = note.copy(
                title = EncryptionHelper.encryptDecrypt(note.title),
                content = EncryptionHelper.encryptDecrypt(note.content)
            )

            dao.insertNote(encryptedNote.toEntity())
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
            val entity = dao.getNoteById(id)
            entity?.let {
                val domainNote = it.toDomain()
                // 3. DECRYPT Single Note
                domainNote.copy(
                    title = EncryptionHelper.encryptDecrypt(domainNote.title),
                    content = EncryptionHelper.encryptDecrypt(domainNote.content)
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}