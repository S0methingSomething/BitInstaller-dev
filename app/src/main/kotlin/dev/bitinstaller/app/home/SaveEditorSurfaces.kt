package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal val SaveEditorPanelShape = RoundedCornerShape(24.dp)
internal val SaveEditorCardShape = RoundedCornerShape(20.dp)
internal val SaveEditorControlShape = RoundedCornerShape(16.dp)
internal val SaveEditorPillShape = RoundedCornerShape(999.dp)

@Composable
internal fun SaveEditorPanel(
    modifier: Modifier = Modifier,
    shape: Shape = SaveEditorPanelShape,
    containerAlpha: Float = 0.05f,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = containerAlpha),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = shape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier,
    ) {
        Column(content = content)
    }
}
