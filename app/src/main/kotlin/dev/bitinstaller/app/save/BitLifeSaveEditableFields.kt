package dev.bitinstaller.app.save

import dev.nrbf4j.BinaryType
import dev.nrbf4j.MemberNode
import dev.nrbf4j.NrbfDocument
import java.io.File

internal fun MemberNode.toEditableField(
    label: String,
    path: String,
    group: String,
): SaveEditableField? {
    val kind = editableValueKind() ?: return null
    return SaveEditableField(
        id = "$objectId:$name",
        objectId = objectId,
        memberName = name,
        label = label.stripSaveNameNoise(),
        path = path.stripSaveNameNoise(),
        group = group.stripSaveNameNoise(),
        value = value.toEditableDisplayValue(),
        valueKind = kind,
    )
}

internal object BitLifeSaveEditor {
    fun applyEdit(
        bytes: ByteArray,
        field: SaveEditableField,
        rawValue: String,
        outputFile: File,
    ): ByteArray =
        NrbfDocument.open(bytes).use { doc ->
            val node = doc.objectNode(field.objectId)
            val member = node.member(field.memberName)
            member.set(field.parseRawValue(rawValue))
            doc.write(outputFile)
            outputFile.readBytes()
        }
}

private fun MemberNode.editableValueKind(): SaveEditableValueKind? =
    when (value) {
        is Boolean -> SaveEditableValueKind.BOOLEAN
        is Byte -> SaveEditableValueKind.BYTE
        is Short -> SaveEditableValueKind.SHORT
        is Int -> SaveEditableValueKind.INT
        is Long -> SaveEditableValueKind.LONG
        is Float -> SaveEditableValueKind.FLOAT
        is Double -> SaveEditableValueKind.DOUBLE
        is String -> SaveEditableValueKind.TEXT
        null -> SaveEditableValueKind.TEXT.takeIf { type == BinaryType.String }
        else -> null
    }
