package com.farzane.securenote.presentation.list
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteExporter
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetAllNotesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNoteListComponentTest {

    // We mock the dependencies because we are testing the Component, not the UseCases.
    private val getAllNotesUseCase: GetAllNotesUseCase = mockk()
    private val addNoteUseCase: AddNoteUseCase = mockk()
    private val deleteNoteUseCase: DeleteNoteUseCase = mockk()
    private val noteExporter: NoteExporter = mockk()


    // A simple mock for the navigation callback
    private val onNoteSelectedCallback: (Long) -> Unit = mockk(relaxed = true)

    // --- Test Setup ---
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Tests ---

    @Test
    fun `shows list of notes when data loads successfully`() = runTest(testDispatcher) {
        // Given (Arrange)
        val dummyNotes = listOf(
            Note(1L, "Meeting", "Discuss project", 1000L),
            Note(2L, "Gym", "Leg day", 2000L)
        )
        every { getAllNotesUseCase() } returns flowOf(Resource.Success(dummyNotes))

        // When (Act)
        val component = createComponent()
        advanceUntilIdle() // Wait for init block to finish

        // Then (Assert)
        val state = component.state.value
        assertFalse(state.isLoading, "Loading should stop after data arrives")
        assertEquals(
            dummyNotes,
            state.notes,
            "State should match the data from UseCase")
        assertNull(state.error, "Error should be null on success")
    }

    @Test
    fun `shows error message when loading notes fails`() = runTest(testDispatcher) {
        // Given
        val errorMessage = "Failed to connect to database"
        every { getAllNotesUseCase() } returns flowOf(Resource.Error(errorMessage))

        // When
        val component = createComponent()
        advanceUntilIdle()

        // Then
        val state = component.state.value
        assertFalse(state.isLoading)
        assertTrue(state.notes.isEmpty())
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `saves new note when AddNote event is triggered`() = runTest(testDispatcher) {
        // Given
        // We need the init block to pass first
        every { getAllNotesUseCase() } returns flowOf(Resource.Success(emptyList()))

        // Mock the add action returning Success
        coEvery {
            addNoteUseCase(
                any(),
                any(),
                any())} returns Resource.Success(Unit)

        val component = createComponent()
        advanceUntilIdle()

        // When
        component.onEvent(NoteListIntent.AddNote(
            "New Title",
            "New Content"
        ))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) {
            addNoteUseCase(id = null, title = "New Title", content = "New Content")
        }
    }

    @Test
    fun `deletes note when DeleteNote event is triggered`() = runTest(testDispatcher) {
        // Given
        val noteIdToDelete = 99L
        every { getAllNotesUseCase() } returns flowOf(Resource.Success(emptyList()))

        // Mock delete action
        coEvery { deleteNoteUseCase(noteIdToDelete) } returns Resource.Success(Unit)

        val component = createComponent()
        advanceUntilIdle()

        // When
        component.onEvent(NoteListIntent.DeleteNote(noteIdToDelete))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { deleteNoteUseCase(noteIdToDelete) }
    }

    @Test
    fun `navigates to detail screen when note is selected`() = runTest(testDispatcher) {
        // Given
        val selectedNoteId = 55L
        every { getAllNotesUseCase() } returns flowOf(Resource.Success(emptyList()))

        val component = createComponent()
        advanceUntilIdle()

        // When
        component.onEvent(NoteListIntent.SelectNote(selectedNoteId))

        // Then
        verify(exactly = 1) { onNoteSelectedCallback(selectedNoteId) }
    }

    @Test
    fun `updates state with export message when export succeeds`() =
        runTest(testDispatcher) {
        // Given
        val notes = listOf(Note(1, "A", "B", 100))
        val successPath = "Saved to /Downloads/notes.txt"

        every { getAllNotesUseCase() } returns flowOf(Resource.Success(notes))
        coEvery { noteExporter.exportNotes(notes) } returns Resource.Success(successPath)

        val component = createComponent()
        advanceUntilIdle()

        // When
        component.onEvent(NoteListIntent.ExportNotes)
        advanceUntilIdle()

        // Then
        assertEquals(successPath, component.state.value.exportMessage)
    }

    // --- Helper ---

    private fun createComponent(): DefaultNoteListComponent {
        return DefaultNoteListComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            getAllNotesUseCase = getAllNotesUseCase,
            addNoteUseCase = addNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            onNoteSelected = onNoteSelectedCallback,
            noteExporter = noteExporter,
            onNoteDeleted = { },
        )
    }
}
