package com.farzane.securenote.core.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit
