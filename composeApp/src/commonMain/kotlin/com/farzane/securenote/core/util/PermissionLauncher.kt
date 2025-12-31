package com.farzane.securenote.core.util

import androidx.compose.runtime.Composable

/**
 * A multiplatform composable that provides a launcher for requesting permissions.
 * On Android, this will show a system permission dialog.
 * On Desktop, it will grant permission immediately as it's not needed.
 */
@Composable
expect fun rememberPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit
