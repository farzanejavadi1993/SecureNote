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
import com.farzane.securenote.presentation.list.NoteListIntent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

/**
 * The Root Component is the "Brain" of the application's navigation and state.
 *
 * It is responsible for:
 * 1. Deciding which screen to show (List, Detail, or Lock).
 * 2. Handling global events like Auto-Locking when the app is in the background.
 * 3. Connecting different screens together (e.g., passing callbacks and data).
 */

@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent(
    componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext, KoinComponent {
    // --- Dependencies (from Koin) ---
    private val authManager by inject<AuthManager>()
    private val getNotesUseCase by inject<GetNotesUseCase>()
    private val addNoteUseCase by inject<AddNoteUseCase>()
    private val deleteNoteUseCase by inject<DeleteNoteUseCase>()
    private val getNoteByIdUseCase by inject<GetNoteByIdUseCase>()
    private val noteExporter by inject<NoteExporter>()
    /**
     * Defines all possible screens (Configurations) in our app's navigation stack.
     * We use @Serializable so Decompose can save/restore the navigation history.
     */

    @Serializable
    sealed interface Config {
        @Serializable
        data object Lock : Config

        @Serializable
        data object NoteList : Config

        @Serializable
        data class NoteDetail(val noteId: Long?) : Config
    }
    // The navigation controller that lets us push, pop, or replace screens.
    private val navigation = StackNavigation<Config>()

    /**
     * The navigation stack. It holds the history of screens the user has visited.
     * Decompose uses this to manage the component lifecycle for each screen.
     */
    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.NoteList,
            handleBackButton = true,
            childFactory = ::createChild
        )

    /**
     * Tracks the currently active Detail screen for the Master-Detail (Split View) layout.
     * This allows the UI to show the detail pane on tablets and desktops.
     */
    private val _activeDetail = MutableValue(
        RootComponent.ActiveDetail(null)
    )
    override val activeDetail: Value<RootComponent.ActiveDetail> = _activeDetail

    init {
        // Listen to stack changes to update the "Active Detail" state.
        // If the user navigates to a NoteDetail screen, we expose it here so the UI can show it side-by-side.

        stack.subscribe { childStack ->
            val activeChild = childStack.active.instance
            if (activeChild is RootComponent.Child.Detail) {
                _activeDetail.value = RootComponent.ActiveDetail(activeChild.component)
            } else {
                _activeDetail.value = RootComponent.ActiveDetail(null)
            }
        }

        // Listen to the App Lifecycle (e.g., when the app goes to the background and comes back).
        lifecycle.subscribe(object : Lifecycle.Callbacks {
            override fun onResume() {
                // Only check for auto-lock IF the user has a PIN enabled.
                if (authManager.hasPin()) {
                    // When the app comes to the foreground:
                    authManager.checkShouldLock()

                    if (
                        authManager.isAppLocked &&
                        stack.value.active.configuration !is Config.Lock
                    ) {
                        // Show the lock screen on top of the current screen.
                        navigation.push(Config.Lock)
                    }
                }
            }

            override fun onPause() {}
        })


    }

    /**
     * The Factory function for creating child components.
     * This is where Decompose builds the logic class for each screen in the stack.
     */
    @OptIn(DelicateDecomposeApi::class)
    private fun createChild(config: Config, context: ComponentContext): RootComponent.Child {
        return when (config) {

            Config.Lock -> RootComponent.Child.Lock(
                DefaultAuthComponent(
                    componentContext = context,
                    authManager = authManager,

                    // When authenticated, just close the lock screen (pop).
                    onAuthenticated = {
                        val listComponent = stack.value.backStack
                            .map { it.instance }
                            .filterIsInstance<RootComponent.Child.List>()
                            .lastOrNull()?.component

                        navigation.pop()
                        listComponent?.onEvent(NoteListIntent.RefreshState)

                    },
                    // NEW: Add a callback for when the user cancels.
                    onCancelled = { navigation.pop() }
                )
            )

            Config.NoteList -> RootComponent.Child.List(

                DefaultNoteListComponent(
                    authManager = authManager,
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
                        // 1. Check if the deleted note is the one currently showing in the detail view.
                        val isDeletingActiveDetail =
                            (_activeDetail.value.component?.state?.value?.id == deletedId)

                        // 2. Remove the deleted note's screen from the backstack history.
                        navigation.navigate { oldStack ->
                            oldStack.filterNot { config ->
                                config is Config.NoteDetail && config.noteId == deletedId
                            }
                        }
                        // 3. If we deleted the visible note, clear the Detail panel to show "Select a note".
                        if (isDeletingActiveDetail) {
                            _activeDetail.value = RootComponent.ActiveDetail(null)
                        }
                    },
                    onLock = {
                        authManager.lockApp() // Tell the manager to lock the state
                        navigation.bringToFront(Config.Lock) // Navigate to the Lock screen
                    },
                    onNavigateToLock = { navigation.push(Config.Lock) },

                    )
            )

            is Config.NoteDetail -> RootComponent.Child.Detail(
                DefaultNoteDetailComponent(
                    componentContext = context,
                    noteId = config.noteId,
                    getNoteByIdUseCase = getNoteByIdUseCase,
                    addNoteUseCase = addNoteUseCase,
                    deleteNoteUseCase = deleteNoteUseCase,
                    // Navigation: Close this screen when the user clicks "Back" or "Save".
                    onFinished = { navigation.pop() },
                )
            )
        }
    }
}

