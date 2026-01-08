package com.farzane.securenote.presentation.detail

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow

interface NoteDetailComponent {
    val state: Value<NoteDetailState>
    val effect: Flow<NoteDetailEffect>
    fun onEvent(intent: NoteDetailIntent)



}
