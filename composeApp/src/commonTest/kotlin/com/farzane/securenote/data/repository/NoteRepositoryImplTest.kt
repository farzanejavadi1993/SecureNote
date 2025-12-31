package com.farzane.securenote.data.repository

import app.cash.turbine.test
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.data.local.dao.NoteDao
import com.farzane.securenote.data.local.entity.NoteEntity
import com.farzane.securenote.domain.model.Note
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.collections.get
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteRepositoryImplTest {

    // Mock the DAO because we're testing the Repository, not the database itself.
    private val dao: NoteDao = mockk()
    private val repository = NoteRepositoryImpl(dao)

    @Test
    fun `emits mapped notes on successful data fetch`() = runTest {
        // Arrange: Prepare a fake database entity
        val dbEntity =
            NoteEntity(id = 1, title = "DB Title", content = "DB Content", timestamp = 100L)
        // Tell the mock DAO to return this entity when called
        every { dao.getAllNotes() } returns flowOf(listOf(dbEntity))

        // Act & Assert: Use Turbine to test the Flow
        repository.getAllNotes().test {
            // Wait for the first item from the flow
            val result = awaitItem()

            // Verify the result is a success
            assertTrue(result is Resource.Success, "Result should be Success")

            // Verify the data was mapped correctly from NoteEntity to Note
            val notes = result.data
            assertEquals(1, notes.size)
            assertEquals("DB Title", notes[0].title)
            assertEquals(1L, notes[0].id)

            // Ensure the flow completes
            awaitComplete()
        }
    }

    @Test
    fun `emits error when database read fails`() = runTest {
        // Arrange: Make the DAO throw an error
        val dbException = RuntimeException("Database is locked")
        every { dao.getAllNotes() } returns flow { throw dbException }

        // Act & Assert
        repository.getAllNotes().test {
            val result = awaitItem()

            // Verify we caught the error and wrapped it
            assertTrue(result is Resource.Error, "Result should be Error")
            assertTrue(
                result.message.contains("Database is locked"),
                "Error message should contain the original exception message"
            )

            awaitComplete()
        }
    }

    @Test
    fun `returns error when inserting a note fails`() = runTest {
        // Arrange: Make the suspend function throw an error
        val noteToInsert = Note(id = null, title = "A", content = "B", timestamp = 1L)
        coEvery { dao.insertNote(any()) } throws RuntimeException("Disk is full")

        // Act: Call the function
        val result = repository.insertNote(noteToInsert)

        // Assert: Check the return value
        assertTrue(result is Resource.Error, "Result should be Error")
        assertTrue(
            result.message.contains("Disk is full"),
            "Error message should reflect the failure cause"
        )
    }
}