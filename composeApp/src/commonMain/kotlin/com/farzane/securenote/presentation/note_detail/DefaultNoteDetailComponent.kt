package com.farzane.securenote.presentation.note_detail

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.farzane.securenote.domain.usecase.AddNoteUseCase
import com.farzane.securenote.domain.usecase.GetNoteByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DefaultNoteDetailComponent(
    componentContext: ComponentContext,
    private val noteId: Long?, // Null for new note, ID for edit
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val onFinished: () -> Unit
) : NoteDetailComponent, ComponentContext by componentContext {

    private val _state = MutableValue(NoteDetailState(id = noteId))
    override val state: Value<NoteDetailState> = _state

    private val scope = coroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        if (noteId != null) {
            loadNote(noteId)
        }
    }

    private fun loadNote(id: Long) {
        scope.launch {
            val note = getNoteByIdUseCase(id)

            if (note != null) {
                _state.value = _state.value.copy(
                    title = note.title,
                    content = note.content
                )
            }
        }
    }

    override fun onEvent(intent: NoteDetailIntent) {
        when (intent) {
            is NoteDetailIntent.UpdateTitle -> {
                _state.value = _state.value.copy(title = intent.title)
            }
            is NoteDetailIntent.UpdateContent -> {
                _state.value = _state.value.copy(content = intent.content)
            }
            is NoteDetailIntent.SaveNote -> saveNote()
            is NoteDetailIntent.Close -> onFinished()
        }
    }

    private fun saveNote() {
        val currentState = _state.value
        if (currentState.title.isBlank())
            return

        scope.launch {
            _state.value = currentState.copy(isSaving = true)

            addNoteUseCase(
                currentState.id,
                currentState.title,
                currentState.content)

            withContext(Dispatchers.Main) {
                onFinished()
            }
        }
    }
}
