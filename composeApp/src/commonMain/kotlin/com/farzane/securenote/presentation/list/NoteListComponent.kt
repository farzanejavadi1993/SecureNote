package com.farzane.securenote.presentation.list

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the public "contract" for the Note List's logic component.
 * It acts as a clean boundary between the UI (View) and the business logic.
 *
 * Think of it as the remote control for the screen:
 * - The UI can observe the `state` to know what to display.
 * - The UI can send user actions via the `onEvent` function.
 *
 * The UI (`NoteListScreen`) doesn't know or care about the implementation details
 * in `DefaultNoteListComponent`; it only interacts with this clean interface.
 */
interface NoteListComponent {

    /**
     * An observable stream of the screen's state (`NoteListState`).
     *
     * The UI "subscribes" to this `Value`. Whenever the state changes (e.g., notes are
     * loaded, the user enters selection mode), the UI will be automatically notified
     * and will redraw itself to reflect the new state.
     *
     * It's a `Value` (from Decompose), which is similar to a `StateFlow` or `LiveData`.
     */
    val state: Value<NoteListState>

    val effect: Flow<NoteListEffect>

    /**
     * The single entry point for all user actions (Intents) from the UI.
     *
     * When a user taps a button, long-presses a note, or performs any action,
     * the UI calls this function with the corresponding `NoteListIntent`
     * (e.g., `onEvent(NoteListIntent.DeleteNote(id = 123))`).
     *
     * This follows the MVI (Model-View-Intent) pattern, creating a clear,
     * unidirectional data flow.
     */
    fun onEvent(intent: NoteListIntent)
}
