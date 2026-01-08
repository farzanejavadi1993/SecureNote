package com.farzane.securenote.presentation.util

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult


import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
actual fun rememberPermissionLauncher(onResult: (Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

    return remember(launcher) {
        {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {

                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    onResult(true)
                } else {
                    launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } else {
                onResult(true)
            }
         }
    }
}
