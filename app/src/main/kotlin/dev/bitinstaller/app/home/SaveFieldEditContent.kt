package dev.bitinstaller.app.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.bitinstaller.app.save.SaveEditableValueKind
import dev.bitinstaller.app.save.explanation

internal data class SaveFieldEditContentState(
    val draft: SaveFieldEditDraft,
    val value: String,
    val validationError: String?,
)

internal data class SaveFieldEditContentActions(
    val onValueChange: (String) -> Unit,
    val onDismissRequest: () -> Unit,
    val onConfirm: () -> Unit,
)

@Composable
internal fun SaveFieldEditContent(
    state: SaveFieldEditContentState,
    actions: SaveFieldEditContentActions,
    modifier: Modifier = Modifier,
) {
    val draft = state.draft
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Edit ${draft.field.label}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = draft.field.path,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SaveFieldEditTip(draft = draft)
            OutlinedTextField(
                value = state.value,
                onValueChange = { next ->
                    actions.onValueChange(draft.field.valueKind.filterEditInput(next))
                },
                label = { Text(text = draft.field.valueKind.inputLabel()) },
                isError = state.validationError != null,
                supportingText = state.validationError?.let { error -> { Text(text = error) } },
                keyboardOptions = KeyboardOptions(keyboardType = draft.field.valueKind.keyboardType()),
                singleLine = draft.field.valueKind != SaveEditableValueKind.TEXT,
                minLines = if (draft.field.valueKind == SaveEditableValueKind.TEXT) 4 else 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Saving creates a .bitinstaller.bak backup before the save is replaced.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = actions.onConfirm,
                enabled = state.validationError == null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Save value")
            }
            TextButton(onClick = actions.onDismissRequest, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
private fun SaveFieldEditTip(draft: SaveFieldEditDraft) {
    val explanation = draft.field.explanation()
    val body = explanation?.description ?: draft.field.valueKind.editTip()
    val title = explanation?.category ?: "Value type: ${draft.field.valueKind.inputLabel()}"
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
