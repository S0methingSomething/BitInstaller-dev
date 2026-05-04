package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PatchEditorToolbar(
    editorMode: EditorMode,
    onModeSelected: (EditorMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EditorSectionLabel(text = "Editor mode")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            EditorModeButton(
                label = "Simplified",
                selected = editorMode == EditorMode.SIMPLIFIED,
                onClick = { onModeSelected(EditorMode.SIMPLIFIED) },
                modifier = Modifier.weight(1f),
            )
            EditorModeButton(
                label = "Raw JSON",
                selected = editorMode == EditorMode.RAW,
                onClick = { onModeSelected(EditorMode.RAW) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun BulkPatchPanel(onUnlockAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        EditorSectionLabel(text = "Bulk patch")
        OutlinedButton(
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            onClick = onUnlockAll,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Enable all unlocks", fontWeight = FontWeight.SemiBold)
                Text(
                    text = "Review changes before saving",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
internal fun EditorContentLabel(editorMode: EditorMode) {
    EditorSectionLabel(
        text =
            when (editorMode) {
                EditorMode.SIMPLIFIED -> "Unlock flags"
                EditorMode.RAW -> "Data stream"
            },
    )
}

@Composable
internal fun PatchEditorFooter(
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onExportRawJson: () -> Unit,
    onSave: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Close")
            }
            OutlinedButton(
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                onClick = onExportRawJson,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Export JSON")
            }
        }
        Button(
            enabled = !isSaving,
            onClick = onSave,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.sizeIn(maxWidth = 18.dp, maxHeight = 18.dp))
            } else {
                Text(text = "Save encrypted file")
            }
        }
    }
}

@Composable
private fun EditorModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                ),
            onClick = onClick,
            modifier = modifier.heightIn(min = 44.dp),
        ) {
            Text(text = label)
        }
    } else {
        OutlinedButton(
            colors =
                ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            onClick = onClick,
            modifier = modifier.heightIn(min = 44.dp),
        ) {
            Text(text = label)
        }
    }
}

@Composable
private fun EditorSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.4.sp,
            ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
