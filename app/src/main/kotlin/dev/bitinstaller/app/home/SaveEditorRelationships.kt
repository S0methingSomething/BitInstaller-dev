package dev.bitinstaller.app.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.SaveCharacterSummary
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

internal const val RELATIONSHIP_CARD_ALPHA = 0.06f
internal const val RELATIONSHIP_BADGE_ALPHA = 0.06f
internal const val RELATIONSHIP_CATEGORY_ALPHA = 0.25f
internal const val RELATIONSHIP_META_ALPHA = 0.35f
internal const val RELATIONSHIP_CATEGORY_LETTER_SPACING = 1f
internal val RelationshipCardShape = RoundedCornerShape(14.dp)
internal val RelationshipBadgeShape = RoundedCornerShape(6.dp)

@Composable
internal fun RelationshipCard(
    character: SaveCharacterSummary,
    draftValues: SnapshotStateMap<String, String>,
    onFieldChange: (SaveEditableField, String) -> Unit,
    maxFieldCount: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = RELATIONSHIP_CARD_ALPHA), shape = RelationshipCardShape)
                .padding(14.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier =
                    Modifier
                        .background(Color.White.copy(alpha = RELATIONSHIP_BADGE_ALPHA), shape = RelationshipBadgeShape)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
            ) {
                Text(
                    text = character.role,
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        RelationshipMetaLine(character = character)
        for (field in character.fields.take(maxFieldCount)) {
            if (field.label.equals("Alive", ignoreCase = true)) {
                SaveInlineToggleField(
                    label = field.label,
                    checked = draftValues.valueFor(field).equals("true", ignoreCase = true),
                    onCheckedChange = { checked ->
                        onFieldChange(field, if (checked) "True" else "False")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                SaveInlineTextField(
                    label = field.label,
                    value = draftValues.valueFor(field),
                    onValueChange = { onFieldChange(field, it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun RelationshipMetaLine(character: SaveCharacterSummary) {
    Text(
        text = character.relationshipMeta(),
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.2.sp,
            ),
        color = Color.White.copy(alpha = RELATIONSHIP_META_ALPHA),
    )
}

internal fun SaveCharacterSummary.relationshipMeta(): String =
    listOfNotNull(
        age?.let { "Age $it" },
        relationship?.let { String.format(Locale.US, "%.0f%%", it) },
        isAlive?.let { alive -> if (alive) "Alive" else "Deceased" },
    ).joinToString(" · ").ifBlank { role }
