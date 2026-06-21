package dev.bitinstaller.app.save

import dev.nrbf4j.MemberNode
import dev.nrbf4j.ObjectNode

internal fun ObjectNode?.attributes(): List<SaveAttributeSummary> {
    val node = this ?: return emptyList()
    return CoreAttributeFields.mapNotNull { (field, label) ->
        val member = node.logicalMember(field)
        node.logicalFloat(field)?.let { value ->
            SaveAttributeSummary(
                label = label,
                value = value,
                field =
                    member?.toEditableField(
                        label = label,
                        path = "Life / Hero / $label",
                        group = "Attributes",
                    ),
            )
        }
    }
}

internal fun ObjectNode.logicalObject(logicalName: String): ObjectNode? =
    logicalMember(logicalName)?.derefObjectOrNull()

internal fun ObjectNode.logicalString(logicalName: String): String? = logicalMember(logicalName)?.value as? String

internal fun ObjectNode.logicalBoolean(logicalName: String): Boolean? = logicalMember(logicalName)?.value as? Boolean

internal fun ObjectNode.logicalInt(logicalName: String): Int? = (logicalMember(logicalName)?.value as? Number)?.toInt()

internal fun ObjectNode.logicalMember(logicalName: String): MemberNode? =
    members()
        .filter { member -> member.name.matchesLogicalName(logicalName) }
        .maxByOrNull { member -> member.name.logicalMemberScore(logicalName) }

private fun String.matchesLogicalName(logicalName: String): Boolean =
    equals(logicalName, ignoreCase = true) || contains("<$logicalName>k__BackingField", ignoreCase = true)

private fun String.logicalMemberScore(logicalName: String): Int =
    when {
        contains("<$logicalName>k__BackingField", ignoreCase = true) -> 2
        equals(logicalName, ignoreCase = true) -> 1
        else -> 0
    }

private fun MemberNode.derefObjectOrNull(): ObjectNode? = runCatching { derefObject() }.getOrNull()
