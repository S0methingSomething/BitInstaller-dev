package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val AUTO_DISMISS_DELAY_MS = 2000L

internal data class SaveSuccessPopup(
    val path: String,
    val token: Int,
    val message: String,
)

@Composable
internal fun SaveEditorSuccessPopup(
    selectedTarget: SaveTargetUiState?,
    dismissedTokens: Map<String, Int>,
    onDismiss: (SaveSuccessPopup) -> Unit,
) {
    val popup = selectedTarget?.saveSuccessPopup(dismissedTokens = dismissedTokens) ?: return
    LaunchedEffect(popup) {
        delay(AUTO_DISMISS_DELAY_MS)
        onDismiss(popup)
    }
    AlertDialog(
        onDismissRequest = { onDismiss(popup) },
        title = { Text(text = "Save updated") },
        text = { Text(text = popup.message) },
        confirmButton = {
            Button(onClick = { onDismiss(popup) }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Close")
            }
        },
    )
}

private fun SaveTargetUiState.saveSuccessPopup(dismissedTokens: Map<String, Int>): SaveSuccessPopup? =
    editMessages
        .asSequence()
        .mapNotNull { (path, message) ->
            val token = editMessageTokens[path] ?: return@mapNotNull null
            SaveSuccessPopup(path = path, token = token, message = message)
                .takeUnless { popup -> dismissedTokens[popup.path] == popup.token }
        }.firstOrNull()
