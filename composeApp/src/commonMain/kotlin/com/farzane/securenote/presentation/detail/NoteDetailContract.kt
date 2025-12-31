package com.farzane.securenote.presentation.detail

data class NoteDetailState(
    val id: Long? = null, // null = new note, value = editing existing
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false
)
sealed interface NoteDetailIntent {
    data class UpdateTitle(val title: String) : NoteDetailIntent
    data class UpdateContent(val content: String) : NoteDetailIntent
    data object SaveNote : NoteDetailIntent
    data object Close : NoteDetailIntent
    data object DeleteNote : NoteDetailIntent
}