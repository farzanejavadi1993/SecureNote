package com.farzane.securenote.presentation.lock

import com.arkivanov.decompose.value.Value
import kotlinx.coroutines.flow.Flow

interface AuthComponent {
    val state: Value<AuthState>
    val effect: Flow<AuthEffect>
    fun onEvent(intent: AuthIntent)
}

