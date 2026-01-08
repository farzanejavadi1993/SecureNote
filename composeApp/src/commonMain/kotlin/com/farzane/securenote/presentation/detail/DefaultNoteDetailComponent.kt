package com.farzane.securenote.presentation.detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The core logic component for the Note Detail screen.
 * This class handles:
 * - Loading an existing note for editing.
 * - Updating the title and content as the user types.
 * - Saving a new note or updating an existing one.
 * - Deleting a note.
 */

class DefaultNoteDetailComponent(
    componentContext: ComponentContext,
    private val noteId: Long?, // Null for new note, ID for edit
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val onFinished: () -> Unit, // A callback to close the screen.
) : NoteDetailComponent, ComponentContext by componentContext {

    // The UI state for the screen (e.g., title, content).
    private val _state = MutableValue(NoteDetailState(id = noteId))
    override val state: Value<NoteDetailState> = _state

    // A channel for sending one-time events (side effects) to the UI, like showing a snackBar.
    private val _effect = Channel<NoteDetailEffect>()
    override val effect = _effect.receiveAsFlow()

    // A dedicated coroutine scope for this component.
    private val scope = coroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        // If we are editing an existing note, load its data from the database.
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    /** Fetches the note data from the database and updates the UI state. */
    private fun loadNote(id: Long) {
        scope.launch {
            val note = getNoteByIdUseCase(id)

            if (note != null) {
                _state.value = _state.value.copy(
                    title = note.title,
                    content = note.content
                )
            }
        }
    }

    /**
     * Main entry point for all user actions from the UI.
     * This delegates the work to specific private functions for clarity.
     */
    override fun onEvent(intent: NoteDetailIntent) {
        when (intent) {
            is NoteDetailIntent.UpdateTitle -> onUpdateTitle(intent.title)
            is NoteDetailIntent.UpdateContent -> onUpdateContent(intent.content)
            is NoteDetailIntent.SaveNote -> onSaveNote()
            is NoteDetailIntent.DeleteNote -> onDeleteNote()
            is NoteDetailIntent.Close -> onFinished() // Direct pass-through for navigation.
        }
    }

    /** Updates the title in the UI state as the user types. */
    private fun onUpdateTitle(title: String) {
        _state.value = _state.value.copy(title = title)
    }

    /** Updates the content in the UI state as the user types. */
    private fun onUpdateContent(content: String) {
        _state.value = _state.value.copy(content = content)
    }

    /** Saves the current note (either new or existing) to the database. */
    private fun onSaveNote() {
        scope.launch {
            val currentState = _state.value

            // First, check if the note is empty. If so, show an error and stop.
            if (currentState.title.isBlank() || currentState.content.isBlank()) {
                _effect.send(NoteDetailEffect.Error("Title and content cannot be empty."))
                return@launch
            }

            // Save the note to the database.
            addNoteUseCase(
                id = currentState.id,
                title = currentState.title,
                content = currentState.content
            )

            // After saving, close the screen.
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }

    /** Deletes the current note from the database. */
    private fun onDeleteNote() {
        scope.launch {
            // Only try to delete if we are editing an existing note.
            if (noteId != null) {
                deleteNoteUseCase(noteId)
            }
            // After deleting (or if it was a new note), close the screen.
            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}