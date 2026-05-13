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
            document.objectNodeOrNull(refId)?.toCharacterSummary("$role ${index + 1}")
        }

private fun NrbfDocument.objectNodeOrNull(objectId: Int): ObjectNode? = runCatching { objectNode(objectId) }.getOrNull()

private fun MemberNode.derefObjectRefsOrNull(): List<Int>? = runCatching { derefArray().objectRefs() }.getOrNull()

private fun ObjectNode.toCharacterSummary(role: String): SaveCharacterSummary {
    val name = logicalObject("Name")
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
    )
}
