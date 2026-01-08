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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The UI screen for entering or creating a PIN.
 * This screen has two modes:
 * 1. Setup Mode: Asks the user to create and confirm a new PIN.
 * 2. Unlock Mode: Asks the user to enter their existing PIN.
 */
@Composable
fun LockScreen(
    isSetupMode: Boolean,
    onPinSuccess: (String) -> Unit, // Called when the PIN is set up or correctly entered.
    onCancel: () -> Unit
) {
    // --- Internal State ---
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") } // Used only in setup mode.

    // Manages the flow: 0=Unlock/Enter, 1=Create PIN, 2=Confirm PIN.
    var step by remember { mutableIntStateOf(if (isSetupMode) 1 else 0) }
    var error by remember { mutableStateOf<String?>(null) }

    // --- UI Layout ---
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spacer on the left to keep the title centered.
            // We make it invisible but it takes up the same space as the icon.
            Spacer(modifier = Modifier.size(48.dp))

            // 1. Title
            Text(
                text = when (step) {
                    0 -> "Enter Passcode"
                    1 -> "Create a Passcode"
                    2 -> "Confirm Passcode"
                    else -> ""
                },
                style = MaterialTheme.typography.headlineMedium
            )

            // Show the Close icon only in setup mode.
            if (isSetupMode) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel PIN Setup"
                    )
                }
            } else {
                // Another spacer on the right to keep the title centered in unlock mode.
                Spacer(modifier = Modifier.size(48.dp))
            }
        }


        Spacer(modifier = Modifier.height(32.dp))

        // 2. PIN Dots Indicator
        PinDots(
            pin = if (step == 2)
                confirmPin
            else
                pin
        )

        // Show an error message if the PINs don't match.
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(64.dp))

        // 3. Number Pad for input
        NumberPad(
            onNumberClick = { num ->
                error = null // Clear previous errors on new input.

                if (step == 2) {
                    // We are in the "Confirm PIN" step.
                    if (confirmPin.length < 4) confirmPin += num

                    if (confirmPin.length == 4) {
                        if (confirmPin == pin) {
                            // Success! The PINs match.
                            onPinSuccess(pin)
                        } else {
                            // Error: PINs do not match. Reset the process.
                            error = "PINs do not match. Try again."
                            pin = ""
                            confirmPin = ""
                            step = 1 // Go back to the "Create PIN" step.
                        }
                    }
                } else {
                    // We are in the "Unlock" or "Create PIN" step.
                    if (pin.length < 4) pin += num

                    if (pin.length == 4) {
                        if (step == 1) {
                            step = 2 // Move to the "Confirm PIN" step.
                        } else {
                            // In unlock mode, we just pass the completed PIN up for validation.
                            onPinSuccess(pin)
                        }
                    }
                }
            },
            onDeleteClick = {
                // Handle the backspace button.
                if (step == 2) {
                    if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                } else {
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            }
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
private fun NumberPad(onNumberClick: (String) -> Unit, onDeleteClick: () -> Unit) {
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
                        "" -> Spacer(modifier = Modifier.size(70.dp)) // Empty space for layout
                        "DEL" -> {
                            Box(
                                modifier = Modifier.size(70.dp).clickable { onDeleteClick() },
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
