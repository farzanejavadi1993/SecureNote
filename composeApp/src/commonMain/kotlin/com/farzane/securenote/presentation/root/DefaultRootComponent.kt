package com.farzane.securenote.presentation.root

import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import com.farzane.securenote.presentation.note_list.DefaultNoteListComponent
import org.koin.core.component.inject
import kotlin.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.farzane.securenote.domain.repository.NoteExporter
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import com.farzane.securenote.presentation.note_detail.DefaultNoteDetailComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val getNotesUseCase by inject<GetNotesUseCase>()
    private val addNoteUseCase by inject<AddNoteUseCase>()
    private val deleteNoteUseCase by inject<DeleteNoteUseCase>()

    private val getNoteByIdUseCase by inject<GetNoteByIdUseCase>()
    private val noteExporter by inject<NoteExporter>()


    @Serializable
    sealed interface Config {
        @Serializable
        data object NoteList : Config

        @Serializable
        data class NoteDetail(val noteId: Long?) : Config
    }
    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = kotlinx.serialization.serializer<Config>(),
            initialConfiguration = Config.NoteList, 
            handleBackButton = true,
            childFactory = ::createChild
        )

    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {
            Config.NoteList -> RootComponent.Child.List(

                DefaultNoteListComponent(
                    componentContext = context,
                    getNotesUseCase = getNotesUseCase,
                    addNoteUseCase = addNoteUseCase,
                    deleteNoteUseCase = deleteNoteUseCase,
                    noteExporter = noteExporter,
                    onNoteSelected = { noteId ->
                        navigation.push(Config.NoteDetail(noteId))
                    },

                )
            )
            is Config.NoteDetail -> RootComponent.Child.Detail(
                DefaultNoteDetailComponent(
                    componentContext = context,
                    noteId = config.noteId,
                    getNoteByIdUseCase = getNoteByIdUseCase,
                    addNoteUseCase = addNoteUseCase,
                    onFinished = { navigation.pop() }
                )
            )
        }
    }
}

