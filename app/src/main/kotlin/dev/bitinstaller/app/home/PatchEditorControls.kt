package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val EDITOR_SUBTLE_SURFACE_ALPHA = 0.05f
private val EditorControlShape = RoundedCornerShape(16.dp)

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
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = EDITOR_SUBTLE_SURFACE_ALPHA),
            contentColor = MaterialTheme.colorScheme.onSurface,
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PatchEditorFooter(
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onExportRawJson: () -> Unit,
    onSave: () -> Unit,
) {
    val primaryWeight by animateExpressiveFontWeight(
        isActive = !isSaving,
        restWeight = FontWeight.SemiBold.weight,
        activeWeight = FontWeight.Black.weight,
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
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
                LoadingIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(text = "Save encrypted file", fontWeight = FontWeight(primaryWeight))
            }
        }
        FilledTonalButton(
            onClick = onExportRawJson,
            shape = EditorControlShape,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(text = "Export JSON")
        }
        TextButton(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
            Text(text = "Close")
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
    val textWeight by animateExpressiveFontWeight(
        isActive = selected,
        restWeight = FontWeight.Medium.weight,
        activeWeight = FontWeight.Black.weight,
    )

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
            Text(text = label, fontWeight = FontWeight(textWeight))
        }
    } else {
        FilledTonalButton(
            colors =
                ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = EDITOR_SUBTLE_SURFACE_ALPHA),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            onClick = onClick,
            shape = EditorControlShape,
            modifier = modifier.heightIn(min = 44.dp),
        ) {
            Text(text = label, fontWeight = FontWeight(textWeight))
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
