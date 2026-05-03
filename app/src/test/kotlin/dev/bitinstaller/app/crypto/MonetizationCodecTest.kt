package dev.bitinstaller.app.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path

class MonetizationCodecTest {
    @Test
    fun roundTripsBooleansAndInts() {
        val original =
            linkedMapOf<String, MonetizationValue>(
                "remove_ads" to false,
                "bitizenship" to true,
                "god_mode_discount_percent" to 15,
            )

        val encrypted = MonetizationCodec.encrypt(original)
        val decrypted = MonetizationCodec.decrypt(encrypted)

        assertEquals(original, decrypted)
        assertEquals(encrypted, MonetizationCodec.encrypt(decrypted))
    }

    @Test
    fun preservesStringPayloadsAsBase64AfterDecrypt() {
        val original =
            linkedMapOf<String, MonetizationValue>(
                "marketplace_banner" to "enabled",
            )

        val decrypted = MonetizationCodec.decrypt(MonetizationCodec.encrypt(original))

        assertEquals("ZW5hYmxlZA==", decrypted["marketplace_banner"])
    }

    @Test
    fun patchesSupportedFalseValues() {
        val original =
            linkedMapOf<String, MonetizationValue>(
                "remove_ads" to false,
                "serialized_false" to B64_NET_BOOLEAN_FALSE_STANDARD,
                "price_tier" to 3,
            )

        val patched = MonetizationCodec.applyUnlockAllPatch(original)

        assertEquals(true, patched["remove_ads"])
        assertEquals(B64_NET_BOOLEAN_TRUE_STANDARD, patched["serialized_false"])
        assertEquals(3, patched["price_tier"])
    }

    @Test
    fun sampleRoundTripsWhenPathIsProvided() {
        val samplePath = System.getProperty("monetizationVarsSample") ?: return
        val original = Files.readString(Path.of(samplePath)).trim()

        val decrypted = MonetizationCodec.decrypt(original)
        val reEncrypted = MonetizationCodec.encrypt(decrypted)

        assertFalse(decrypted.isEmpty())
        assertTrue(decrypted.keys.all { key -> key.isNotBlank() })
        assertEquals(original, reEncrypted.trim())
    }
}
