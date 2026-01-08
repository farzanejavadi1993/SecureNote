package com.farzane.securenote.presentation.lock

import com.arkivanov.decompose.value.Value

interface AuthComponent {
    val state: Value<AuthState>
    fun onPinEnter(pin: String)
    fun onCancel()
}


