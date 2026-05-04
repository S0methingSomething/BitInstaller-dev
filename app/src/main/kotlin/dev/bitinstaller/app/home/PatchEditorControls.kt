package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EditorControlShape = RoundedCornerShape(6.dp)

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
        Surface(
            onClick = onUnlockAll,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            shape = EditorControlShape,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Text(text = "Enable all unlocks", fontWeight = FontWeight.Medium)
                Text(
                    text = "Bulk command - review before saving",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun EditorContentLabel() {
    EditorSectionLabel(text = "Unlock flags")
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
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                onClick = onExportRawJson,
                shape = EditorControlShape,
                modifier = Modifier.weight(1f),
            ) {
                Text(text = "Export JSON")
            }
        }
        Button(
            enabled = !isSaving,
            onClick = onSave,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            shape = EditorControlShape,
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            onClick = onClick,
            shape = EditorControlShape,
            modifier = modifier.heightIn(min = 44.dp),
        ) {
            Text(text = label)
        }
    } else {
        OutlinedButton(
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.025f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            onClick = onClick,
            shape = EditorControlShape,
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
