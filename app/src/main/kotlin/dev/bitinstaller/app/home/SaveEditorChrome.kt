package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun SaveScanPrompt() {
    SaveEditorPanel(shape = SaveEditorControlShape, containerAlpha = 0.045f, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Scan to load each current life slot. Historical age backups are ignored.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
        )
    }
}

@Composable
internal fun SaveSlotBadge(slotName: String) {
    SaveEditorPanel(shape = SaveEditorPillShape, containerAlpha = 0.075f) {
        Text(
            text = slotName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
