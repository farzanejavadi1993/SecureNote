package com.farzane.securenote.presentation.root

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farzane.securenote.presentation.detail.NoteDetailComponent
import com.farzane.securenote.presentation.detail.NoteDetailScreen
import com.farzane.securenote.presentation.list.NoteListComponent
import com.farzane.securenote.presentation.list.NoteListScreen


/**
 * A layout that shows two panels side-by-side on large screens.
 * The left panel is the "master" list, and the right is the "detail" view.
 */
@Composable
fun MasterDetailLayout(
    listComponent: NoteListComponent,
    detailComponent: NoteDetailComponent?, // The detail can be null if no note is selected.
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        // --- Left Panel (Master) ---
        // This is the list of notes, with a fixed width.
        val listModifier = Modifier
            .fillMaxHeight()
            .width(350.dp)

        NoteListScreen(
            component = listComponent,
            modifier = listModifier
        )

       // A thin vertical line to separate the two panels.
        VerticalDivider()

        // --- Right Panel (Detail) ---
        // This panel takes up the rest of the available space.
        androidx.compose.material3.Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            // If a note is selected, show its details.
            if (detailComponent != null) {
                NoteDetailScreen(
                    component = detailComponent,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // If no note is selected, show a placeholder message.
                EmptyDetailScreen(Modifier.fillMaxSize())
            }
        }
    }
}


/**
 * A simple placeholder screen shown when no note is selected in the detail panel.
 */
@Composable
fun EmptyDetailScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text("Select a note to view details")
    }
}
