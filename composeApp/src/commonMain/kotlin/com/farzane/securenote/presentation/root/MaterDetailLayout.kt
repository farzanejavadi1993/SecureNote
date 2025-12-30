package com.farzane.securenote.presentation.root

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.farzane.securenote.presentation.note_detail.NoteDetailComponent
import com.farzane.securenote.presentation.note_detail.NoteDetailScreen
import com.farzane.securenote.presentation.note_list.NoteListComponent
import com.farzane.securenote.presentation.note_list.NoteListScreen

@Composable
fun MasterDetailLayout(
    listComponent: NoteListComponent,
    detailComponent: NoteDetailComponent?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize()) {
        val listModifier = Modifier
            .fillMaxHeight()
            .width(350.dp)

        NoteListScreen(
            component = listComponent,
            modifier = listModifier
        )


        VerticalDivider()

        androidx.compose.material3.Surface(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            if (detailComponent != null) {
                NoteDetailScreen(
                    component = detailComponent,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                EmptyDetailScreen(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun EmptyDetailScreen(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.Text("Select a note to view details")
    }
}
