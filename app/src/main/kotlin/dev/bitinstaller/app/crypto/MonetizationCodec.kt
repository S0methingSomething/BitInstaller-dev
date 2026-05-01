@file:Suppress("MagicNumber")

package dev.bitinstaller.app.crypto

import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64

const val DEFAULT_CIPHER_KEY: String = "com.wtfapps.apollo16"

const val B64_NET_BOOLEAN_TRUE_STANDARD: String =
    "AAEAAAD/////AQAAAAAAAAAEAQAAAA5TeXN0ZW0uQm9vbGVhbgEAAAAHbV92YWx1ZQABAQs="
const val B64_NET_BOOLEAN_TRUE_VARIANT: String =
    "AAEAAAD/////AQAAAAAAAAAEAQAAAA5TeXN0ZW0uQm9vbGVhbgEAAAAHbV92YWx1ZQABAAs="
const val B64_NET_BOOLEAN_FALSE_STANDARD: String =
    "AAEAAAD/////AQAAAAAAAAAEAQAAAA5TeXN0ZW0uQm9vbGVhbgEAAAAHbV92YWx1ZQABAAw="

typealias MonetizationValue = Any
typealias MonetizationData = Map<String, MonetizationValue>

private val obfuscationCharMap: Map<Char, Char> = mapOf(
    'a' to 'z',
    'b' to 'm',
    'c' to 'y',
    'd' to 'l',
    'e' to 'x',
    'f' to 'k',
    'g' to 'w',
    'h' to 'j',
    'i' to 'v',
    'j' to 'i',
    'k' to 'u',
    'l' to 'h',
    'm' to 't',
    'n' to 'g',
    'o' to 's',
    'p' to 'f',
    'q' to 'r',
    'r' to 'e',
    's' to 'q',
    't' to 'd',
    'u' to 'p',
    'v' to 'c',
    'w' to 'o',
    'x' to 'b',
    'y' to 'n',
    'z' to 'a',
)

private val userSerializedInt32Prefix: ByteArray = byteArrayOf(
    0x00,
    0x01,
    0x00,
    0x00,
    0x00,
    0xff.toByte(),
    0xff.toByte(),
    0xff.toByte(),
    0xff.toByte(),
    0x01,
    0x00,
    0x00,
    0x00,
    0x00,
    0x00,
    0x00,
    0x00,
    0x04,
    0x01,
    0x00,
    0x00,
    0x00,
    0x0c,
    0x53,
    0x79,
    0x73,
    0x74,
    0x65,
    0x6d,
    0x2e,
    0x49,
    0x6e,
    0x74,
    0x33,
    0x32,
    0x01,
    0x00,
    0x00,
    0x00,
    0x07,
    0x6d,
    0x5f,
    0x76,
    0x61,
    0x6c,
    0x75,
    0x65,
    0x00,
    0x08,
)
private val userSerializedInt32Suffix: ByteArray = byteArrayOf(0x0b)
private const val INT32_VALUE_LENGTH: Int = 4
private const val USER_SERIALIZED_INT32_TOTAL_LENGTH: Int =
    49 + INT32_VALUE_LENGTH + 1

object MonetizationCodec {
    fun decrypt(
        content: String,
        cipherKey: String = DEFAULT_CIPHER_KEY,
    ): MonetizationData {
        if (content.isBlank()) {
            return linkedMapOf()
        }

        val obfuscatedKey = getObfuscatedKey(cipherKey)
        val result = linkedMapOf<String, MonetizationValue>()

        content
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .forEach { line ->
                val colonIndex = line.indexOf(':')
                if (colonIndex == -1) {
                    return@forEach
                }

                val encryptedKey = line.substring(0, colonIndex).trim()
                val encryptedValue = line.substring(colonIndex + 1).trim()
                if (encryptedKey.isEmpty() || encryptedValue.isEmpty()) {
                    return@forEach
                }

                val decryptedKey = base64DecodeAndXor(encryptedKey, obfuscatedKey)
                val decryptedValueBase64 = base64DecodeAndXor(encryptedValue, obfuscatedKey)

                val parsedValue =
                    decryptNetBoolean(decryptedValueBase64)
                        ?: decryptNetInt32(decryptedValueBase64)
                        ?: decryptedValueBase64

                result[decryptedKey] = parsedValue
            }

        return result
    }

    fun encrypt(
        data: Map<String, MonetizationValue>,
        cipherKey: String = DEFAULT_CIPHER_KEY,
    ): String {
        if (data.isEmpty()) {
            return ""
        }

        val obfuscatedKey = getObfuscatedKey(cipherKey)

        return data.entries.joinToString(separator = "\n") { (key, value) ->
            val encryptedKey = xorAndBase64Encode(key, obfuscatedKey)
            val serializedValueBase64 = serializeValue(value)
            val encryptedValue = xorAndBase64Encode(serializedValueBase64, obfuscatedKey)
            "$encryptedKey:$encryptedValue"
        }
    }

    fun applyUnlockAllPatch(data: Map<String, MonetizationValue>): MonetizationData {
        val patched = linkedMapOf<String, MonetizationValue>()

        data.forEach { (key, value) ->
            patched[key] =
                when (value) {
                    false -> true
                    B64_NET_BOOLEAN_FALSE_STANDARD -> B64_NET_BOOLEAN_TRUE_STANDARD
                    else -> value
                }
        }

        return patched
    }

    fun toPrettyJson(data: Map<String, MonetizationValue>): String {
        val jsonObject = JSONObject()
        data.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        return jsonObject.toString(2)
    }

    fun parseJsonObject(text: String): MonetizationData {
        val jsonObject = JSONObject(text)
        val parsed = linkedMapOf<String, MonetizationValue>()
        val keys = jsonObject.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            parsed[key] = normalizeJsonValue(key = key, value = value)
        }

        return parsed
    }

    private fun serializeValue(value: MonetizationValue): String =
        when (value) {
            is Boolean -> encryptNetBoolean(value)
            is Int -> encryptNetInt32(value)
            is Long -> {
                require(value in Int.MIN_VALUE..Int.MAX_VALUE) {
                    "Value $value is outside the supported Int32 range"
                }
                encryptNetInt32(value.toInt())
            }
            is String -> {
                if (value == B64_NET_BOOLEAN_TRUE_STANDARD ||
                    value == B64_NET_BOOLEAN_TRUE_VARIANT ||
                    value == B64_NET_BOOLEAN_FALSE_STANDARD
                ) {
                    value
                } else {
                    base64Encode(value)
                }
            }
            else -> error("Unsupported MonetizationVars value type: ${value::class.simpleName}")
        }

    private fun normalizeJsonValue(
        key: String,
        value: Any,
    ): MonetizationValue =
        when (value) {
            is Boolean -> value
            is Int -> value
            is Long -> {
                require(value in Int.MIN_VALUE..Int.MAX_VALUE) {
                    "'$key' is outside the supported Int32 range"
                }
                value.toInt()
            }
            is Double -> {
                require(value % 1.0 == 0.0) {
                    "'$key' must stay an integer"
                }
                require(value >= Int.MIN_VALUE.toDouble() && value <= Int.MAX_VALUE.toDouble()) {
                    "'$key' is outside the supported Int32 range"
                }
                value.toInt()
            }
            is String -> value
            JSONObject.NULL -> error("'$key' cannot be null")
            else -> error("'$key' uses unsupported JSON type ${value::class.simpleName}")
        }
}

private fun getObfuscatedKey(key: String): String =
    buildString(key.length) {
        key.lowercase().forEach { character ->
            append(obfuscationCharMap[character] ?: character)
        }
    }

private fun xorAndBase64Encode(
    text: String,
    key: String,
): String {
    if (text.isEmpty()) {
        return ""
    }

    val textBytes = text.encodeToByteArray()
    val keyBytes = key.encodeToByteArray()
    val xoredBytes = ByteArray(textBytes.size)

    textBytes.indices.forEach { index ->
        xoredBytes[index] = (textBytes[index].toInt() xor keyBytes[index % keyBytes.size].toInt()).toByte()
    }

    return base64Encode(xoredBytes)
}

private fun base64DecodeAndXor(
    encoded: String,
    key: String,
): String {
    if (encoded.isEmpty()) {
        return ""
    }

    val decodedBytes = base64Decode(encoded)
    val keyBytes = key.encodeToByteArray()
    val xoredBytes = ByteArray(decodedBytes.size)

    decodedBytes.indices.forEach { index ->
        xoredBytes[index] = (decodedBytes[index].toInt() xor keyBytes[index % keyBytes.size].toInt()).toByte()
    }

    return xoredBytes.toString(Charsets.UTF_8)
}

private fun decryptNetBoolean(base64String: String): Boolean? =
    when (base64String) {
        B64_NET_BOOLEAN_TRUE_STANDARD,
        B64_NET_BOOLEAN_TRUE_VARIANT,
        -> true

        B64_NET_BOOLEAN_FALSE_STANDARD -> false
        else -> null
    }

private fun encryptNetBoolean(value: Boolean): String =
    if (value) {
        B64_NET_BOOLEAN_TRUE_STANDARD
    } else {
        B64_NET_BOOLEAN_FALSE_STANDARD
    }

private fun decryptNetInt32(base64String: String): Int? {
    if (base64String.isEmpty()) {
        return null
    }

    val bytes = runCatching { base64Decode(base64String) }.getOrNull()
    val isValidEnvelope =
        bytes != null &&
            bytes.size == USER_SERIALIZED_INT32_TOTAL_LENGTH &&
            bytes.copyOfRange(0, userSerializedInt32Prefix.size).contentEquals(userSerializedInt32Prefix) &&
            bytes.last() == userSerializedInt32Suffix.first()

    return if (bytes != null && isValidEnvelope) {
        ByteBuffer
            .wrap(bytes, userSerializedInt32Prefix.size, INT32_VALUE_LENGTH)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
    } else {
        null
    }
}

private fun encryptNetInt32(value: Int): String {
    val valueBytes =
        ByteBuffer
            .allocate(INT32_VALUE_LENGTH)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(value)
            .array()

    val fullByteArray = ByteArray(USER_SERIALIZED_INT32_TOTAL_LENGTH)
    userSerializedInt32Prefix.copyInto(fullByteArray, destinationOffset = 0)
    valueBytes.copyInto(fullByteArray, destinationOffset = userSerializedInt32Prefix.size)
    userSerializedInt32Suffix.copyInto(
        fullByteArray,
        destinationOffset = userSerializedInt32Prefix.size + INT32_VALUE_LENGTH,
    )

    return base64Encode(fullByteArray)
}

private fun base64Encode(text: String): String =
    base64Encode(text.encodeToByteArray())

private fun base64Encode(bytes: ByteArray): String =
    Base64.getEncoder().encodeToString(bytes)

private fun base64Decode(encoded: String): ByteArray =
    Base64.getDecoder().decode(encoded)
