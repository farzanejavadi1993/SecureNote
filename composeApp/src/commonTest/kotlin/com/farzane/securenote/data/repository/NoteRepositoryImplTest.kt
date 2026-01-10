package com.farzane.securenote.data.repository

import app.cash.turbine.test
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.data.local.dao.NoteDao
import com.farzane.securenote.data.local.entity.NoteEntity
import com.farzane.securenote.data.mapper.toEntity
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for [NoteRepositoryImpl].
 * Focuses on:
 * 1. Data flow from DAO to Domain.
 * 2. Automated Encryption/Decryption logic.
 * 3. Error handling mapping to [Resource.Error].
 */
class NoteRepositoryTest {

    private lateinit var repository: NoteRepository
    private lateinit var fakeDao: FakeNoteDao

    @BeforeTest
    fun setup() {
        fakeDao = FakeNoteDao()
        repository = NoteRepositoryImpl(fakeDao)
    }

    //run Coroutine block in TestDispatcher
    @Test
    fun `getAllNotes should decrypt data retrieved from DAO`() = runTest {
        // GIVEN: An encrypted note exists in the "database"
        // (Note: In your logic, encryptDecrypt is a toggle. XORing twice returns original)
        val rawTitle = "Secret"
        val noteEntity = Note(
            id = 1,
            title = rawTitle,
            content = "Body",
            timestamp = 123L).toEntity()

        fakeDao.insertNote(noteEntity)

        // WHEN: Retrieving notes
        val result = repository.getAllNotes().first()

        // THEN: Result is Success and Title is Decrypted
        assertTrue(result is Resource.Success)
        // Since EncryptionHelper.encryptDecrypt is called in Repo,
        // the title should be different from raw if our logic works.
        assertTrue(result.data[0].title != rawTitle)
    }

    @Test
    fun `addNote should encrypt data before passing to DAO`() = runTest {
        // GIVEN: A domain note
        val note = Note(id = 1, title = "My Title", content = "My Content", timestamp = 123L)

        // WHEN: Adding the note
        repository.addNote(note)

        // THEN: The version saved in the DAO must be encrypted
        val savedEntity = fakeDao.getNoteById(1)
        assertTrue(savedEntity?.title != "My Title")
    }

    @Test
    fun `getNoteById returns null when note does not exist`() = runTest {
        // WHEN: Requesting non-existent ID
        val result = repository.getNoteById(999)

        // THEN: Result is null
        assertEquals(null, result)
    }

    @Test
    fun `deleteNote handles exceptions and returns Resource Error`() =
        runTest {
        // GIVEN: A DAO that throws an exception
        fakeDao.shouldThrowError = true

        // WHEN: Deleting
        val result = repository.deleteNote(1)

        // THEN: Return Resource.Error instead of crashing
        assertTrue(result is Resource.Error)
        assertTrue(result.message.contains("Could not delete"))
    }
}

