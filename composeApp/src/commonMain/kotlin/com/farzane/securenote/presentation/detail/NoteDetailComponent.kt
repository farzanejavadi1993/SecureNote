package com.farzane.securenote.presentation.detail

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow

interface NoteDetailComponent {
    val state: Value<NoteDetailState>
    val labels: Flow<Label>
    fun onEvent(intent: NoteDetailIntent)

    sealed interface Label {
        data class Error(val message: String) : Label
    }

}
