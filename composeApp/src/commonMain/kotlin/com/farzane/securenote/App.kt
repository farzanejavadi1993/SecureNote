package com.farzane.securenote

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.farzane.securenote.presentation.note_list.NoteListScreen
import com.farzane.securenote.presentation.root.RootComponent


@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        NoteListScreen(component = rootComponent.noteListComponent)
    }
}