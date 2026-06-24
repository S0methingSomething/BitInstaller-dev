package dev.bitinstaller.app.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveEditableField

private val SaveEditorButtonShape = SaveEditorControlShape
private const val SAVE_TARGET_HINT_ALPHA = 0.4f
private const val SAVE_TARGET_SECONDARY_ALPHA = 0.45f
private const val SAVE_TARGET_DISABLED_ALPHA = 0.08f
private const val SAVE_TARGET_DISABLED_TEXT_ALPHA = 0.5f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SaveEditorTargetList(
    targets: List<SaveTargetUiState>,
    onTargetClick: (SaveTargetUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(horizontal = SaveEditorHorizontalPadding, vertical = 18.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        if (targets.isEmpty()) {
            item(contentType = "empty") { EmptySaveTargetsCard() }
        } else {
            items(targets, key = { target ->
                target.packageName
            }, contentType = { "target" }) { target ->
                SaveTargetCard(
                    target = target,
                    showSaves = false,
                    actions = SaveTargetCardActions(onTargetClick = onTargetClick),
                    modifier = Modifier.animateItem(placementSpec = BitInstallerAnimations.listPlacementSpec()),
                )
            }
        }
    }
}

@Composable
internal fun SaveTargetDetail(
    target: SaveTargetUiState,
    actions: SaveTargetCardActions,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    transitionState: SaveSlotSharedTransitionState = SaveSlotSharedTransitionState(),
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize().padding(horizontal = SaveEditorHorizontalPadding, vertical = 12.dp),
    ) {
        Text(
            text = "Pick a life ID to edit",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = SAVE_TARGET_HINT_ALPHA),
            fontWeight = FontWeight.Bold,
        )
        when {
            target.isLoading -> {
                SaveLoadingStateView(
                    message = "Analyzing save files...",
                    progress = target.scanProgress,
                    modifier = Modifier.weight(1f),
                )
            }

            target.saves == null -> {
                SaveScanPrompt()
            }

            else -> {
                SaveFileList(
                    target = target,
                    saves = target.saves,
                    onSaveOpen = actions.onSaveOpen,
                    modifier = Modifier.weight(1f),
                    transitionState = transitionState,
                )
            }
        }
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        ) {
            Text(text = "Close")
        }
    }
}

internal data class SaveTargetCardActions(
    val onTargetClick: (SaveTargetUiState) -> Unit,
    val onSaveOpen: (BitLifeSaveSummary) -> Unit = {},
)

@Composable
private fun EmptySaveTargetsCard() {
    SaveEditorPanel(shape = SaveEditorCardShape, containerAlpha = 0.035f, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "No installed BitLife apps found on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
        )
    }
}

@Composable
private fun SaveTargetCard(
    target: SaveTargetUiState,
    showSaves: Boolean,
    actions: SaveTargetCardActions,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
) {
    SaveEditorPanel(
        shape = if (isFocused) SaveEditorPanelShape else SaveEditorCardShape,
        containerAlpha = if (isFocused) 0.055f else 0.04f,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            SaveTargetCardHeader(target = target, onTargetClick = actions.onTargetClick)

            if (showSaves) {
                if (target.saves == null) {
                    SaveScanPrompt()
                } else {
                    SaveFileList(
                        target = target,
                        saves = target.saves,
                        onSaveOpen = actions.onSaveOpen,
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveTargetCardHeader(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SaveAppGlyph(icon = target.icon, name = target.name)
            SaveTargetTextBlock(target = target)
        }
        SaveTargetActionButton(
            target = target,
            onTargetClick = onTargetClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SaveTargetActionButton(
    target: SaveTargetUiState,
    onTargetClick: (SaveTargetUiState) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        enabled = target.actionEnabled,
        onClick = { onTargetClick(target) },
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = SAVE_TARGET_DISABLED_ALPHA),
                disabledContentColor = Color.White.copy(alpha = SAVE_TARGET_DISABLED_TEXT_ALPHA),
            ),
        shape = SaveEditorButtonShape,
        modifier = modifier.heightIn(min = 48.dp),
    ) {
        if (target.isLoading) {
            LoadingIndicator(
                modifier = Modifier.size(18.dp),
                color = Color.Black,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = target.actionLabel, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SaveTargetTextBlock(target: SaveTargetUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = target.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (target.versionLabel.isNotEmpty()) {
            Text(
                text = "v${target.versionLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = SAVE_TARGET_SECONDARY_ALPHA),
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape,
                modifier = Modifier.size(5.dp),
            ) {}
            Text(
                text = target.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SAVE_TARGET_SECONDARY_ALPHA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun SaveAppGlyph(
    icon: TargetIcon,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    if (icon.drawable != null) {
        val painter =
            remember(icon.drawable) {
                BitmapPainter(icon.drawable.toBitmap().asImageBitmap())
            }
        Image(
            painter = painter,
            contentDescription = name,
            modifier =
                modifier
                    .size(size)
                    .clip(RoundedCornerShape(12.dp)),
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                modifier
                    .size(size)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                    ),
        ) {
            Text(
                text = icon.monogram,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
