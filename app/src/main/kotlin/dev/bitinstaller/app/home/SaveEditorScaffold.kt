package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary

private const val SAVE_EDITOR_SCREEN_BACKGROUND_ARGB = 0x99070707
private const val SAVE_EDITOR_DIVIDER_ARGB = 0x14FFFFFF
private const val SAVE_EDITOR_LABEL_ALPHA = 0.35f
private const val SAVE_EDITOR_HEADER_LETTER_SPACING = 2f
internal val SaveEditorHorizontalPadding = 24.dp

@Composable
internal fun SaveEditorFullscreenFrame(
    selectedTarget: SaveTargetUiState?,
    selectedSave: BitLifeSaveSummary?,
    successPopup: SaveSuccessPopup?,
    onDismissPopup: (SaveSuccessPopup) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(SAVE_EDITOR_SCREEN_BACKGROUND_ARGB))
                .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SaveEditorHeaderSection(
                title = selectedTarget?.name ?: "Save Editor",
                subtitle =
                    when {
                        selectedSave != null -> "SLOT EDITOR"
                        selectedTarget != null -> "SAVE SLOTS"
                        else -> "SAVE EDITOR"
                    },
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = SaveEditorHorizontalPadding),
                color = Color(SAVE_EDITOR_DIVIDER_ARGB),
            )
            content()
        }
        SaveEditorSuccessPopup(
            popup = successPopup,
            onDismiss = onDismissPopup,
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 28.dp),
        )
    }
}

@Composable
private fun SaveEditorHeaderSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SaveEditorHorizontalPadding, vertical = 20.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SAVE_EDITOR_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            letterSpacing = SAVE_EDITOR_HEADER_LETTER_SPACING.sp,
        )
    }
}
