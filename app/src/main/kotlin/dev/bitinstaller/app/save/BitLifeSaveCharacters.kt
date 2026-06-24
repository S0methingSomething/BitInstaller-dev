package dev.bitinstaller.app.save

import dev.nrbf4j.MemberNode
import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode

internal fun ObjectNode.logicalCharacter(
    logicalName: String,
    role: String,
    lightweight: Boolean,
): SaveCharacterSummary? = logicalObject(logicalName)?.toCharacterSummary(role, lightweight)

internal fun ObjectNode.logicalCharacterList(
    document: NrbfDocument,
    logicalName: String,
    role: String,
    lightweight: Boolean,
): List<SaveCharacterSummary>? =
    summaryList(document, logicalName, role) { node, label -> node.toCharacterSummary(label, lightweight) }

internal fun ObjectNode.logicalPetList(
    document: NrbfDocument,
    logicalName: String,
    role: String,
    lightweight: Boolean,
): List<SaveCharacterSummary>? =
    summaryList(document, logicalName, role) { node, label -> node.toPetSummary(label, lightweight) }

internal fun ObjectNode.logicalAncestorList(
    document: NrbfDocument,
    logicalName: String,
    role: String,
    lightweight: Boolean,
): List<SaveCharacterSummary>? =
    summaryList(document, logicalName, role) { node, label -> node.toAncestorSummary(label, lightweight) }

private fun ObjectNode.summaryList(
    document: NrbfDocument,
    logicalName: String,
    role: String,
    build: (ObjectNode, String) -> SaveCharacterSummary,
): List<SaveCharacterSummary>? =
    logicalMember(logicalName)
        ?.derefObjectRefsOrNull()
        ?.mapIndexedNotNull { index, refId ->
            val node = document.objectNodeOrNull(refId) ?: return@mapIndexedNotNull null
            val firstName = node.logicalObject("Name")?.logicalString("FirstName")
            val fullName = node.logicalString("FullName")
            val name = firstName?.takeIf { it.isNotBlank() } ?: fullName?.takeIf { it.isNotBlank() }
            val label = if (name == null) "$role ${index + 1}" else "$role ($name)"
            build(node, label)
        }

internal fun MemberNode.derefObjectRefsOrNull(): List<Int>? = runCatching { derefArray().objectRefs() }.getOrNull()

private fun ObjectNode.toCharacterSummary(
    role: String,
    lightweight: Boolean,
): SaveCharacterSummary {
    val name = logicalObject("Name")
    val fields =
        if (lightweight) {
            emptyList()
        } else {
            buildList {
                name
                    ?.logicalMember("FirstName")
                    ?.toEditableField("First name", "Characters / $role / First name", role)
                    ?.let(::add)
                name
                    ?.logicalMember("LastName")
                    ?.toEditableField("Last name", "Characters / $role / Last name", role)
                    ?.let(::add)
                addAll(schemaFields(HeroCharacterFields, role))
                addAll(schemaFields(CoreAttributeFields, role, group = "Attributes"))
            }
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

private fun ObjectNode.toPetSummary(
    role: String,
    lightweight: Boolean,
): SaveCharacterSummary {
    val fields =
        if (lightweight) {
            emptyList()
        } else {
            buildList {
                addAll(schemaFields(PetFields, role))
                addAll(schemaFields(PetAttributeFields, role, group = "Attributes"))
            }
        }
    return SaveCharacterSummary(
        role = role,
        name = logicalString("Name").orEmpty(),
        age = logicalInt("Age"),
        relationship = logicalFloat("HeroRelationshipStrength"),
        isAlive = logicalBoolean("Alive"),
        fields = fields,
    )
}

private fun ObjectNode.toAncestorSummary(
    role: String,
    lightweight: Boolean,
): SaveCharacterSummary {
    val fields = if (lightweight) emptyList() else schemaFields(AncestorFields, role)
    return SaveCharacterSummary(
        role = role,
        name = logicalString("FullName").orEmpty(),
        age = null,
        relationship = null,
        isAlive = false,
        fields = fields,
    )
}

private fun ObjectNode.schemaFields(
    schema: List<FieldSchema>,
    role: String,
    group: String = role,
): List<SaveEditableField> =
    schema.mapNotNull { (memberName, label) ->
        logicalMember(memberName)
            ?.toEditableField(label, "Characters / $role / $label", group)
    }
