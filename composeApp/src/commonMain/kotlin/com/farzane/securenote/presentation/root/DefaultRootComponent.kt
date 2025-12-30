package com.farzane.securenote.presentation.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import com.farzane.securenote.presentation.note_list.DefaultNoteListComponent
import com.farzane.securenote.presentation.note_list.NoteListComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.getValue

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val getNotesUseCase by inject<GetNotesUseCase>()
    private val addNoteUseCase by inject<AddNoteUseCase>()
    private val deleteNoteUseCase by inject<DeleteNoteUseCase>()

    override val noteListComponent: NoteListComponent = DefaultNoteListComponent(
        componentContext = childContext(key = "NoteList"),
        getNotesUseCase = getNotesUseCase,
        addNoteUseCase = addNoteUseCase,
        deleteNoteUseCase = deleteNoteUseCase
    )
}
