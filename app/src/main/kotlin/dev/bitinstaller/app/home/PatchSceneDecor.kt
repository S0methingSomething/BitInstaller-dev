package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private const val HANDLE_WIDTH_FRACTION: Float = 0.18f
private const val HANDLE_ALPHA: Float = 0.18f
private const val HEADER_ALPHA: Float = 0.92f
private const val HANDLE_HEIGHT_DP: Int = 6

@Composable
fun ColumnScope.PatchSceneHandle(alpha: Float) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = HANDLE_ALPHA * alpha),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp),
            modifier = Modifier
                .fillMaxWidth(HANDLE_WIDTH_FRACTION)
                .height(HANDLE_HEIGHT_DP.dp),
        ) {}
    }
}

@Composable
fun PatchSceneHeader(
    target: PatchTargetUiState,
    alpha: Float,
) {
    val subheadColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = HEADER_ALPHA * alpha)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = target.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "MonetizationVars editor",
            style = MaterialTheme.typography.bodySmall,
            color = subheadColor,
        )
    }
}
