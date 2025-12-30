package com.farzane.securenote

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import com.farzane.securenote.presentation.note_list.NoteListScreen
import com.farzane.securenote.presentation.root.RootComponent
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.farzane.securenote.presentation.note_detail.NoteDetailScreen
import com.farzane.securenote.presentation.root.MasterDetailLayout
import com.farzane.securenote.presentation.theme.AppTheme

@Composable
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
}



