package com.farzane.securenote.presentation.note_detail

import com.arkivanov.decompose.value.Value

interface NoteDetailComponent {
    val state: Value<NoteDetailState>
    fun onEvent(intent: NoteDetailIntent)

}
