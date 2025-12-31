package com.farzane.securenote.domain.usecase


import app.cash.turbine.test
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetNotesUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val getNotesUseCase = GetNotesUseCase(repository)

    @Test
    fun `returns notes sorted by newest first`() = runTest {
        // Arrange: Create unsorted data (Oldest is first here)
        val oldNote = Note(1, "Old", "Content", timestamp = 1000L)
        val newNote = Note(2, "New", "Content", timestamp = 9000L)
        val midNote = Note(3, "Mid", "Content", timestamp = 5000L)

        val unsortedList = listOf(oldNote, newNote, midNote)

        // Mock repo returning raw list
        every { repository.getAllNotes() } returns flowOf(Resource.Success(unsortedList))

        // Act & Assert
        getNotesUseCase().test {
            val result = awaitItem()
            val data = (result as Resource.Success).data

            // Verify business rule: 9000 > 5000 > 1000
            assertEquals(newNote, data[0])
            assertEquals(midNote, data[1])
            assertEquals(oldNote, data[2])

            awaitComplete()
        }
    }

    @Test
    fun `propagates success data from repository`() = runTest {
        // Arrange
        val notes = listOf(Note(1, "Title", "Content", 100L))
        every { repository.getAllNotes() } returns flowOf(Resource.Success(notes))

        // Act & Assert
        getNotesUseCase().test {
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals(notes, result.data)
            awaitComplete()
        }
    }

    @Test
    fun `propagates error when repository fails`() = runTest {
        // Arrange
        val errorMessage = "Database read failed"
        every { repository.getAllNotes() } returns flowOf(Resource.Error(errorMessage))

        // Act & Assert
        getNotesUseCase().test {
            val result = awaitItem()
            assertTrue(result is Resource.Error)
            assertEquals(errorMessage, result.message)
            awaitComplete()
        }
    }
}
