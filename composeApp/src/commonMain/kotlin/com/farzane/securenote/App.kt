package com.farzane.securenote

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import com.farzane.securenote.presentation.note_list.NoteListScreen
import com.farzane.securenote.presentation.root.RootComponent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.farzane.securenote.presentation.note_detail.NoteDetailScreen

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        Children(
            stack = rootComponent.stack,
            animation = stackAnimation(slide())
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.List -> NoteListScreen(instance.component)
                is RootComponent.Child.Detail -> NoteDetailScreen(instance.component)
            }
        }
    }
}
