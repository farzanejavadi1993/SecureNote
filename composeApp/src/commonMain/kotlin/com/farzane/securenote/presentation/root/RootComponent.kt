package com.farzane.securenote.presentation.root

import com.farzane.securenote.presentation.note_list.NoteListComponent
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.farzane.securenote.presentation.note_detail.NoteDetailComponent

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>
    val activeDetail: Value<ActiveDetail>
    data class ActiveDetail(val component: NoteDetailComponent?)
    sealed class Child {
        data class List(val component: NoteListComponent) : Child()
        data class Detail(val component: NoteDetailComponent) : Child()
    }
}
