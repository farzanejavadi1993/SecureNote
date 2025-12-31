package com.farzane.securenote.data.local.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.data.repository.NoteRepositoryImpl
import com.farzane.securenote.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: NoteRepositoryImpl

    @BeforeTest
    fun setUp() {
        // 1. Create an In-Memory Database
        // This simulates the full database logic but runs fast in RAM.
        val builder = Room.inMemoryDatabaseBuilder<AppDatabase>()

        database = builder
            .setDriver(BundledSQLiteDriver()) // Required for KMP Room
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

        // 2. Initialize the real Repository with the real DAO
        repository = NoteRepositoryImpl(database.noteDao())
    }

    @AfterTest
    fun tearDown() {
        // Always close the DB after each test
        database.close()
    }

    @Test
    fun `verifies data persistence (Insert and Read)`() = runTest {
        // Arrange
        val note =
            Note(id = null, title = "Integration Test", content = "Persist Me", timestamp = 123L)

        // Act: Insert via Repository
        val insertResult = repository.insertNote(note)

        // Assert: Check insert result
        assertTrue(insertResult is Resource.Success, "Insert should succeed")

        // Act: Retrieve via Repository
        val resultResource = repository.getAllNotes().first()

        // Assert: Verify data was actually saved to SQLite
        assertTrue(resultResource is Resource.Success)
        val savedNotes = resultResource.data
        assertEquals(1, savedNotes.size)
        assertEquals("Integration Test", savedNotes[0].title)
    }

    @Test
    fun `verifies notes are sorted by Newest First (SQL Order By)`() = runTest {
        // This test proves your @Query("... ORDER BY timestamp DESC") is correct.

        // Arrange: Insert notes with timestamps out of order
        val oldNote = Note(id = null, title = "Old", content = "...", timestamp = 1000L)
        val newNote = Note(id = null, title = "New", content = "...", timestamp = 9000L)
        val midNote = Note(id = null, title = "Mid", content = "...", timestamp = 5000L)

        repository.insertNote(oldNote)
        repository.insertNote(newNote)
        repository.insertNote(midNote)

        // Act
        val result = repository.getAllNotes().first()

        // Assert
        assertTrue(result is Resource.Success)
        val list = result.data

        // 9000 -> 5000 -> 1000
        assertEquals(3, list.size)
        assertEquals(
            "New",
            list[0].title,
            "First note should be the newest"
        )
        assertEquals(
            "Mid",
            list[1].title,
            "Second note should be the middle one"
        )
        assertEquals(
            "Old",
            list[2].title,
            "Last note should be the oldest"
        )
    }

    @Test
    fun `verifies deletion removes data from real database`() = runTest {
        // Arrange
        val note = Note(id = null, title = "Delete Me", content = "...", timestamp = 100L)
        repository.insertNote(note)

        // We need to fetch it to get the auto-generated ID
        val savedList = (repository.getAllNotes().first() as Resource.Success).data
        val idToDelete = savedList.first().id!!

        // Act
        val deleteResult = repository.deleteNote(idToDelete)

        // Assert
        assertTrue(deleteResult is Resource.Success)

        val finalList = (repository.getAllNotes().first() as Resource.Success).data
        assertTrue(finalList.isEmpty(), "Database should be empty after delete")
    }

    @Test
    fun `verifies getting a single note by ID`() = runTest {
        // Arrange
        val note = Note(id = null, title = "Find Me", content = "...", timestamp = 1L)
        repository.insertNote(note)

        // Get the real ID
        val list = (repository.getAllNotes().first() as Resource.Success).data
        val generatedId = list.first().id!!

        // Act
        val fetchedNote = repository.getNoteById(generatedId)

        // Assert
        assertTrue(fetchedNote != null)
        assertEquals("Find Me", fetchedNote.title)
    }
}
