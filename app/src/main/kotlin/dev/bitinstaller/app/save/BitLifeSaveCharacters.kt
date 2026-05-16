package dev.bitinstaller.app.save

import dev.nrbf4j.MemberNode
import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode

internal fun ObjectNode.logicalCharacter(
    logicalName: String,
    role: String,
): SaveCharacterSummary? = logicalObject(logicalName)?.toCharacterSummary(role)

internal fun ObjectNode.logicalCharacterList(
    document: NrbfDocument,
    logicalName: String,
    role: String,
): List<SaveCharacterSummary>? =
    logicalMember(logicalName)
        ?.derefObjectRefsOrNull()
        ?.mapIndexedNotNull { index, refId ->
            val node = document.objectNodeOrNull(refId) ?: return@mapIndexedNotNull null
            val firstName = node.logicalObject("Name")?.logicalString("FirstName")
            val roleLabel =
                if (firstName.isNullOrBlank()) "$role ${index + 1}" else "$role ($firstName)"
            node.toCharacterSummary(roleLabel)
        }

private fun NrbfDocument.objectNodeOrNull(objectId: Int): ObjectNode? = runCatching { objectNode(objectId) }.getOrNull()

private fun MemberNode.derefObjectRefsOrNull(): List<Int>? = runCatching { derefArray().objectRefs() }.getOrNull()

private fun ObjectNode.toCharacterSummary(role: String): SaveCharacterSummary {
    val name = logicalObject("Name")
    val fields =
        buildList {
            name
                ?.logicalMember("FirstName")
                ?.toEditableField(
                    label = "First name",
                    path = "Characters / $role / First name",
                    group = role,
                )?.let(::add)
            name
                ?.logicalMember("LastName")
                ?.toEditableField(
                    label = "Last name",
                    path = "Characters / $role / Last name",
                    group = role,
                )?.let(::add)
            logicalMember("Age")
                ?.toEditableField(
                    label = "Age",
                    path = "Characters / $role / Age",
                    group = role,
                )?.let(::add)
            logicalMember("HeroRelationshipStrength")
                ?.toEditableField(
                    label = "Relationship",
                    path = "Characters / $role / Relationship",
                    group = role,
                )?.let(::add)
            logicalMember("Alive")
                ?.toEditableField(
                    label = "Alive",
                    path = "Characters / $role / Alive",
                    group = role,
                )?.let(::add)
        }
    return SaveCharacterSummary(
        role = role,
        name =
            displayName(
                firstName = name?.logicalString("FirstName").orEmpty(),
                lastName = name?.logicalString("LastName").orEmpty(),
                royalTitle = name?.logicalString("RoyalTitle").orEmpty(),
                hasDoctorate = name?.logicalBoolean("HasDoctorate") == true,
            ),
        age = logicalInt("Age"),
        relationship = logicalFloat("HeroRelationshipStrength"),
        isAlive = logicalBoolean("Alive"),
        fields = fields,
    )
}
