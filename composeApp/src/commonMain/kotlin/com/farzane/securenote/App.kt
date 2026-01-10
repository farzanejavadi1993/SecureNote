@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

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
import com.farzane.securenote.presentation.lock.LockScreen

/**
 * The root Composable of the entire application.
 *
 * It is responsible for:
 * 1. Setting up the theme and global listeners (like the inactivity timer).
 * 2. Deciding whether to show the Lock Screen or the main app content.
 * 3. Switching between the phone layout (single pane) and the tablet/desktop layout (Master-Detail).
 */
@Composable
fun App(rootComponent: RootComponent) {
    AppTheme {
        // A global Box that listens for any touch to reset the auto-lock timer.
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Get the current navigation stack state from the RootComponent.
            val stack by rootComponent.stack.subscribeAsState()
            val activeInstance = stack.active.instance

            // --- Security Check: Show Lock Screen if needed ---
            // If the active screen is the Lock Screen, show it on top of everything else.
            if (activeInstance is RootComponent.Child.Lock) {
                LockScreen(component = activeInstance.authComponent)
            } else {
                // --- Main App Content (if unlocked) ---
                // This checks the screen size to decide which layout to use.
                BoxWithConstraints {
                    val isSplitView = maxWidth > 600.dp

                    if (isSplitView) {
                        // --- TABLET / DESKTOP LAYOUT (Master-Detail) ---
                        val activeDetailWrapper by rootComponent.activeDetail.subscribeAsState()
                        val activeDetailComponent = activeDetailWrapper.noteDetailComponent

                        // Find the NoteList component from the stack.
                        val listChild = stack.items.find {
                            it.instance is RootComponent.Child.List
                        }?.instance

                        if (listChild is RootComponent.Child.List) {
                            MasterDetailLayout(
                                listComponent = listChild.noteListComponent,
                                detailComponent = activeDetailComponent
                            )
                        }
                    } else {
                        // --- PHONE LAYOUT (Single Pane Navigation) ---
                        Children(
                            stack = stack,
                            animation = stackAnimation(slide())
                        ) { child ->
                            when (val instance = child.instance) {
                                is RootComponent.Child.List -> NoteListScreen(instance.noteListComponent)
                                is RootComponent.Child.Detail -> NoteDetailScreen(instance.noteDetailComponent)
                                is RootComponent.Child.Lock -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}


