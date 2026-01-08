package com.farzane.securenote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.farzane.securenote.presentation.root.DefaultRootComponent
import android.view.WindowManager
import com.farzane.securenote.di.appModule
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )


        val root = DefaultRootComponent(
            componentContext = defaultComponentContext()
        )

        setContent {
            App(rootComponent = root)
        }
    }
}

