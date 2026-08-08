package com.mattschoe.apptemplate.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import apptemplate.shared.generated.resources.Res
import apptemplate.shared.generated.resources.action_cancel
import apptemplate.shared.generated.resources.action_confirm
import org.jetbrains.compose.resources.stringResource

/**
 * Shared dialogs live in this one file rather than one file each — they are small,
 * and keeping them together makes it obvious when a new one duplicates an old one.
 */

@Composable
fun TextInputDialog(
    title: String,
    label: String,
    confirmEnabled: (String) -> Boolean = { it.isNotBlank() },
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(label) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(text) },
                enabled = confirmEnabled(text)
            ) {
                Text(stringResource(Res.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}
