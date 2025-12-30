package com.farzane.securenote.presentation.theme


import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme {
    val context = LocalContext.current

    return when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = PrimaryDark,
            onPrimary = OnPrimaryDark,
            secondary = SecondaryDark,
            background = BackgroundDark,
            surface = SurfaceDark,
            error = ErrorDark
        )
        else -> lightColorScheme(
            primary = PrimaryLight,
            onPrimary = OnPrimaryLight,
            secondary = SecondaryLight,
            background = BackgroundLight,
            surface = SurfaceLight,
            error = ErrorLight
        )
    }
}
