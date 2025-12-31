package com.farzane.securenote.presentation.list

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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import com.farzane.securenote.presentation.components.ConfirmationDialog
import kotlinx.coroutines.launch

/**
 * The main screen that displays the list of all notes.
 * This composable is the "View" part of the MVI pattern for the note list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    component: NoteListComponent,
    modifier: Modifier = Modifier
) {
    // Get the current screen state (notes, loading status, etc.) from the component.
    val state by component.state.subscribeAsState()

    // Setup for showing snackBars (like for export messages or errors).
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- State for Dialogs ---
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Shows a snackbar message when the export status changes.
    LaunchedEffect(state.exportMessage) {
        state.exportMessage?.let { message ->
            snackBarHostState.showSnackbar(message = message)
        }
    }

    // Prepares the permission request for saving files.
    // This will ask for permission on older Android versions or grant it automatically on newer ones.
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
            // Show a different app bar when the user is selecting multiple notes.
            if (state.isMultiSelectionMode) {
                SelectionAppBar(
                    selectedCount = state.selectedNoteIds.size,
                    onClose = { component.onEvent(NoteListIntent.ClearSelectionMode) },
                    onExport = { showExportDialog = true },
                    onDelete = { showDeleteDialog = true }
                )
            } else {
                NormalAppBar(
                    onExport = { showExportDialog = true },
                    onLock = { component.onEvent(NoteListIntent.LockApp) }
                )
            }


        },
        floatingActionButton = {
            // The "Add" button to open the new note dialog.
            FloatingActionButton(onClick = { isAddDialogOpen = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {

            when {
                // Show a spinner while notes are loading.
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                // Show a message if there are no notes.
                state.notes.isEmpty() -> {
                    Text(
                        text = "No notes found. Create one!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Show the list of notes.
                else ->
                    NoteList(
                        notes = state.notes,
                        selectedIds = state.selectedNoteIds,
                        isInSelectionMode = state.isMultiSelectionMode,
                        onNoteClick = { noteId ->
                            // If we're already selecting, a tap toggles the check.
                            // Otherwise, it opens the detail screen.
                            if (state.isMultiSelectionMode) {
                                component.onEvent(NoteListIntent.ToggleNoteSelection(noteId))
                            } else {
                                component.onEvent(NoteListIntent.SelectNote(noteId))
                            }
                        },
                        onNoteLongClick = { noteId ->
                            // A long click always starts selection mode.
                            if (!state.isMultiSelectionMode) {
                                component.onEvent(NoteListIntent.ToggleSelectionMode(noteId))
                            }
                        }
                    )
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
        val count = if (state.isMultiSelectionMode) state.selectedNoteIds.size else state.notes.size
        ConfirmationDialog(

            "Export Notes",
            if (state.isMultiSelectionMode) {
                "Are you sure you want to export $count selected notes?"
            } else {
                "Are you sure you want to export all $count notes?"
            },
            "Export",
            confirmButtonColor = MaterialTheme.colorScheme.error,

            onConfirm = {
                launchExport()
                showExportDialog = false
            },
            onDismiss = {
                showExportDialog = false
            }
        )

    }

    // Delete Confirmation Dialog
    // Shows the delete confirmation dialog when needed.
    if (showDeleteDialog) {
        val count = state.selectedNoteIds.size
        ConfirmationDialog(
            "Delete Notes",
            "Are you sure you want to delete $count selected notes? This cannot be undone.",
            "Delete",
            confirmButtonColor = MaterialTheme.colorScheme.error,


            onConfirm = { // Tell the component to delete each selected note.
                state.selectedNoteIds.forEach { id ->
                    component.onEvent(NoteListIntent.DeleteNote(id))
                }
                // Exit selection mode after deleting.
                component.onEvent(NoteListIntent.ClearSelectionMode)

                // Close the dialog.
                showDeleteDialog = false
            },
            onDismiss = {
                // Just close the dialog if the user cancels.
                showDeleteDialog = false
            }
        )

    }
}


/**
 * Displays the scrollable list of notes.
 */
@Composable
 fun NoteList(
    notes: List<Note>,
    selectedIds: Set<Long>,
    isInSelectionMode: Boolean,
    onNoteClick: (Long) -> Unit,
    onNoteLongClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id ?: 0 }) { note ->
            NoteItemRow(
                note = note,
                isSelected = selectedIds.contains(note.id),
                isInSelectionMode = isInSelectionMode,
                onNoteClick = { onNoteClick(note.id ?: 0) },
                onNoteLongClick = { onNoteLongClick(note.id ?: 0) }
            )
        }
    }
}


/**
 * The top app bar shown when selecting multiple items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionAppBar(selectedCount: Int, onClose: () -> Unit, onExport: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        title = { Text("$selectedCount Selected") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close Selection")
            }
        },
        actions = {
            IconButton(onClick = onExport) {
                Icon(Icons.Default.Share, contentDescription = "Export Selected")
            }
            IconButton(onClick = onDelete) {
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
}

/**
 * The top app bar shown during normal use.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalAppBar(onExport: () -> Unit, onLock: () -> Unit) {
    TopAppBar(
        title = { Text("Secure Notes") },
        modifier = Modifier.shadow(elevation = 4.dp),
        actions = {
            IconButton(onClick = onExport) {
                Icon(Icons.Default.Share, contentDescription = "Export")
            }
            IconButton(onClick = onLock) {
                Icon(Icons.Default.Lock, contentDescription = "Lock App")
            }
        }
    )
}


/**
 * A single row representing one note in the list.
 * It changes its appearance if it's selected.
 */
@Composable
private fun NoteItemRow(
    note: Note,
    isSelected: Boolean = false,
    isInSelectionMode: Boolean = false,
    onNoteClick: () -> Unit,
    onNoteLongClick: () -> Unit
) {
    // Show a different background color to highlight selected notes.
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

            // This modifier handles both a regular tap (onClick) and a long press (onLongClick).
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
            // Show a checkbox at the start of the row only when in selection mode.
            if (isInSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null // The whole row is clickable, so we don't need a separate click here.
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            // The main content of the note (Title and a preview of the content).
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis // Add "..." if the title is too long.
                )
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AddNoteDialog(
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