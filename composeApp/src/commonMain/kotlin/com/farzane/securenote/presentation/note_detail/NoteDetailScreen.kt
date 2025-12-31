package com.farzane.securenote.presentation.note_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    component: NoteDetailComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                    if (state.id != null) {
                        IconButton(onClick = {
                            //component.onEvent(NoteDetailIntent.DeleteNote)
                            showDeleteDialog = true
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Note",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
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


        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note") },
                text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            component.onEvent(NoteDetailIntent.DeleteNote)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
