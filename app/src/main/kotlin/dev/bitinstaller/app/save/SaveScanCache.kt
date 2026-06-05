package dev.bitinstaller.app.save

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

private const val CACHE_VERSION = 1
private const val DEFAULT_CACHE_DIR = "save-scans"

@Serializable
private data class SaveScanCacheEnvelope(
    val version: Int = CACHE_VERSION,
    val saves: List<BitLifeSaveSummary>,
)

internal class SaveScanCache(
    private val cacheDir: File,
) {
    constructor(context: Context) : this(cacheDir = File(context.cacheDir, DEFAULT_CACHE_DIR))

    suspend fun read(packageName: String): List<BitLifeSaveSummary>? =
        withContext(Dispatchers.IO) {
            val file = cacheFile(packageName)
            if (!file.isFile) return@withContext null
            runCatching {
                val envelope = json.decodeFromString<SaveScanCacheEnvelope>(file.readText())
                if (envelope.version == CACHE_VERSION) envelope.saves else null
            }.getOrElse { error ->
                file.delete()
                null
            }
        }

    suspend fun warm(packageNames: Collection<String>): Map<String, List<BitLifeSaveSummary>> =
        withContext(Dispatchers.IO) {
            packageNames
                .mapNotNull { packageName -> read(packageName)?.let { saves -> packageName to saves } }
                .toMap()
        }

    suspend fun write(
        packageName: String,
        saves: List<BitLifeSaveSummary>,
    ) {
        withContext(Dispatchers.IO) {
            val file = cacheFile(packageName)
            val tmp = File(file.parent, "${file.name}.tmp")
            runCatching {
                file.parentFile?.mkdirs()
                tmp.writeText(json.encodeToString(SaveScanCacheEnvelope(saves = saves)))
                tmp.renameTo(file)
            }.onFailure {
                tmp.delete()
            }
        }
    }

    private fun cacheFile(packageName: String): File = File(cacheDir, "scan-${packageName.replace('.', '-')}.json")

    private companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
    }
}
