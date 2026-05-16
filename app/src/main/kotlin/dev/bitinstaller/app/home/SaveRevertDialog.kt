package dev.bitinstaller.app.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.bitinstaller.app.save.BitLifeSaveSummary

@Composable
internal fun SaveRevertDialog(
    save: BitLifeSaveSummary,
    target: SaveTargetUiState?,
    onDismissRequest: () -> Unit,
    onConfirm: (SaveTargetUiState, BitLifeSaveSummary) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Revert ${save.slotName}?") },
        text = {
            Text(
                text =
                    "Restore this save from its .bitinstaller.bak backup. " +
                        "Any edits made after that backup will be lost.",
            )
        },
        confirmButton = {
            Button(enabled = target != null, onClick = { target?.let { onConfirm(it, save) } }) {
                Text(text = "Revert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Cancel")
            }
        },
    )
}
