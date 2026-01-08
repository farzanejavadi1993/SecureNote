package com.farzane.securenote.presentation.root

import com.farzane.securenote.presentation.list.NoteListComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.farzane.securenote.presentation.lock.AuthComponent
import com.farzane.securenote.presentation.detail.NoteDetailComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    val activeDetail: Value<ActiveDetail>
    data class ActiveDetail(val noteDetailComponent: NoteDetailComponent?)
    sealed interface Child {
        data class List(val noteListComponent: NoteListComponent) : Child
        data class Detail(val noteDetailComponent: NoteDetailComponent) : Child
        data class Lock(val authComponent: AuthComponent) : Child
    }
}