package dev.bitinstaller.app.home

import dev.bitinstaller.app.crypto.MonetizationCodec
import dev.bitinstaller.app.crypto.MonetizationData
import dev.bitinstaller.app.crypto.MonetizationValue

private const val PREVIEW_DISCOUNT_PERCENT: Int = 15

fun buildPreviewData(): MonetizationData {
    val seed =
        linkedMapOf<String, MonetizationValue>(
            "remove_ads" to false,
            "bitizenship" to false,
            "challenge_vault" to false,
            "god_mode_discount_percent" to PREVIEW_DISCOUNT_PERCENT,
            "marketplace_banner" to "enabled",
        )

    return MonetizationCodec.decrypt(MonetizationCodec.encrypt(seed))
}
