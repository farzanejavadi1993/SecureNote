package com.farzane.securenote.presentation.lock

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.farzane.securenote.domain.manager.AuthManager

/**
 * The logic component for the Lock Screen.
 * It decides whether the user is setting up a new PIN or entering an existing one,
 * and handles the validation logic.
 */
class DefaultAuthComponent(
    componentContext: ComponentContext,
    private val authManager: AuthManager,
    private val onAuthenticated: () -> Unit  // Callback to navigate away when unlocked.
) : AuthComponent, ComponentContext by componentContext {

    // Initialize the state based on whether a PIN already exists.
    // If no PIN exists, we start in "Setup Mode".
    private val _state = MutableValue(
        AuthState(isSetupMode = !authManager.hasPin())
    )
    override val state: Value<AuthState> = _state

    /**
     * Called when the user finishes entering a PIN on the number pad.
     */
    override fun onPinEnter(pin: String) {
        if (_state.value.isSetupMode) {
            // Case 1: Setting up a new PIN.
            // Save it securely and unlock the app.
             authManager.savePin(pin)
             onAuthenticated()
        } else {
            // Case 2: Unlocking with an existing PIN.
             if (authManager.validatePin(pin)) {
                 // Correct PIN: Unlock the app.
                 onAuthenticated()
             } else {
                 // Incorrect PIN: Show an error message to the user.
                 _state.value = _state.value.copy(error = "Wrong PIN")
             }
        }
    }
}
