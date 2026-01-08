package com.farzane.securenote.presentation.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.farzane.securenote.presentation.components.ConfirmationDialog

/**
 * The screen for watching and editing an existing note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    component: NoteDetailComponent,
    modifier: Modifier = Modifier
) {
    // Get the current screen state (title, content, etc.) from the component.
    val state by component.state.subscribeAsState()

    // A state to control the visibility of the delete confirmation dialog.
    var showDeleteDialog by remember { mutableStateOf(false) }
    // Setup for showing snackBars (like errors).
    val snackBarHostState = remember { SnackbarHostState() }


    LaunchedEffect(component) {
        component.effect.collect { detailEffect ->
            when (detailEffect) {
                is NoteDetailEffect.Error -> {
                    // Show the snackbar when an error label arrives
                    snackBarHostState.showSnackbar(detailEffect.message)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            NoteDetailAppBar(
                isEditing = state.id != null, // We are editing if the note has an ID.
                onClose = { component.onEvent(NoteDetailIntent.Close) },
                onSave = { component.onEvent(NoteDetailIntent.SaveNote) },
                onDelete = { showDeleteDialog = true }
            )
        }
    ){ padding ->
        // The main area with the text fields for title and content.
        NoteContent(
            modifier = Modifier.padding(padding),
            title = state.title,
            onTitleChange = { newTitle ->
                component.onEvent(NoteDetailIntent.UpdateTitle(newTitle))
            },
            content = state.content,
            onContentChange = { newContent ->
                component.onEvent(NoteDetailIntent.UpdateContent(newContent))
            }
        )

        // Show a confirmation dialog when the user tries to delete the note.
        if (showDeleteDialog) {
           ConfirmationDialog(
                title = "Delete Note",
                text = "Are you sure you want to delete this note? This cannot be undone.",
                confirmButtonText = "Delete",
                confirmButtonColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    component.onEvent(NoteDetailIntent.DeleteNote)
                    showDeleteDialog = false
                },
                onDismiss = {
                    showDeleteDialog = false
                }
            )
        }
    }
}


/**
 * The top app bar for the detail screen.
 * Shows a "Delete" button only when editing an existing note.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDetailAppBar(
    isEditing: Boolean,
    onClose: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            // Show a different title for new notes vs. existing notes.
            Text(if (isEditing) "Edit Note" else "New Note")
        },
        navigationIcon = {
            // The back button to close the screen.
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Only show the delete button if we are editing an existing note.
            if (isEditing) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Note",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            // The checkmark button to save the note.
            IconButton(onClick = onSave) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
    )
}

/**
 * The text fields for the note's title and content.
 */
@Composable
private fun NoteContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // The text field for the note's title.
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // The text field for the note's main content.
        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            label = { Text("Content") },
            modifier = Modifier.fillMaxSize()
        )
    }
}