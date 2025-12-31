package com.farzane.securenote.presentation.root

import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import com.farzane.securenote.presentation.list.DefaultNoteListComponent
import org.koin.core.component.inject
import kotlin.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.farzane.securenote.domain.manager.AuthManager
import com.farzane.securenote.domain.repository.NoteExporter
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import com.farzane.securenote.presentation.lock.DefaultAuthComponent
import com.farzane.securenote.presentation.detail.DefaultNoteDetailComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val authManager by inject<AuthManager>()
    private val getNotesUseCase by inject<GetNotesUseCase>()
    private val addNoteUseCase by inject<AddNoteUseCase>()
    private val deleteNoteUseCase by inject<DeleteNoteUseCase>()
    private val getNoteByIdUseCase by inject<GetNoteByIdUseCase>()
    private val noteExporter by inject<NoteExporter>()

    @Serializable
    sealed interface Config {
        @Serializable
        data object Lock : Config

        @Serializable
        data object NoteList : Config

        @Serializable
        data class NoteDetail(val noteId: Long?) : Config
    }

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = if (authManager.hasPin()) Config.Lock else Config.NoteList ,
            handleBackButton = true,
            childFactory = ::createChild
        )

    private val _activeDetail = MutableValue(
        RootComponent.ActiveDetail(null)
    )
    override val activeDetail: Value<RootComponent.ActiveDetail> = _activeDetail

    init {

        stack.subscribe { childStack ->
            val activeChild = childStack.active.instance
            if (activeChild is RootComponent.Child.Detail) {
                _activeDetail.value = RootComponent.ActiveDetail(activeChild.component)
            } else {
                _activeDetail.value = RootComponent.ActiveDetail(null)
            }
        }
        lifecycle.subscribe(object : Lifecycle.Callbacks {
            override fun onResume() {

                authManager.checkShouldLock()


                if (authManager.isAppLocked && stack.value.active.configuration !is Config.Lock) {
                    navigation.bringToFront(Config.Lock)

                }
            }

            override fun onPause() {
                // App went to background
            }
        })
    }


    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {

            Config.Lock -> RootComponent.Child.Lock(
                DefaultAuthComponent(
                    componentContext = context,
                    authManager = authManager,
                    onAuthenticated = {
                        navigation.replaceAll(Config.NoteList)
                    }
                )
            )
            Config.NoteList -> RootComponent.Child.List(

                DefaultNoteListComponent(
                    componentContext = context,
                    getNotesUseCase = getNotesUseCase,
                    addNoteUseCase = addNoteUseCase,
                    deleteNoteUseCase = deleteNoteUseCase,
                    noteExporter = noteExporter,
                    onNoteSelected = { noteId ->
                        if (stack.value.active.configuration is Config.NoteDetail) {
                            navigation.replaceCurrent(Config.NoteDetail(noteId))
                        } else {
                            navigation.push(Config.NoteDetail(noteId))
                        }
                    },
                    onNoteDeleted = { deletedId ->
                        val isDeletingActiveDetail = (_activeDetail.value.component?.state?.value?.id == deletedId)

                        navigation.navigate { oldStack ->
                            oldStack.filterNot { config ->
                                config is Config.NoteDetail && config.noteId == deletedId
                            }
                        }
                        if (isDeletingActiveDetail) {
                            _activeDetail.value = RootComponent.ActiveDetail(null)
                        }
                    },
                    onLock = {
                        authManager.lockApp() // Tell the manager to lock the state
                        navigation.bringToFront(Config.Lock) // Navigate to the Lock screen
                    }

            )
            )

            is Config.NoteDetail -> RootComponent.Child.Detail(
                DefaultNoteDetailComponent(
                    componentContext = context,
                    noteId = config.noteId,
                    getNoteByIdUseCase = getNoteByIdUseCase,
                    addNoteUseCase = addNoteUseCase,
                    deleteNoteUseCase = deleteNoteUseCase,
                    onFinished = { navigation.pop() },
                )
            )
        }
    }
}

