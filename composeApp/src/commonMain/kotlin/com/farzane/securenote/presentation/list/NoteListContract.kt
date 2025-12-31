package com.farzane.securenote.presentation.list

import com.farzane.securenote.domain.model.Note

data class NoteListState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val exportMessage: String? = null,
    val isMultiSelectionMode: Boolean = false,
    val selectedNoteIds: Set<Long> = emptySet()
)
sealed interface NoteListIntent {
    data class AddNote(val title: String, val content: String) : NoteListIntent
    data class DeleteNote(val id: Long) : NoteListIntent
    data class SelectNote(val id: Long) : NoteListIntent
    data object ExportNotes : NoteListIntent
    data class ToggleSelectionMode(val noteId: Long) : NoteListIntent
    data class ToggleNoteSelection(val noteId: Long) : NoteListIntent
    data object ClearSelectionMode : NoteListIntent

    data object LockApp : NoteListIntent

}
