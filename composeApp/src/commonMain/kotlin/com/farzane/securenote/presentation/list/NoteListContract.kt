package com.farzane.securenote.presentation.list

import com.farzane.securenote.domain.model.Note

/**
 * This file defines the "Contract" for the Note List screen.
 * It's the single source of truth for how the UI and the logic communicate,
 * following the MVI pattern.
 *
 * It contains two main parts:
 * 1. NoteListState: Everything the UI needs to know to draw itself.
 * 2. NoteListIntent: Every possible action the user can perform.
 */

/**
 * Represents a single snapshot of the Note List screen's state.
 * It answers the question: "What should the UI look like right now?"
 */
data class NoteListState(
    /** The list of notes to be displayed on the screen. */
    val notes: List<Note> = emptyList(),

    /** A flag to show a loading spinner while notes are being fetched. */
    val isLoading: Boolean = false,

    /** A flag to know if the user is currently selecting multiple notes. */
    val isMultiSelectionMode: Boolean = false,
    /**
     * A collection of the unique IDs of the notes that the user has selected.
     * A `Set` is used because it's highly efficient for checking if an item is selected (`contains`).
     */
    val selectedNoteIds: Set<Long> = emptySet(),

    /** A flag to know if the user has set up a PIN lock. Used to show the correct lock icon. */
    val hasPin: Boolean = false
)

/**
 * Represents every possible action or "Intent" that the user can perform on the screen.
 * It answers the question: "What did the user just do?"
 */

/**
 *Introduce NoteListEffect for one-time UI events
 *Moved side effects (like showing SnackBars and Errors) out of the persistent UI State
 *and into a dedicated 'Effect' flow.
 *This prevents bugs where messages would reappear on screen rotation.
 */
sealed interface NoteListEffect {
    data class ShowMessage(val message: String) : NoteListEffect
    data class ShowError(val error: String) : NoteListEffect
}

sealed interface NoteListIntent {
    /** User saved a new note from the "Add Note" dialog. Carries the new title and content. */
    data class AddNote(val title: String, val content: String) : NoteListIntent

    /** User confirmed they want to delete a specific note. Carries the ID of the note to delete. */
    data class DeleteNote(val id: Long) : NoteListIntent

    /** User tapped a note to view its details. Carries the ID to know which detail screen to open. */
    data class SelectNote(val id: Long) : NoteListIntent

    /** User confirmed they want to export notes. A simple signal with no extra data. */
    data object ExportNotes : NoteListIntent

    /** User long-pressed a note to *start* multi-selection mode. */
    data class ToggleSelectionMode(val noteId: Long) : NoteListIntent

    /** User tapped a note while *already in* multi-selection mode to add or remove it
     * from the selection. */
    data class ToggleNoteSelection(val noteId: Long) : NoteListIntent

    /** User tapped the "Close" button to exit multi-selection mode. */
    data object ClearSelectionMode : NoteListIntent

    /** User tapped the manual "Lock" icon in the app bar to secure the app now. */
    data object LockApp : NoteListIntent

    /** User confirmed they want to remove their PIN lock from the app. */
    data object RemovePin : NoteListIntent
}