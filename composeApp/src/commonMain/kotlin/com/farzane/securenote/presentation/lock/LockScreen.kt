package com.farzane.securenote.presentation.lock

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState

/**
 * The UI screen for entering or creating a PIN.
 * This screen has two modes:
 * 1. Setup Mode: Asks the user to create and confirm a new PIN.
 * 2. Unlock Mode: Asks the user to enter their existing PIN.
 */
@Composable
fun LockScreen(component: AuthComponent) {
    val state by component.state.subscribeAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Header Row ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.size(48.dp))
            Text(
                text = when (state.step) {
                    0 -> "Enter Passcode"
                    1 -> "Create a Passcode"
                    2 -> "Confirm Passcode"
                    else -> ""
                },
                style = MaterialTheme.typography.headlineMedium
            )
            if (state.isSetupMode) {
                IconButton(onClick = { component.onEvent(AuthIntent.Cancel) }) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
            }
            else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Dots ---
        PinDots(pin = state.currentInput)

        // --- Error ---
        state.error?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(64.dp))

        // --- Input ---
        NumberPad(
            onNumberClick = { component.onEvent(AuthIntent.EnterNumber(it)) },
            onDeleteClick = { component.onEvent(AuthIntent.DeleteNumber) }
        )
    }
}

/**
 * Displays the four dots that fill up as the user types their PIN.
 */
@Composable
private fun PinDots(pin: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(4) { index ->
            val isFilled = index < pin.length
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

/**
 * A simple number pad UI with numbers 0-9 and a delete button.
 */
@Composable
private fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(modifier = Modifier.size(70.dp))
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clickable { onDeleteClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⌫", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onNumberClick(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(key, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
