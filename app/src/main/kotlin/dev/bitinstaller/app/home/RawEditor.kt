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
    OutlinedTextField(
        value = rawJson,
        onValueChange = onRawJsonChanged,
        label = { Text(text = "JSON stream") },
        textStyle = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontStyle = FontStyle.Normal,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .sizeIn(minHeight = 260.dp, maxHeight = 460.dp),
    )
}
