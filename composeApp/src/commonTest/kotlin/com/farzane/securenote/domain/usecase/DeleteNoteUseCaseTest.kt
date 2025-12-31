package com.farzane.securenote.domain.usecase

import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeleteNoteUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val deleteNoteUseCase = DeleteNoteUseCase(repository)

    @Test
    fun `deletes note successfully when repository succeeds`() = runTest {
        // Arrange
        val noteId = 100L
        coEvery { repository.deleteNote(noteId) } returns Resource.Success(Unit)

        // Act
        val result = deleteNoteUseCase(noteId)

        // Assert
        assertTrue(result is Resource.Success)
        // Verify we actually tried to delete the specific ID, not just 'any'
        coVerify(exactly = 1) { repository.deleteNote(noteId) }
    }

    @Test
    fun `returns error when repository operation fails`() = runTest {
        // Arrange
        val errorMsg = "Could not delete"
        coEvery { repository.deleteNote(any()) } returns Resource.Error(errorMsg)

        // Act
        val result = deleteNoteUseCase(55L)

        // Assert
        // We verify that the UI will actually receive the error message
        assertTrue(result is Resource.Error)
        assertEquals(errorMsg, result.message)
    }
}
