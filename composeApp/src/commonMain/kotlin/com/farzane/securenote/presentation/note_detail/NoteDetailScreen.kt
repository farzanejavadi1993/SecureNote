package com.farzane.securenote.presentation.note_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(component: NoteDetailComponent) {
    val state by component.state.subscribeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == null) "New Note" else "Edit Note") },

                navigationIcon = {
                    IconButton(onClick = { component.onEvent(NoteDetailIntent.Close) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { component.onEvent(NoteDetailIntent.SaveNote) }) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = {
                    component.onEvent(NoteDetailIntent.UpdateTitle(it))
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.content,
                onValueChange = {
                    component.onEvent(NoteDetailIntent.UpdateContent(it))
                },
                label = { Text("Content") },
                modifier = Modifier.fillMaxSize().weight(1f),
                maxLines = 20
            )
        }
    }
}
