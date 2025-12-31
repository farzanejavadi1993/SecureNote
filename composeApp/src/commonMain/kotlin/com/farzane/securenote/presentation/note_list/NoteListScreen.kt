package com.farzane.securenote.presentation.note_list

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.farzane.securenote.core.util.rememberPermissionLauncher
import com.farzane.securenote.domain.model.Note
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    component: NoteListComponent,
    modifier: Modifier = Modifier
) {

    val state by component.state.subscribeAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }


    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let { message ->
            snackBarHostState.showSnackbar(message = message)
        }
    }

    val launchExport = rememberPermissionLauncher { isGranted ->
        if (isGranted) {
            component.onEvent(NoteListIntent.ExportNotes)
        } else {
            scope.launch {
                snackBarHostState.showSnackbar("Permission needed to save file.")
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) },
        topBar = {

            if (state.isMultiSelectionMode) {
                TopAppBar(
                    title = { Text("${state.selectedNoteIds.size} Selected") },
                    navigationIcon = {
                        IconButton(
                            onClick =
                                { component.onEvent(NoteListIntent.ClearSelectionMode) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close Selection"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                showExportDialog = true
                                //launchExport()
                            }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Export Selected"
                            )
                        }


                        IconButton(onClick = {
                            showDeleteDialog = true
                            /*state.selectedNoteIds.forEach { id ->
                                component.onEvent(NoteListIntent.DeleteNote(id))
                            }
                            component.onEvent(NoteListIntent.ClearSelectionMode)*/
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Secure Notes") },
                    modifier = Modifier.shadow(elevation = 8.dp),

                    actions = {
                        IconButton(onClick = {
                            showExportDialog = true
                            //launchExport()
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Export")
                        }
                    }
                )
            }


        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isAddDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.notes.isEmpty()) {
                Text(
                    text = "No notes found. Create one!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {


                    items(state.notes) { note ->
                        val isSelected = state.selectedNoteIds.contains(note.id)
                        val isInSelectionMode = state.isMultiSelectionMode

                        NoteItemRow(
                            note = note,
                            isSelected,
                            isInSelectionMode,
                            onNoteClick = {
                                if (isInSelectionMode) {
                                    component.onEvent(
                                        NoteListIntent.ToggleNoteSelection(
                                            note.id ?: 0
                                        )
                                    )
                                } else {
                                    component.onEvent(NoteListIntent.SelectNote(note.id ?: 0))
                                }
                            },
                            /*onDeleteClick = {
                                component.onEvent(NoteListIntent.DeleteNote(note.id ?: 0))
                            },*/
                            onNoteLongClick = {
                                if (!isInSelectionMode) {
                                    component.onEvent(
                                        NoteListIntent.ToggleSelectionMode(
                                            note.id ?: 0
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }

        if (isAddDialogOpen) {
            AddNoteDialog(
                onDismiss = { isAddDialogOpen = false },
                onConfirm = { title, content ->
                    component.onEvent(NoteListIntent.AddNote(title, content))
                    isAddDialogOpen = false
                },
                onError = { errorMessage ->
                    scope.launch {
                        snackBarHostState.showSnackbar(errorMessage)
                    }
                }
            )
        }


        // Export Confirmation Dialog
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Notes") },
                text = {
                    val count = if (state.isMultiSelectionMode) state.selectedNoteIds.size else state.notes.size
                    Text("Are you sure you want to export $count notes to a file?")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            launchExport()
                            showExportDialog = false
                        }
                    ) {
                        Text("Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Notes") },
                text = {
                    Text("Are you sure you want to delete ${state.selectedNoteIds.size} selected notes? This cannot be undone.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            state.selectedNoteIds.forEach { id ->
                                component.onEvent(NoteListIntent.DeleteNote(id))
                            }
                            component.onEvent(NoteListIntent.ClearSelectionMode)
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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

@Composable
fun NoteItemRow(
    note: Note,
    isSelected: Boolean = false,
    isInSelectionMode: Boolean = false,
    onNoteClick: () -> Unit,
    onNoteLongClick: () -> Unit
) {
    val cardColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = onNoteClick,
                onLongClick = onNoteLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            /*IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }*/
        }
    }
}


@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Note") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onConfirm(title, content)
                    } else {
                        onError("Title and Content cannot be empty")
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },


        )
}
