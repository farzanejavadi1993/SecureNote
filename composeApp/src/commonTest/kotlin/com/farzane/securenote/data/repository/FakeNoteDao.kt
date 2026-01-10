package com.farzane.securenote.data.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.farzane.securenote.data.local.dao.NoteDao
import com.farzane.securenote.data.local.database.AppDatabase
import com.farzane.securenote.data.local.entity.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.test.BeforeTest

class FakeNoteDao : NoteDao {

    private lateinit var database : AppDatabase
    private val db = mutableMapOf<Long, NoteEntity>()
    var shouldThrowError = false

    override fun getAllNotes(): Flow<List<NoteEntity>> = flow {
        emit(db.values.toList())
    }

    override suspend fun insertNote(note: NoteEntity) {
        if (shouldThrowError) throw Exception("DB Error")
        db[note.id] = note
    }

    override suspend fun getNoteById(id: Long): NoteEntity? = db[id]

    override suspend fun deleteNoteById(id: Long) {
        if (shouldThrowError) throw Exception("DB Error")
        db.remove(id)
    }
}
