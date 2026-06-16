package dev.bitinstaller.app.home

import androidx.compose.ui.graphics.Color
import dev.bitinstaller.app.save.SaveEditableField
import dev.bitinstaller.app.save.SaveFieldExplanation
import dev.bitinstaller.app.save.explanation

private const val COLOR_CHARACTER_ARGB = 0xFF4CAF50L
private const val COLOR_FINANCES_ARGB = 0xFF2196F3L
private const val COLOR_CAREER_ARGB = 0xFF9C27B0L
private const val COLOR_ASSETS_ARGB = 0xFFFF9800L
private const val COLOR_RELATIONSHIPS_ARGB = 0xFFE91E63L
private const val COLOR_STATE_ARGB = 0xFF00BCD4L
private const val COLOR_TIMING_ARGB = 0xFF795548L
private const val COLOR_COUNTERS_ARGB = 0xFF607D8BL
private const val COLOR_INTERNAL_ARGB = 0xFF9E9E9EL
private const val COLOR_OTHER_ARGB = 0xFFFFFFFFL

private const val CATEGORY_CONTAINER_ALPHA = 0.10f
private const val CATEGORY_OTHER_ALPHA = 0.08f

internal enum class SaveFieldUiCategory(
    val label: String,
    val colorArgb: Long,
    val containerAlpha: Float,
) {
    CHARACTER("Character", COLOR_CHARACTER_ARGB, CATEGORY_CONTAINER_ALPHA),
    FINANCES("Finances", COLOR_FINANCES_ARGB, CATEGORY_CONTAINER_ALPHA),
    CAREER("Career", COLOR_CAREER_ARGB, CATEGORY_CONTAINER_ALPHA),
    ASSETS("Assets", COLOR_ASSETS_ARGB, CATEGORY_CONTAINER_ALPHA),
    RELATIONSHIPS("Relationships", COLOR_RELATIONSHIPS_ARGB, CATEGORY_CONTAINER_ALPHA),
    STATE("State", COLOR_STATE_ARGB, CATEGORY_CONTAINER_ALPHA),
    TIMING("Timing", COLOR_TIMING_ARGB, CATEGORY_CONTAINER_ALPHA),
    COUNTERS("Counters", COLOR_COUNTERS_ARGB, CATEGORY_CONTAINER_ALPHA),
    INTERNAL("Internal", COLOR_INTERNAL_ARGB, CATEGORY_CONTAINER_ALPHA),
    OTHER("Other", COLOR_OTHER_ARGB, CATEGORY_OTHER_ALPHA),
}

internal fun SaveEditableField.uiCategory(): SaveFieldUiCategory {
    val explanation = explanation()
    return computeUiCategory(explanation?.category)
}

internal fun SaveEditableField.computeUiCategory(category: String?): SaveFieldUiCategory {
    val member = memberName.lowercase()
    val groupLower = group.lowercase()
    val pathLower = path.lowercase()

    return categoryFromExplanation(category, member, groupLower)
        ?: categoryFromPath(pathLower)
        ?: categoryFromMemberName(member, groupLower)
        ?: SaveFieldUiCategory.OTHER
}

private fun categoryFromExplanation(
    category: String?,
    member: String,
    groupLower: String,
): SaveFieldUiCategory? =
    when (category) {
        "Attribute" -> SaveFieldUiCategory.CHARACTER
        "Money" -> SaveFieldUiCategory.FINANCES
        "Asset condition" -> SaveFieldUiCategory.ASSETS
        "State flag", "Boolean" -> SaveFieldUiCategory.STATE
        "Naming flag" -> SaveFieldUiCategory.CHARACTER
        "Cosmetic index" -> SaveFieldUiCategory.CHARACTER
        "Cooldown / timing" -> SaveFieldUiCategory.TIMING
        "Counter" -> SaveFieldUiCategory.COUNTERS
        "Identity / metadata", "Rendering internal", "Enum id" -> SaveFieldUiCategory.INTERNAL
        else -> categoryFromHeuristics(member, groupLower)
    }

private fun categoryFromMemberName(
    member: String,
    groupLower: String,
): SaveFieldUiCategory? =
    when {
        member.startsWith("att_") || member.startsWith("hero") -> SaveFieldUiCategory.CHARACTER
        member == "condition" -> SaveFieldUiCategory.ASSETS
        member == "alive" || member == "vampire" -> SaveFieldUiCategory.STATE
        member.startsWith("ageatlast") || member.startsWith("heroageatlast") -> SaveFieldUiCategory.TIMING
        member.startsWith("num") -> SaveFieldUiCategory.COUNTERS
        else -> categoryFromGroup(groupLower)
    }

private fun categoryFromHeuristics(
    member: String,
    groupLower: String,
): SaveFieldUiCategory? =
    when {
        groupLower == "finances" || member.isFinancial() -> SaveFieldUiCategory.FINANCES
        groupLower == "career" || member.isCareer() -> SaveFieldUiCategory.CAREER
        groupLower.isAsset() || member == "condition" -> SaveFieldUiCategory.ASSETS
        groupLower.contains("relationship") || member.contains("relationship") -> SaveFieldUiCategory.RELATIONSHIPS
        else -> null
    }

private fun categoryFromGroup(groupLower: String): SaveFieldUiCategory? =
    when {
        groupLower == "finances" -> SaveFieldUiCategory.FINANCES
        groupLower == "career" -> SaveFieldUiCategory.CAREER
        groupLower.isAsset() -> SaveFieldUiCategory.ASSETS
        groupLower.contains("relationship") -> SaveFieldUiCategory.RELATIONSHIPS
        else -> null
    }

private fun String.isFinancial(): Boolean =
    contains("bank") || contains("balance") || contains("salary") || contains("price")

private fun String.isCareer(): Boolean =
    contains("career") || contains("occupation") || contains("jobtitle") || contains("job")

private fun String.isAsset(): Boolean = contains("asset") || contains("car") || contains("home") || contains("house")

internal fun SaveFieldUiCategory.color(): Color = Color(colorArgb)
