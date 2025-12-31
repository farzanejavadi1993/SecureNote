package com.farzane.securenote.core.util


import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    return {
        onResult(true)
    }
}
