package com.farzane.securenote

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.farzane.securenote.presentation.list.NoteListScreen
import com.farzane.securenote.presentation.root.RootComponent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.farzane.securenote.presentation.detail.NoteDetailScreen
import com.farzane.securenote.presentation.root.MasterDetailLayout
import com.farzane.securenote.presentation.theme.AppTheme

/*@Composable
fun App(rootComponent: RootComponent) {
    AppTheme {
        BoxWithConstraints {
            val isSplitView = maxWidth > 600.dp

            if (isSplitView) {
                val stack by rootComponent.stack.subscribeAsState()
                val activeDetailWrapper by rootComponent.activeDetail.subscribeAsState()
                val activeDetail = activeDetailWrapper.component
                val listChild = stack.items.find {
                    it.instance is RootComponent.Child.List
                }?.instance ?: stack.active.instance


                if (listChild is RootComponent.Child.List) {
                    MasterDetailLayout(
                        listComponent = listChild.component,
                        detailComponent = activeDetail
                    )
                }
            } else {

                Children(
                    stack = rootComponent.stack,
                    animation = stackAnimation(slide())
                ) { child ->
                    when (val instance = child.instance) {
                        is RootComponent.Child.List -> NoteListScreen(instance.component)
                        is RootComponent.Child.Detail -> NoteDetailScreen(instance.component)
                    }
                }
            }
        }
    }
}*/




import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.farzane.securenote.domain.manager.AuthManager
import com.farzane.securenote.presentation.lock.LockScreen
import org.koin.compose.koinInject

@Composable
fun App(rootComponent: RootComponent) {
    val authManager = koinInject<AuthManager>()

    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                            authManager.updateActivity() // Reset 2-min timer
                        }
                    }
                }
        ) {
            val stack by rootComponent.stack.subscribeAsState()

            // 3. CHECK FOR LOCK SCREEN FIRST
            // If the active screen is the Lock Screen, show it immediately (blocking everything else)
            val activeInstance = stack.active.instance
            if (activeInstance is RootComponent.Child.Lock) {
                val state by activeInstance.component.state.subscribeAsState()
                LockScreen(
                    isSetupMode = state.isSetupMode,
                    onPinSuccess = { pin -> activeInstance.component.onPinEnter(pin) }
                )
            } else {

                BoxWithConstraints {
                    val isSplitView = maxWidth > 600.dp

                    if (isSplitView) {
                        val stack by rootComponent.stack.subscribeAsState()
                        val activeDetailWrapper by rootComponent.activeDetail.subscribeAsState()
                        val activeDetail = activeDetailWrapper.component
                        val listChild = stack.items.find {
                            it.instance is RootComponent.Child.List
                        }?.instance ?: stack.active.instance


                        if (listChild is RootComponent.Child.List) {
                            MasterDetailLayout(
                                listComponent = listChild.component,
                                detailComponent = activeDetail
                            )
                        } else {
                            // Fallback: If for some reason we are deep in navigation without a list,
                            // just render whatever is active (though unusual for this architecture)
                            Children(
                                stack = rootComponent.stack,
                                animation = stackAnimation(slide())
                            ) { child ->
                                when (val instance = child.instance) {
                                    is RootComponent.Child.List -> NoteListScreen(instance.component)
                                    is RootComponent.Child.Detail -> NoteDetailScreen(instance.component)
                                    is RootComponent.Child.Lock -> {

                                    }
                                }
                            }
                        }
                    } else {

                        Children(
                            stack = rootComponent.stack,
                            animation = stackAnimation(slide())
                        ) { child ->
                            when (val instance = child.instance) {
                                is RootComponent.Child.List -> NoteListScreen(instance.component)
                                is RootComponent.Child.Detail -> NoteDetailScreen(instance.component)
                                is RootComponent.Child.Lock -> {

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



