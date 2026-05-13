package dev.bitinstaller.app.save

import dev.nrbf4j.ObjectNode

internal fun ObjectNode.logicalFloat(logicalName: String): Float? =
    (logicalMember(logicalName)?.value as? Number)?.toFloat()

internal fun ObjectNode.logicalDouble(logicalName: String): Double? =
    (logicalMember(logicalName)?.value as? Number)?.toDouble()
