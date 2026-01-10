package com.farzane.securenote.presentation.lock

data class AuthState(
    val isSetupMode: Boolean,
    val step: Int = 0, // 0: Enter, 1: Create, 2: Confirm
    val currentInput: String = "",
    val pinToConfirm: String = "",
    val error: String? = null
)

sealed interface AuthEffect {
    data object Authenticated : AuthEffect
}

sealed interface AuthIntent {
    data class EnterNumber(val number: String) : AuthIntent
    data object DeleteNumber : AuthIntent
    data object Cancel : AuthIntent
}


