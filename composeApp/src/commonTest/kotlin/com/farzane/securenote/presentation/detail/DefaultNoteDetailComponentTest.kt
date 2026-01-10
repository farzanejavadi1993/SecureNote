package com.farzane.securenote.presentation.detail

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.farzane.securenote.domain.model.Note
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import com.farzane.securenote.presentation.FakeNoteRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.farzane.securenote.core.util.Resource
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailComponentTest {

    private lateinit var fakeRepository: FakeNoteRepository
    private val onFinished: () -> Unit = mockk(relaxed = true)

    // UseCases
    private lateinit var getNoteByIdUseCase: GetNoteByIdUseCase
    private lateinit var addNoteUseCase: AddNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase

    @BeforeTest
    fun setup() {
        // Set Main dispatcher to a test version
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeRepository = FakeNoteRepository()

        getNoteByIdUseCase = GetNoteByIdUseCase(fakeRepository)
        addNoteUseCase = AddNoteUseCase(fakeRepository)
        deleteNoteUseCase = DeleteNoteUseCase(fakeRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Helper to create the component for specific test scenarios */
    private fun createComponent(id: Long? = null) = DefaultNoteDetailComponent(
        componentContext = DefaultComponentContext(LifecycleRegistry()),
        noteId = id,
        getNoteByIdUseCase = getNoteByIdUseCase,
        addNoteUseCase = addNoteUseCase,
        deleteNoteUseCase = deleteNoteUseCase,
        onFinished = onFinished
    )

    @Test
    fun `when editing existing note, it should load data into state`() = runTest {
        // GIVEN: Note exists in repo
        val note = Note(id = 1, title = "Original", content = "Content", timestamp = 0L)
        fakeRepository.addNoteSync(note)

        // WHEN: Component starts
        val component = createComponent(id = 1)

        // THEN: State is populated
        assertEquals("Original", component.state.value.title)
    }

    @Test
    fun `saving valid note should call repository and finish navigation`() = runTest {
        val component = createComponent(id = null) // New Note

        // WHEN: User inputs data and saves
        component.onEvent(NoteDetailIntent.UpdateTitle("New Title"))
        component.onEvent(NoteDetailIntent.UpdateContent("New Content"))
        component.onEvent(NoteDetailIntent.SaveNote)

        // THEN: Note is in repository
        val notes = fakeRepository.getAllNotes().first()
        assertEquals(1, (notes as Resource.Success<List<Note>>).data.size)
        assertEquals("New Title", notes.data[0].title)

        // AND: Navigation is closed
        verify { onFinished() }
    }

    @Test
    fun `EDGE CASE - saving empty note should trigger error effect and NOT save`() = runTest {
        val component = createComponent(id = null)

        // WHEN: User tries to save empty strings
        component.onEvent(NoteDetailIntent.UpdateTitle(" "))
        component.onEvent(NoteDetailIntent.SaveNote)

        // THEN: Error effect is sent
        val effect = component.effect.first()
        assertTrue(effect is NoteDetailEffect.Error)

        // AND: Navigation is NOT closed
        verify(exactly = 0) { onFinished() }
    }

    @Test
    fun `deleting existing note should call repository and finish`() = runTest {
        // GIVEN: Note exists
        fakeRepository.addNoteSync(Note(1, "Title", "Body", 0L))
        val component = createComponent(id = 1)

        // WHEN: User deletes
        component.onEvent(NoteDetailIntent.DeleteNote)

        // THEN: Note is removed and screen closed
        val notes = fakeRepository.getAllNotes().first()
        assertTrue((notes as Resource.Success).data.isEmpty())
        verify { onFinished() }
    }

    @Test
    fun `Close intent should trigger navigation callback immediately`() {
        val component = createComponent()

        // WHEN
        component.onEvent(NoteDetailIntent.Close)

        // THEN
        verify { onFinished() }
    }
}