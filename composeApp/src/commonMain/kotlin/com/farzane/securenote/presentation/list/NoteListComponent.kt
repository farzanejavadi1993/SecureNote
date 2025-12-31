package com.farzane.securenote.presentation.list

import com.arkivanov.decompose.value.Value

interface NoteListComponent {
    val state: Value<NoteListState>
    fun onEvent(intent: NoteListIntent)
}