package com.farzane.securenote.presentation.lock

import com.arkivanov.decompose.value.Value

interface AuthComponent {
    val state: Value<AuthState>
    fun onPinEnter(pin: String)
}

data class AuthState(
    val isSetupMode: Boolean,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)
