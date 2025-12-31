package com.farzane.securenote.presentation.note_detail

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNoteDetailComponentTest {

    // --- Mocks ---
    private val getNoteByIdUseCase: GetNoteByIdUseCase = mockk()
    private val addNoteUseCase: AddNoteUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk()
    private val onFinishedCallback: () -> Unit = mockk(relaxed = true)

    // --- Test Control ---
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Initialization Tests ---

    @Test
    fun `starts as empty form when creating a New Note`() = runTest(testDispatcher) {
        // Act: Create component with null ID
        val component = createComponent(noteId = null)

        // Assert
        val state = component.state.value
        assertEquals("", state.title)
        assertEquals("", state.content)
        assertEquals(null, state.id)
    }

    @Test
    fun `loads existing note data when creating component with an ID`() = runTest(testDispatcher) {
        // Arrange
        val existingNote =
            Note(id = 123L, title = "My Title", content = "My Content", timestamp = 100L)
        coEvery { getNoteByIdUseCase(123L) } returns existingNote

        // Act
        val component = createComponent(noteId = 123L)
        advanceUntilIdle() // Wait for loadNote coroutine

        // Assert
        val state = component.state.value
        assertEquals("My Title", state.title)
        assertEquals("My Content", state.content)
        assertEquals(123L, state.id)
    }

    // --- Interaction Tests ---

    @Test
    fun `updates title in state when user types`() = runTest(testDispatcher) {
        val component = createComponent(noteId = null)

        // Act
        component.onEvent(NoteDetailIntent.UpdateTitle("New T"))
        component.onEvent(NoteDetailIntent.UpdateTitle("New Title"))

        // Assert
        assertEquals("New Title", component.state.value.title)
    }

    @Test
    fun `updates content in state when user types`() = runTest(testDispatcher) {
        val component = createComponent(noteId = null)

        // Act
        component.onEvent(NoteDetailIntent.UpdateContent("Hello Wor"))
        component.onEvent(NoteDetailIntent.UpdateContent("Hello World"))

        // Assert
        assertEquals("Hello World", component.state.value.content)
    }

    // --- Save & Navigation Tests ---

    @Test
    fun `saves note and closes screen when data is valid`() = runTest(testDispatcher) {
        // Arrange
        coEvery {
            addNoteUseCase(
                any(),
                any(),
                any()
            )
        } returns Resource.Success(Unit)// Mock successful save

        val component = createComponent(noteId = null)

        // Input data
        component.onEvent(NoteDetailIntent.UpdateTitle("Meeting"))
        component.onEvent(NoteDetailIntent.UpdateContent("At 5 PM"))

        // Act: Click Save
        component.onEvent(NoteDetailIntent.SaveNote)
        advanceUntilIdle() // Wait for save coroutine

        // Assert
        // 1. Check loading state was triggered (it might flip back fast, but logic sets it)
        assertTrue(component.state.value.isSaving)

        // 2. Verify UseCase was called with correct data
        coVerify(exactly = 1) {
            addNoteUseCase(id = null, title = "Meeting", content = "At 5 PM")
        }

        // 3. Verify navigation happened
        verify(exactly = 1) { onFinishedCallback() }
    }

    @Test
    fun `does nothing if title is blank when saving`() = runTest(testDispatcher) {
        // Arrange
        val component = createComponent(noteId = null)

        // Only set content, leave title empty
        component.onEvent(NoteDetailIntent.UpdateContent("Content without title"))

        // Act
        component.onEvent(NoteDetailIntent.SaveNote)
        advanceUntilIdle()

        // Assert
        assertFalse(component.state.value.isSaving)
        coVerify(exactly = 0) { addNoteUseCase(any(), any(), any()) } // Should NOT save
        verify(exactly = 0) { onFinishedCallback() } // Should NOT close
    }

    @Test
    fun `closes screen when Close event received`() = runTest(testDispatcher) {
        val component = createComponent(noteId = null)

        // Act
        component.onEvent(NoteDetailIntent.Close)

        // Assert
        verify(exactly = 1) { onFinishedCallback() }
    }

    // --- Helper to avoid repeating setup code ---
    private fun createComponent(noteId: Long?): DefaultNoteDetailComponent {
        val lifecycle = LifecycleRegistry()
        return DefaultNoteDetailComponent(
            componentContext = DefaultComponentContext(lifecycle),
            noteId = noteId,
            getNoteByIdUseCase = getNoteByIdUseCase,
            addNoteUseCase = addNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            onFinished = onFinishedCallback,
            )
    }
}
