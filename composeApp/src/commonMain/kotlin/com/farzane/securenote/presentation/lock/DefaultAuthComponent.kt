package com.farzane.securenote.presentation.lock

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.farzane.securenote.domain.manager.AuthManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * The logic component for the Lock Screen.
 * It decides whether the user is setting up a new PIN or entering an existing one,
 * and handles the validation logic.
 */
class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val authManager: AuthManager,
    private val onAuthenticated: () -> Unit,
    private val onCancelled: () -> Unit
) : AuthComponent, ComponentContext by componentContext {

    private val _state = MutableValue(
        AuthState(isSetupMode = !authManager.hasPin(), step = if (!authManager.hasPin()) 1 else 0)
    )
    override val state: Value<AuthState> = _state

    private val _effect = Channel<AuthEffect>()
    override val effect = _effect.receiveAsFlow()

    override fun onEvent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.EnterNumber -> handleInput(intent.number)
            is AuthIntent.DeleteNumber -> handleDelete()
            is AuthIntent.Cancel -> onCancelled()
        }
    }

    private fun handleInput(num: String) {
        val current = _state.value
        if (current.currentInput.length >= 4)
            return

        val newInput = current.currentInput + num
        _state.value = current.copy(currentInput = newInput, error = null)

        if (newInput.length == 4) {
            processCompletedInput(newInput)
        }
    }

    private fun processCompletedInput(input: String) {
        val current = _state.value
        when (current.step) {
            0 -> { // UNLOCK MODE
                if (authManager.validatePin(input)) {
                    onAuthenticated()
                } else {
                    _state.value = current.copy(currentInput = "", error = "Wrong PIN")
                }
            }
            1 -> { // SETUP: FIRST ENTER
                _state.value = current.copy(step = 2, pinToConfirm = input, currentInput = "")
            }
            2 -> { // SETUP: CONFIRMATION
                if (input == current.pinToConfirm) {
                    authManager.savePin(input)
                    onAuthenticated()
                } else {
                    _state.value = current.copy(step = 1, currentInput = "", pinToConfirm = "", error = "PINs do not match")
                }
            }
        }
    }

    private fun handleDelete() {
        _state.value = _state.value.copy(
            currentInput = _state.value.currentInput.dropLast(1),
            error = null
        )
    }
}
