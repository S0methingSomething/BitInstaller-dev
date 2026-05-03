package dev.bitinstaller.app.home

private val KnownMonetizationLabels =
    mapOf(
        "UserBoughtLegacyBitizenship" to "Legacy Bitizenship",
        "UserBoughtNewBitizenship" to "New Bitizenship",
        "UserGivenBitizenship" to "Gifted Bitizenship",
        "UserBoughtGodMode" to "God Mode",
        "UserGivenGodMode" to "Gifted God Mode",
        "UserBoughtBitizenshipAndGodModeTogether" to "Bitizenship + God Mode Bundle",
        "UserBoughtChallengeVault" to "Challenge Vault",
        "UserGivenChallengeVault" to "Gifted Challenge Vault",
        "UserBoughtBossMode" to "Boss Mode",
        "UserGivenBossMode" to "Gifted Boss Mode",
    )

internal fun monetizationDisplayName(key: String): String =
    KnownMonetizationLabels[key] ?: key
        .removePrefix("UserBought")
        .removePrefix("UserGiven")
        .replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
        .replace("Bitizenship And God Mode Together", "Bitizenship + God Mode Bundle")
        .ifBlank { key }
