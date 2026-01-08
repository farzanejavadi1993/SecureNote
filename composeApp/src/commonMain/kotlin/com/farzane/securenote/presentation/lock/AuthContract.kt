package com.farzane.securenote.presentation.lock

data class AuthState(
    val isSetupMode: Boolean,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)