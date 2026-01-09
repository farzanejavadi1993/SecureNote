package com.farzane.securenote.domain.usecase


import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteRepository
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AddNoteUseCaseTest {

    // Use 'relaxed = true' so we don't have to mock the Unit return type every time
    private val repository: NoteRepository = mockk(relaxed = true)
    private val addNoteUseCase = AddNoteUseCase(repository)

    @Test
    fun `saves new note with correct title and content`() = runTest {
        val title = "Groceries"
        val content = "Milk, Eggs, Bread"

        // Execute
        addNoteUseCase(id = null, title = title, content = content)

        // Capture what was sent to the repo
        val noteSlot = slot<Note>()
        coVerify { repository.addNote(capture(noteSlot)) }

        // Verify the data is correct
        assertEquals("Groceries", noteSlot.captured.title)
        assertEquals("Milk, Eggs, Bread", noteSlot.captured.content)
        assertEquals(null, noteSlot.captured.id) // Null ID means new note
    }

    @Test
    fun `updates existing note when id is provided`() = runTest {
        val existingId = 101L
        val newTitle = "Updated Title"

        // Execute
        addNoteUseCase(id = existingId, title = newTitle, content = "Some content")

        // Capture
        val noteSlot = slot<Note>()
        coVerify { repository.addNote(capture(noteSlot)) }

        // Verify ID was preserved (this ensures it updates instead of creating new)
        assertEquals(existingId, noteSlot.captured.id)
        assertEquals(newTitle, noteSlot.captured.title)
    }
}
