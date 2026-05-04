package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp

@Composable
fun RawEditor(
    rawJson: String,
    onRawJsonChanged: (String) -> Unit,
) {
    val syntaxColors =
        JsonSyntaxColors(
            key = MaterialTheme.colorScheme.primary,
            string = MaterialTheme.colorScheme.secondary,
            number = MaterialTheme.colorScheme.tertiary,
            literal = MaterialTheme.colorScheme.primary,
            punctuation = MaterialTheme.colorScheme.onSurfaceVariant,
        )

    OutlinedTextField(
        value = rawJson,
        onValueChange = onRawJsonChanged,
        label = { Text(text = "Raw JSON") },
        visualTransformation = JsonSyntaxTransformation(syntaxColors),
        textStyle =
            MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontStyle = FontStyle.Normal,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 240.dp, maxHeight = 350.dp),
    )
}
