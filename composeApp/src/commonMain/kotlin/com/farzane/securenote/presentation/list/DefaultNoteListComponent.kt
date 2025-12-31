package com.farzane.securenote.presentation.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.manager.AuthManager
import com.farzane.securenote.domain.repository.NoteExporter
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The core logic component for the Note List screen.
 * This class is responsible for:
 * - Loading notes from the repository.
 * - Handling all user actions (Intents) like adding, deleting, and selecting notes.
 * - Managing the UI state (`NoteListState`).
 * - Communicating navigation events (like selecting a note or locking the app) up to the parent component.
 */
class DefaultNoteListComponent(
    componentContext: ComponentContext,
    private val authManager: AuthManager,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val onNoteSelected: (Long) -> Unit,
    private val onNoteDeleted: (Long) -> Unit,
    private val onLock: () -> Unit,
    private val onNavigateToLock: () -> Unit,
    private val noteExporter: NoteExporter
) : NoteListComponent, ComponentContext by componentContext {


    private val _state = MutableValue(NoteListState(isLoading = true))
    override val state: Value<NoteListState> = _state

    // A dedicated coroutine scope for this component that is cancelled when the component is destroyed.
    private val scope =
        coroutineScope(
            SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate
        )

    init {
        _state.value = _state.value.copy(hasPin = authManager.hasPin())
        // Automatically load notes as soon as the component is created.
        loadNotes()
    }



    /**
     * Main entry point for all user actions from the UI.
     * Delegates the work to specific private functions for clarity.
     */
    override fun onEvent(intent: NoteListIntent) {
        when (intent) {
            is NoteListIntent.AddNote -> onAddNote(intent.title, intent.content)
            is NoteListIntent.DeleteNote -> onDeleteNote(intent.id)
            is NoteListIntent.SelectNote -> onNoteSelected(intent.id) // Direct pass-through for navigation
            is NoteListIntent.ExportNotes -> onExportNotes()
            is NoteListIntent.ClearSelectionMode -> onClearSelectionMode()
            is NoteListIntent.ToggleNoteSelection -> onToggleNoteSelection(intent.noteId)
            is NoteListIntent.ToggleSelectionMode -> onToggleSelectionMode(intent.noteId)
            is NoteListIntent.LockApp -> onLock() // Direct pass-through for navigation
            is NoteListIntent.NavigateToLock -> onNavigateToLock()
            is NoteListIntent.RemovePin -> {
                authManager.removePin()
                _state.value = _state.value.copy(hasPin = false)
            }
            is NoteListIntent.RefreshState -> {
                // Re-check the PIN status and update the UI.
                _state.value = _state.value.copy(hasPin = authManager.hasPin())
            }
        }
    }


    /** Loads all notes from the database. */
    private fun loadNotes() {
        scope.launch {
            getNotesUseCase().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(isLoading = true, error = null)
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(isLoading = false, notes = result.data, error = null)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    /**
     * Handles the logic for creating a new note.
     */
    private fun onAddNote(title: String, content: String) {
        scope.launch {
            val result = addNoteUseCase(id = null, title = title, content = content)
            if (result is Resource.Error) {
                // In a real app, this should trigger a snackbar via a side-effect channel.
                println("Error adding note: ${result.message}")
            }
        }
    }

    /**
     * Handles the logic for deleting a single note and notifies the parent.
     */
    private fun onDeleteNote(noteId: Long) {
        scope.launch {
            val result = deleteNoteUseCase(noteId)
            if (result is Resource.Success) {
                // Notify the root component so it can clear the detail view if necessary.
                onNoteDeleted(noteId)
            } else if (result is Resource.Error) {
                println("Error deleting note: ${result.message}")
            }
        }
    }

    /**
     * Manages exporting notes, either all notes or only the selected ones.
     */
    private fun onExportNotes() {
        scope.launch {
            val notesToExport = if (_state.value.isMultiSelectionMode) {
                _state.value.notes.filter { it.id in _state.value.selectedNoteIds }
            } else {
                _state.value.notes
            }

            if (notesToExport.isEmpty()) {
                _state.value = _state.value.copy(exportMessage = "No notes to export.")
                return@launch
            }

            when (val result = noteExporter.exportNotes(notesToExport)) {
                is Resource.Success -> {
                    // On success, show a message and automatically exit selection mode.
                    _state.value = _state.value.copy(
                        exportMessage = result.data,
                        isMultiSelectionMode = false,
                        selectedNoteIds = emptySet()
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(exportMessage = "Export failed: ${result.message}")
                }
                else -> {}
            }
        }
    }

    /**
     * Exits multi-selection mode and clears all selections.
     * Typically triggered by the user pressing the "Close" or "Back" button.
     */
    private fun onClearSelectionMode() {
        _state.value = _state.value.copy(
            isMultiSelectionMode = false,
            selectedNoteIds = emptySet()
        )
    }

    /**
     * Adds or removes a note from the selection set when the user is already in multi-selection mode.
     */
    private fun onToggleNoteSelection(noteId: Long) {
        val currentIds = _state.value.selectedNoteIds.toMutableSet()
        if (currentIds.contains(noteId)) {
            currentIds.remove(noteId)
        } else {
            currentIds.add(noteId)
        }

        // If the user unchecks the last item, automatically exit selection mode.
        val isModeActive = currentIds.isNotEmpty()
        _state.value = _state.value.copy(
            selectedNoteIds = currentIds,
            isMultiSelectionMode = isModeActive
        )
    }

    /**
     * Enters multi-selection mode when a user long-presses a note for the first time.
     */
    private fun onToggleSelectionMode(noteId: Long) {
        val currentIds = _state.value.selectedNoteIds.toMutableSet()
        currentIds.add(noteId) // Long-press always starts selection with the target item.

        _state.value = _state.value.copy(
            selectedNoteIds = currentIds,
            isMultiSelectionMode = true
        )
    }

}


