package com.farzane.securenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.farzane.securenote.di.initKoin
import com.farzane.securenote.presentation.root.DefaultRootComponent
import org.koin.android.ext.koin.androidContext
import android.view.WindowManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        if (org.koin.core.context.GlobalContext.getOrNull() == null) {
            initKoin {
                androidContext(applicationContext)
            }
        }

        val root = DefaultRootComponent(
            componentContext = defaultComponentContext()
        )

        setContent {
            App(rootComponent = root)
        }
    }
}

