package com.farzane.securenote.presentation.note_list

import com.arkivanov.decompose.value.Value

interface NoteListComponent {
    val state: Value<NoteListState>
    fun onEvent(intent: NoteListIntent)
}