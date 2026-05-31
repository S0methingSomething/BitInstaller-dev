package dev.bitinstaller.app.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

private val TargetCardShape = RoundedCornerShape(24.dp)
private val TargetButtonShape = RoundedCornerShape(16.dp)
private val TargetGlyphShape = RoundedCornerShape(16.dp)
private const val TARGET_CARD_COLOR_ARGB = 0x0EFFFFFF
private const val TARGET_CARD_BORDER_COLOR_ARGB = 0x14FFFFFF
private const val TARGET_GLYPH_COLOR_ARGB = 0x0DFFFFFF
private const val TARGET_GLYPH_BORDER_COLOR_ARGB = 0x1AFFFFFF
private const val TARGET_SUBTLE_BUTTON_COLOR_ARGB = 0x08FFFFFF
private const val TARGET_SUBTLE_BUTTON_BORDER_COLOR_ARGB = 0x14FFFFFF
private const val TARGET_PRESSED_SCALE = 0.97f
private const val TARGET_REST_SCALE = 1f
private const val TARGET_DISABLED_ALPHA = 0.42f
private const val TARGET_SECONDARY_TEXT_ALPHA = 0.5f
private const val TARGET_MONOGRAM_ACCENT_ALPHA = 0.14f

@Composable
internal fun PatchTargetsSection(
    targets: List<PatchTargetUiState>,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier =
            Modifier
                .fillMaxWidth(),
    ) {
        item(contentType = "header") {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Games",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    text = "Pick an installed app to inspect or update its MonetizationVars patch.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = TARGET_SECONDARY_TEXT_ALPHA),
                )
            }
        }
        items(targets, key = { target -> target.packageName }, contentType = { "patch-target" }) { target ->
            PatchTargetCard(
                target = target,
                onPatchClick = onPatchClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PatchTargetCard(
    target: PatchTargetUiState,
    onPatchClick: (PatchTargetUiState) -> Unit,
) {
    val accent = targetAccentColor(target.patchState.supportState)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && target.patchState.actionEnabled) TARGET_PRESSED_SCALE else TARGET_REST_SCALE,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "target_card_scale",
    )
    val textWeight by animateExpressiveFontWeight(
        isActive = isPressed && target.patchState.actionEnabled,
        restWeight = FontWeight.SemiBold.weight,
        activeWeight = FontWeight.Black.weight,
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(TARGET_CARD_COLOR_ARGB)),
        border = BorderStroke(1.dp, Color(TARGET_CARD_BORDER_COLOR_ARGB)),
        shape = TargetCardShape,
        modifier =
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth(),
            ) {
                AppGlyph(icon = target.icon, name = target.name, accent = accent)
                TargetTextBlock(target = target, titleWeight = textWeight)
            }

            if (target.isInstalled) {
                TargetActionButton(
                    target = target,
                    interactionSource = interactionSource,
                    textWeight = textWeight,
                    onClick = { onPatchClick(target) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TargetActionButton(
    target: PatchTargetUiState,
    interactionSource: MutableInteractionSource,
    textWeight: Int,
    onClick: () -> Unit,
) {
    val isSolid = target.patchState.presenceState != PatchPresenceState.PATCHED

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(
                    color =
                        when {
                            !target.patchState.actionEnabled -> Color(TARGET_SUBTLE_BUTTON_COLOR_ARGB)
                            isSolid -> Color.White
                            else -> Color(TARGET_SUBTLE_BUTTON_COLOR_ARGB)
                        },
                    shape = TargetButtonShape,
                ).border(
                    width = 1.dp,
                    color = if (isSolid) Color.Transparent else Color(TARGET_SUBTLE_BUTTON_BORDER_COLOR_ARGB),
                    shape = TargetButtonShape,
                ).clip(TargetButtonShape)
                .clickable(
                    enabled = target.patchState.actionEnabled,
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!target.patchState.actionEnabled && target.patchState.actionLabel == "Opening") {
                LoadingIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White.copy(alpha = TARGET_DISABLED_ALPHA),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = target.patchState.actionLabel,
                color =
                    when {
                        !target.patchState.actionEnabled -> Color.White.copy(alpha = TARGET_DISABLED_ALPHA)
                        isSolid -> Color.Black
                        else -> Color.White
                    },
                fontSize = 14.sp,
                fontWeight = FontWeight(textWeight),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TargetTextBlock(
    target: PatchTargetUiState,
    titleWeight: Int,
) {
    val statusAccent = patchPresenceColor(target.patchState.presenceState)

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = target.name,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight(titleWeight),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (target.versionLabel.isNotEmpty()) {
                Text(
                    text = "v${target.versionLabel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = TARGET_SECONDARY_TEXT_ALPHA),
                )
            }
            PatchStateChip(
                label = target.patchState.presenceLabel,
                patchPresenceState = target.patchState.presenceState,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = statusAccent,
                shape = CircleShape,
                modifier = Modifier.size(5.dp),
            ) {}
            Text(
                text = target.patchState.statusLabel,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = TARGET_SECONDARY_TEXT_ALPHA),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AppGlyph(
    icon: TargetIcon,
    name: String,
    accent: Color,
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
                Modifier
                    .size(56.dp)
                    .clip(TargetGlyphShape),
        )
    } else {
        MonogramGlyph(monogram = icon.monogram, accent = accent)
    }
}

@Composable
private fun MonogramGlyph(
    monogram: String,
    accent: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(56.dp)
                .background(
                    color = Color(TARGET_GLYPH_COLOR_ARGB),
                    shape = TargetGlyphShape,
                ).border(1.dp, Color(TARGET_GLYPH_BORDER_COLOR_ARGB), TargetGlyphShape),
    ) {
        Surface(
            color = accent.copy(alpha = TARGET_MONOGRAM_ACCENT_ALPHA),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(32.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = monogram,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                )
            }
        }
    }
}

@Composable
private fun PatchStateChip(
    label: String,
    patchPresenceState: PatchPresenceState,
) {
    val contentColor = patchPresenceColor(patchPresenceState)
    val containerColor = patchPresenceContainerColor(patchPresenceState)

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun targetAccentColor(supportState: PatchSupportState): Color =
    when (supportState) {
        PatchSupportState.READY -> MaterialTheme.colorScheme.primary
        PatchSupportState.BACKEND_REQUIRED -> MaterialTheme.colorScheme.secondary
        PatchSupportState.UNSUPPORTED -> MaterialTheme.colorScheme.outline
    }

@Composable
private fun patchPresenceColor(patchPresenceState: PatchPresenceState): Color =
    when (patchPresenceState) {
        PatchPresenceState.NOT_PATCHED -> MaterialTheme.colorScheme.onSurfaceVariant
        PatchPresenceState.PATCHED -> MaterialTheme.colorScheme.tertiary
        PatchPresenceState.UNKNOWN -> MaterialTheme.colorScheme.outline
    }

@Composable
private fun patchPresenceContainerColor(patchPresenceState: PatchPresenceState): Color =
    when (patchPresenceState) {
        PatchPresenceState.NOT_PATCHED -> Color.Transparent
        PatchPresenceState.PATCHED -> MaterialTheme.colorScheme.tertiary.copy(alpha = TARGET_MONOGRAM_ACCENT_ALPHA)
        PatchPresenceState.UNKNOWN -> Color.Transparent
    }
