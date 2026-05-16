package dev.bitinstaller.app.save

import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode

internal fun NrbfDocument.objectByClassOrNull(className: String): ObjectNode? =
    runCatching { objectByClass(className) }.getOrNull()

internal fun NrbfDocument.lifeObjectOrNull(): ObjectNode? =
    objectByClassOrNull("Life")
        ?: runCatching { objectNode(1) }.getOrNull()?.takeIf { node -> node.className == "Life" }
