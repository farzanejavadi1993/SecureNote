package com.farzane.securenote.presentation.list
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.repository.NoteExporter
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetAllNotesUseCase
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import com.arkivanov.essenty.lifecycle.resume
import com.farzane.securenote.presentation.FakeAuthManager
import com.farzane.securenote.presentation.FakeNoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class NoteListComponentTest {

    // Fakes and Mocks
    private lateinit var fakeAuthManager: FakeAuthManager
    private lateinit var fakeRepository: FakeNoteRepository
    private val onNoteSelected: (Long) -> Unit = mockk(relaxed = true)
    private val onLock: () -> Unit = mockk(relaxed = true)
    private val noteExporter: NoteExporter = mockk(relaxed = true)

    // Component under test
    private lateinit var component: DefaultNoteListComponent
    private val lifecycle = LifecycleRegistry()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeAuthManager = FakeAuthManager()
        fakeRepository = FakeNoteRepository()

        // Setup UseCases
        val getAllNotesUseCase = GetAllNotesUseCase(fakeRepository)
        val addNoteUseCase = AddNoteUseCase(fakeRepository)
        val deleteNoteUseCase = DeleteNoteUseCase(fakeRepository)

        component = DefaultNoteListComponent(
            componentContext = DefaultComponentContext(lifecycle),
            authManager = fakeAuthManager,
            getAllNotesUseCase = getAllNotesUseCase,
            addNoteUseCase = addNoteUseCase,
            deleteNoteUseCase = deleteNoteUseCase,
            onNoteSelected = onNoteSelected,
            onNoteDeleted = {},
            onLock = onLock,
            noteExporter = noteExporter
        )
        lifecycle.resume()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization should load notes into state`() = runTest {
        // GIVEN: Repository has one note
        val note = Note(1, "Title", "Content", 123L)
        fakeRepository.addNote(note)

        // WHEN: Component starts (already happened in setup)

        // THEN: State should reflect the note and not be loading
        assertEquals(1, component.state.value.notes.size)
        assertFalse(component.state.value.isLoading)
    }

    @Test
    fun `ToggleSelectionMode should enable multi-selection and add note ID`() {
        // WHEN
        component.onEvent(NoteListIntent.ToggleSelectionMode(1L))

        // THEN
        assertTrue(component.state.value.isMultiSelectionMode)
        assertTrue(component.state.value.selectedNoteIds.contains(1L))
    }

    @Test
    fun `ClearSelectionMode should reset selection state`() {
        // GIVEN
        component.onEvent(NoteListIntent.ToggleSelectionMode(1L))

        // WHEN
        component.onEvent(NoteListIntent.ClearSelectionMode)

        // THEN
        assertFalse(component.state.value.isMultiSelectionMode)
        assertTrue(component.state.value.selectedNoteIds.isEmpty())
    }

    @Test
    fun `SelectNote intent should trigger navigation callback`() {
        // WHEN
        component.onEvent(NoteListIntent.SelectNote(123L))

        // THEN
        verify { onNoteSelected(123L) }
    }

    @Test
    fun `DeleteNote should notify parent on success`() = runTest {
        // GIVEN
        val note = Note(1, "Delete me", "", 0L)
        fakeRepository.addNote(note)
        val onNoteDeleted: (Long) -> Unit = mockk(relaxed = true)

        // Re-init with mocked callback for this specific test
        val testComponent = DefaultNoteListComponent(
            componentContext = DefaultComponentContext(LifecycleRegistry()),
            authManager = fakeAuthManager,
            getAllNotesUseCase = GetAllNotesUseCase(fakeRepository),
            addNoteUseCase = AddNoteUseCase(fakeRepository),
            deleteNoteUseCase = DeleteNoteUseCase(fakeRepository),
            onNoteSelected = {},
            onNoteDeleted = onNoteDeleted,
            onLock = {},
            noteExporter = noteExporter
        )

        // WHEN
        testComponent.onEvent(NoteListIntent.DeleteNote(1L))

        // THEN
        verify { onNoteDeleted(1L) }
    }

    @Test
    fun `ExportNotes with no notes should trigger error effect`() = runTest {
        // GIVEN: Empty repository

        // WHEN
        component.onEvent(NoteListIntent.ExportNotes)

        // THEN: Collect first effect
        val effect = component.effect.first()
        assertTrue(effect is NoteListEffect.ShowMessage)
        assertEquals("No notes to export.", (effect as NoteListEffect.ShowMessage).message)
    }
}
