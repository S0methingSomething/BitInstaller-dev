package dev.bitinstaller.app.home

private const val PATH_RELATIONSHIP_KEYWORDS =
    "child|sibling|friend|lover|partner|father|mother|relationship|characters"
private const val PATH_ASSET_KEYWORDS =
    "car|house|home|aircraft|watercraft|jewel|recreational|assets"
private const val PATH_FINANCE_KEYWORDS =
    "finances|portfolio|investment|bond|fund|stock|crypto"
private const val PATH_CAREER_KEYWORDS =
    "occupation|job|freelance|workplace|school"
private const val PATH_CHARACTER_KEYWORDS =
    "hero|attributes|health|happy|smarts|looks"

private val PATH_RELATIONSHIP_REGEX = PATH_RELATIONSHIP_KEYWORDS.toRegex(RegexOption.IGNORE_CASE)
private val PATH_ASSET_REGEX = PATH_ASSET_KEYWORDS.toRegex(RegexOption.IGNORE_CASE)
private val PATH_FINANCE_REGEX = PATH_FINANCE_KEYWORDS.toRegex(RegexOption.IGNORE_CASE)
private val PATH_CAREER_REGEX = PATH_CAREER_KEYWORDS.toRegex(RegexOption.IGNORE_CASE)
private val PATH_CHARACTER_REGEX = PATH_CHARACTER_KEYWORDS.toRegex(RegexOption.IGNORE_CASE)

internal fun categoryFromPath(pathLower: String): SaveFieldUiCategory? =
    when {
        PATH_RELATIONSHIP_REGEX.containsMatchIn(pathLower) -> SaveFieldUiCategory.RELATIONSHIPS
        PATH_ASSET_REGEX.containsMatchIn(pathLower) -> SaveFieldUiCategory.ASSETS
        PATH_FINANCE_REGEX.containsMatchIn(pathLower) -> SaveFieldUiCategory.FINANCES
        PATH_CAREER_REGEX.containsMatchIn(pathLower) -> SaveFieldUiCategory.CAREER
        PATH_CHARACTER_REGEX.containsMatchIn(pathLower) -> SaveFieldUiCategory.CHARACTER
        else -> null
    }
