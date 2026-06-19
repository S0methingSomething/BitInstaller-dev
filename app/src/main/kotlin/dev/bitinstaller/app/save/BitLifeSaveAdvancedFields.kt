package dev.bitinstaller.app.save

import dev.nrbf4j.BinaryType
import dev.nrbf4j.MemberNode
import dev.nrbf4j.NrbfDocument
import dev.nrbf4j.ObjectNode

private const val MAX_ADVANCED_TRAVERSAL_DEPTH = 24

/**
 * Class names whose instances are pure noise in the Advanced field list:
 * price-history logs, destiny/flashback logs, internal bookkeeping.
 * Skipping them removes ~17k of ~18k advanced fields.
 */
private val ADVANCED_SKIP_CLASS_NAMES: Set<String> =
    setOf(
        "SimStockEvent",
        "SimCryptoEvent",
        "SimSectorEvent",
        "SimDestinyEvent",
        "ScenarioTimestamp",
        "SimMarketEvent",
    )

private val ADVANCED_SKIP_CLASS_PREFIXES: List<String> =
    listOf(
        // System.Collections.Generic.List`1[[System.Double, mscorlib, ...]]
        "System.Collections.Generic.List`1[[System.Double",
    )

private fun ObjectNode.shouldSkipAdvanced(): Boolean =
    className in ADVANCED_SKIP_CLASS_NAMES || ADVANCED_SKIP_CLASS_PREFIXES.any { className.startsWith(it) }

internal fun NrbfDocument.collectAdvancedFields(life: ObjectNode?): List<SaveEditableField> {
    val root = life ?: return emptyList()
    val visited = mutableSetOf<Int>()
    val fields = mutableListOf<SaveEditableField>()

    fun visit(
        node: ObjectNode,
        path: List<String>,
        depth: Int,
    ) {
        if (depth > MAX_ADVANCED_TRAVERSAL_DEPTH || !visited.add(node.objectId)) return
        if (node.shouldSkipAdvanced()) return
        node.members().forEach { member ->
            collectAdvancedMember(member = member, path = path, depth = depth, visit = ::visit, fields = fields)
        }
    }

    visit(root, listOf("Life"), depth = 0)
    return fields.distinctBy { field -> field.id }.sortedBy { field -> field.path }
}

private fun NrbfDocument.collectAdvancedMember(
    member: MemberNode,
    path: List<String>,
    depth: Int,
    visit: (ObjectNode, List<String>, Int) -> Unit,
    fields: MutableList<SaveEditableField>,
) {
    val label = member.name.cleanSaveMemberName()
    val memberPath = path + label
    member
        .toEditableField(label = label, path = memberPath.joinToString(" / "), group = path.lastOrNull().orEmpty())
        ?.let(fields::add)
    when (member.type) {
        BinaryType.Class,
        BinaryType.Object,
        BinaryType.SystemClass,
        -> member.derefObjectOrNull()?.let { child -> visit(child, memberPath, depth + 1) }

        BinaryType.ObjectArray,
        BinaryType.StringArray,
        BinaryType.PrimitiveArray,
        -> visitArrayRefs(member = member, label = label, memberPath = memberPath, depth = depth, visit = visit)

        BinaryType.Primitive,
        BinaryType.String,
        -> Unit
    }
}

private fun NrbfDocument.visitArrayRefs(
    member: MemberNode,
    label: String,
    memberPath: List<String>,
    depth: Int,
    visit: (ObjectNode, List<String>, Int) -> Unit,
) {
    member.derefObjectRefsOrNull()?.forEachIndexed { index, objectId ->
        objectNodeOrNull(objectId)?.let { child ->
            visit(child, memberPath + "${label.singularize()} ${index + 1}", depth + 1)
        }
    }
}

private fun MemberNode.derefObjectOrNull(): ObjectNode? = runCatching { derefObject() }.getOrNull()

internal fun NrbfDocument.objectNodeOrNull(objectId: Int): ObjectNode? =
    runCatching {
        objectNode(objectId)
    }.getOrNull()

private fun String.singularize(): String =
    if (endsWith("ss") || endsWith("us") || endsWith("is")) this else removeSuffix("s")
