package com.farzane.securenote.domain.usecase

import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetNoteByIdUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val getNoteByIdUseCase = GetNoteByIdUseCase(repository)

    @Test
    fun `returns note when it exists in repository`() = runTest {
        // Arrange
        val expectedNote = Note(id = 1, title = "Target", content = "Content", timestamp = 100L)
        coEvery { repository.getNoteById(1) } returns expectedNote

        // Act
        val result = getNoteByIdUseCase(1)

        // Assert
        assertEquals(expectedNote, result)
        coVerify(exactly = 1) { repository.getNoteById(1) }
    }


}
