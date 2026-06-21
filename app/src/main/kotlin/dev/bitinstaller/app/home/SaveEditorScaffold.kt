package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary

private const val SAVE_EDITOR_SCREEN_BACKGROUND_ARGB = 0xFF070707
private const val SAVE_EDITOR_SCREEN_BACKGROUND_ALPHA = 0.60f
private const val SAVE_EDITOR_DIVIDER_ARGB = 0x14FFFFFF
private const val SAVE_EDITOR_LABEL_ALPHA = 0.35f
private const val SAVE_EDITOR_BADGE_ALPHA = 0.08f
private const val SAVE_EDITOR_BADGE_TEXT_ALPHA = 0.72f
private const val SAVE_EDITOR_HEADER_LETTER_SPACING = 2f
private const val BACK_TRANSLATION_FRACTION = 0.25f
private const val BACK_SCALE_FACTOR = 0.06f
private const val BACK_SCALE_OFFSET = 0.94f
private const val BACK_ALPHA_OFFSET = 0.30f
private const val BACK_ALPHA_MIN = 0.70f

internal data class SaveEditorFrameConfig(
    val selectedTarget: SaveTargetUiState?,
    val selectedSave: BitLifeSaveSummary?,
    val successPopup: SaveSuccessPopup?,
    val backProgress: Float = 0f,
)

internal val SaveEditorHorizontalPadding = 24.dp
private val SaveEditorHeaderPadding = 24.dp

@Composable
internal fun SaveEditorFullscreenFrame(
    config: SaveEditorFrameConfig,
    onDismissPopup: (SaveSuccessPopup) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color(SAVE_EDITOR_SCREEN_BACKGROUND_ARGB).copy(
                        alpha = SAVE_EDITOR_SCREEN_BACKGROUND_ALPHA,
                    ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .graphicsLayer {
                        val backProgress = 1f - config.backProgress
                        translationX = size.width * config.backProgress * BACK_TRANSLATION_FRACTION
                        scaleX = backProgress * BACK_SCALE_FACTOR + BACK_SCALE_OFFSET
                        scaleY = backProgress * BACK_SCALE_FACTOR + BACK_SCALE_OFFSET
                        alpha = backProgress * BACK_ALPHA_OFFSET + BACK_ALPHA_MIN
                    },
        ) {
            SaveEditorHeaderSection(
                selectedTarget = config.selectedTarget,
                selectedSave = config.selectedSave,
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = SaveEditorHeaderPadding),
                color = Color(SAVE_EDITOR_DIVIDER_ARGB),
            )
            content()
        }
        SaveEditorSuccessPopup(
            popup = config.successPopup,
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
    selectedTarget: SaveTargetUiState?,
    selectedSave: BitLifeSaveSummary?,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SaveEditorHeaderPadding, vertical = 20.dp),
    ) {
        selectedTarget?.let { target ->
            SaveAppGlyph(icon = target.icon, name = target.name, size = 52.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
            SaveEditorHeaderText(selectedTarget = selectedTarget, selectedSave = selectedSave)
            selectedTarget?.let { target -> SaveEditorHeaderBadges(target = target, selectedSave = selectedSave) }
        }
    }
}

@Composable
private fun SaveEditorHeaderText(
    selectedTarget: SaveTargetUiState?,
    selectedSave: BitLifeSaveSummary?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = selectedTarget?.name ?: "Save Editor",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text =
                when {
                    selectedSave != null -> "SLOT EDITOR"
                    selectedTarget != null -> "SAVE SLOTS"
                    else -> "SAVE EDITOR"
                },
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SAVE_EDITOR_LABEL_ALPHA),
            fontWeight = FontWeight.Bold,
            letterSpacing = SAVE_EDITOR_HEADER_LETTER_SPACING.sp,
        )
    }
}

@Composable
private fun SaveEditorHeaderBadges(
    target: SaveTargetUiState,
    selectedSave: BitLifeSaveSummary?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        if (target.versionLabel.isNotBlank()) {
            SaveEditorHeaderBadge(text = "v${target.versionLabel}")
        }
        SaveEditorHeaderBadge(text = selectedSave?.slotName ?: target.statusLabel)
    }
}

@Composable
private fun SaveEditorHeaderBadge(text: String) {
    Surface(
        color = Color.White.copy(alpha = SAVE_EDITOR_BADGE_ALPHA),
        shape = SaveEditorPillShape,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
            color = Color.White.copy(alpha = SAVE_EDITOR_BADGE_TEXT_ALPHA),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
