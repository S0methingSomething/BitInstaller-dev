package dev.bitinstaller.app.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.bitinstaller.app.save.BitLifeSaveSummary
import dev.bitinstaller.app.save.SaveAttributeSummary
import dev.bitinstaller.app.save.SaveCharacterSummary
import dev.bitinstaller.app.save.SaveEditableField
import java.util.Locale

private const val BYTES_PER_KIB = 1024f
private const val BYTES_PER_MIB = BYTES_PER_KIB * BYTES_PER_KIB
private const val MAX_ATTRIBUTE_PREVIEW_COUNT = 8
private const val MAX_CHARACTER_PREVIEW_COUNT = 4
private const val SAVE_DETAIL_LABEL_ALPHA = 0.4f

@Composable
internal fun SaveFileMetaLine(save: BitLifeSaveSummary) {
    Text(
        text =
            listOfNotNull(
                save.slotName,
                save.age?.let { "Age $it" },
                save.gender,
                formatBytes(save.sizeBytes),
            ).joinToString(" · "),
        style =
            MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.2.sp,
            ),
        color = Color.White.copy(alpha = SAVE_DETAIL_LABEL_ALPHA),
    )
}

@Composable
internal fun SaveFactRows(
    save: BitLifeSaveSummary,
    draft: SaveSlotEditDraft,
    onFieldChange: (SaveEditableField, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        save.bankBalanceField?.let { field ->
            SaveInlineTextField(
                label = "BANK",
                value = draft.valueFor(field),
                onValueChange = { onFieldChange(field, it) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        save.facts.forEach { fact ->
            if (fact.field != null) {
                SaveInlineTextField(
                    label = fact.label,
                    value = draft.valueFor(fact.field),
                    onValueChange = { onFieldChange(fact.field, it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun SaveAttributeRows(
    attributes: List<SaveAttributeSummary>,
    draft: SaveSlotEditDraft,
    onFieldChange: (SaveEditableField, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        attributes.take(MAX_ATTRIBUTE_PREVIEW_COUNT).forEach { attribute ->
            SaveAttributeMeterRow(attribute = attribute, draft = draft, onFieldChange = onFieldChange)
        }
        if (attributes.size > MAX_ATTRIBUTE_PREVIEW_COUNT) {
            Text(
                text = "+${attributes.size - MAX_ATTRIBUTE_PREVIEW_COUNT} more attributes in Advanced Editor",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = SAVE_DETAIL_LABEL_ALPHA),
            )
        }
    }
}

@Composable
internal fun SaveCharacterRows(
    characters: List<SaveCharacterSummary>,
    draft: SaveSlotEditDraft,
    onFieldChange: (SaveEditableField, String) -> Unit,
) {
    if (characters.isEmpty()) return

    val immediateFamily = listOf("FATHER", "MOTHER", "BROTHER", "SISTER")
    val partners = listOf("PARTNER", "HUSBAND", "WIFE", "BOYFRIEND", "GIRLFRIEND")
    val children = listOf("SON", "DAUGHTER", "CHILD")

    val partitioned = linkedMapOf<String, List<SaveCharacterSummary>>()
    val immed = characters.filter { it.role in immediateFamily }
    val prt = characters.filter { it.role in partners }
    val chld = characters.filter { it.role in children }
    val other =
        characters.filter {
            it.role !in immediateFamily && it.role !in partners && it.role !in children
        }

    if (immed.isNotEmpty()) partitioned["IMMEDIATE FAMILY"] = immed
    if (prt.isNotEmpty()) partitioned["PARTNERS & SPOUSES"] = prt
    if (chld.isNotEmpty()) partitioned["CHILDREN"] = chld
    if (other.isNotEmpty()) partitioned["OTHERS"] = other

    var count = 0
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for ((categoryName, categoryPeople) in partitioned) {
            count += categoryPeople.size
            if (count > MAX_CHARACTER_PREVIEW_COUNT) {
                Text(
                    text = "+${characters.size - MAX_CHARACTER_PREVIEW_COUNT} more people in Advanced Editor",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = SAVE_DETAIL_LABEL_ALPHA),
                )
                return
            }
            Text(
                text = categoryName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = RELATIONSHIP_CATEGORY_ALPHA),
                fontWeight = FontWeight.Bold,
                letterSpacing = RELATIONSHIP_CATEGORY_LETTER_SPACING.sp,
            )
            for (character in categoryPeople) {
                RelationshipCard(
                    character = character,
                    draft = draft,
                    onFieldChange = onFieldChange,
                    maxFieldCount = MAX_CHARACTER_FIELD_COUNT,
                )
            }
        }
    }
}

internal const val MAX_CHARACTER_FIELD_COUNT = 5

private fun formatBytes(sizeBytes: Int): String =
    when {
        sizeBytes <= 0 -> "unknown size"
        sizeBytes >= BYTES_PER_MIB -> String.format(Locale.US, "%.1f MB", sizeBytes / BYTES_PER_MIB)
        else -> String.format(Locale.US, "%.0f KB", sizeBytes / BYTES_PER_KIB)
    }
