package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val EDITOR_SUBTLE_SURFACE_ALPHA = 0.05f
private const val EDITOR_ACTIVE_ALPHA = 0.12f
private const val EDITOR_LABEL_ALPHA = 0.30f
private const val EDITOR_TEXT_ALPHA = 0.85f
private val EditorControlShape = RoundedCornerShape(12.dp)

@Composable
internal fun PatchEditorToolbar(
    editorMode: EditorMode,
    onModeSelected: (EditorMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        EditorSectionLabel(text = "Editor mode")
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        EditorSectionLabel(text = "Bulk patch")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = EDITOR_SUBTLE_SURFACE_ALPHA), shape = EditorControlShape)
                    .clickable(onClick = onUnlockAll)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(text = "\u26A1", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(1.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable all unlocks",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = EDITOR_TEXT_ALPHA),
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Bulk command — review before saving",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = EDITOR_LABEL_ALPHA),
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                    .heightIn(min = 46.dp),
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
        ) {
            Text(text = "Export JSON")
        }
        TextButton(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp)) {
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
    val bgAlpha = if (selected) EDITOR_ACTIVE_ALPHA else EDITOR_SUBTLE_SURFACE_ALPHA
    val textColor = if (selected) Color.White else Color.White.copy(alpha = EDITOR_LABEL_ALPHA)

    TextButton(
        onClick = onClick,
        shape = EditorControlShape,
        modifier =
            modifier
                .heightIn(min = 38.dp)
                .background(Color.White.copy(alpha = bgAlpha), shape = EditorControlShape),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight(textWeight),
            color = textColor,
        )
    }
}

@Composable
private fun EditorSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
            ),
        color = Color.White.copy(alpha = EDITOR_LABEL_ALPHA),
        fontWeight = FontWeight.Bold,
    )
}
