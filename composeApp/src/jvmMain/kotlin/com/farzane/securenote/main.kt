package com.farzane.securenote

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.farzane.securenote.di.initKoin
import com.farzane.securenote.presentation.root.DefaultRootComponent
import javax.swing.SwingUtilities

fun main() {

    initKoin()

    val lifecycle = LifecycleRegistry()

    val root = runOnUiThread {
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle)
        )
    }

    application {
        val windowState = androidx.compose.ui.window.rememberWindowState()
        LifecycleController(lifecycle, windowState)
        Window(onCloseRequest = ::exitApplication, title = "Secure Notes", state = windowState) {
            App(rootComponent = root)
        }

    }
}


fun <T> runOnUiThread(block: () -> T): T {
    if (SwingUtilities.isEventDispatchThread()) {
        return block()
    }

    var result: T? = null
    SwingUtilities.invokeAndWait {
        result = block()
    }
    return result!!
}
