package dev.bitinstaller.app.home

private val RELATIONSHIP_KEYWORDS =
    listOf("child", "sibling", "friend", "lover", "partner", "father", "mother", "relationship", "characters")
private val ASSET_KEYWORDS = listOf("car", "house", "home", "aircraft", "watercraft", "jewel", "recreational", "assets")
private val FINANCE_KEYWORDS = listOf("finances", "portfolio", "investment", "bond", "fund", "stock", "crypto")
private val CAREER_KEYWORDS = listOf("occupation", "job", "freelance", "workplace", "school")
private val CHARACTER_KEYWORDS = listOf("hero", "attributes", "health", "happy", "smarts", "looks")

internal fun categoryFromPath(pathLower: String): SaveFieldUiCategory? =
    when {
        pathLower.containsAny(RELATIONSHIP_KEYWORDS) -> SaveFieldUiCategory.RELATIONSHIPS
        pathLower.containsAny(ASSET_KEYWORDS) -> SaveFieldUiCategory.ASSETS
        pathLower.containsAny(FINANCE_KEYWORDS) -> SaveFieldUiCategory.FINANCES
        pathLower.containsAny(CAREER_KEYWORDS) -> SaveFieldUiCategory.CAREER
        pathLower.containsAny(CHARACTER_KEYWORDS) -> SaveFieldUiCategory.CHARACTER
        else -> null
    }

private fun String.containsAny(keywords: List<String>): Boolean = keywords.any { keyword -> contains(keyword) }
