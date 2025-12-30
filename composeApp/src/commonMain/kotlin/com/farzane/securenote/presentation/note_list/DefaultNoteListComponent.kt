package com.farzane.securenote.presentation.note_list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.farzane.securenote.core.util.Resource
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.DeleteNoteUseCase
import com.farzane.securenote.domain.usecase.GetNotesUseCase
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DefaultNoteListComponent(
    componentContext: ComponentContext,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val onNoteSelected: (Long) -> Unit,
) : NoteListComponent, ComponentContext by componentContext {
    private val _state = MutableValue(NoteListState(isLoading = true))
    override val state: Value<NoteListState> = _state

    private val scope =
        coroutineScope(SupervisorJob() + kotlinx.coroutines.Dispatchers.Main.immediate)

    init {
        loadNotes()
    }

    private fun loadNotes() {
        scope.launch {
            getNotesUseCase().collect { result ->

                when (result) {
                    is Resource.Loading -> {
                        _state.value = _state.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            notes = result.data,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    override fun onEvent(intent: NoteListIntent) {
        when (intent) {
            is NoteListIntent.AddNote -> {
                scope.launch {
                    addNoteUseCase(null,intent.title, intent.content)
                }
            }
            is NoteListIntent.DeleteNote -> {
                scope.launch {
                    deleteNoteUseCase(intent.id)
                }
            }
            is NoteListIntent.SelectNote -> {
                onNoteSelected(intent.id)
            }
        }
    }

}
