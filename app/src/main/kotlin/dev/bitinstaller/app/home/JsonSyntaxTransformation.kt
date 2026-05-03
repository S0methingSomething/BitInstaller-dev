package dev.bitinstaller.app.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

private val JsonLiterals = listOf("true", "false", "null")

internal data class JsonSyntaxColors(
    val key: Color,
    val string: Color,
    val number: Color,
    val literal: Color,
    val punctuation: Color,
)

internal class JsonSyntaxTransformation(
    private val colors: JsonSyntaxColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText =
        TransformedText(highlightJson(text.text, colors), OffsetMapping.Identity)
}

private fun highlightJson(
    text: String,
    colors: JsonSyntaxColors,
): AnnotatedString {
    val builder = AnnotatedString.Builder()
    var index = 0
    while (index < text.length) {
        index = appendNextJsonToken(text = text, index = index, builder = builder, colors = colors)
    }
    return builder.toAnnotatedString()
}

private fun appendNextJsonToken(
    text: String,
    index: Int,
    builder: AnnotatedString.Builder,
    colors: JsonSyntaxColors,
): Int =
    when {
        text[index] == '"' -> appendJsonString(text, index, builder, colors)
        text[index].isDigit() || text[index] == '-' -> appendJsonNumber(text, index, builder, colors)
        text.startsWithJsonLiteral(index) -> appendJsonLiteral(text, index, builder, colors)
        text[index] in "{}[]:," -> appendStyled(builder, text[index].toString(), colors.punctuation).let { index + 1 }
        else -> builder.append(text[index]).let { index + 1 }
    }

private fun appendJsonString(
    text: String,
    startIndex: Int,
    builder: AnnotatedString.Builder,
    colors: JsonSyntaxColors,
): Int {
    val endIndex = text.findJsonStringEnd(startIndex)
    val token = text.substring(startIndex, endIndex)
    val color = if (text.isJsonKey(endIndex)) colors.key else colors.string
    appendStyled(builder, token, color)
    return endIndex
}

private fun appendJsonNumber(
    text: String,
    startIndex: Int,
    builder: AnnotatedString.Builder,
    colors: JsonSyntaxColors,
): Int {
    val endIndex = text.indexOfFirstAfter(startIndex) { it !in "-+.eE0123456789" }
    appendStyled(builder, text.substring(startIndex, endIndex), colors.number)
    return endIndex
}

private fun appendJsonLiteral(
    text: String,
    startIndex: Int,
    builder: AnnotatedString.Builder,
    colors: JsonSyntaxColors,
): Int {
    val literal = JsonLiterals.first { text.startsWith(it, startIndex) }
    appendStyled(builder, literal, colors.literal)
    return startIndex + literal.length
}

private fun appendStyled(
    builder: AnnotatedString.Builder,
    token: String,
    color: Color,
) {
    builder.pushStyle(SpanStyle(color = color))
    builder.append(token)
    builder.pop()
}

private fun String.findJsonStringEnd(startIndex: Int): Int {
    var index = startIndex + 1
    var escaped = false
    while (index < length) {
        val current = this[index]
        if (!escaped && current == '"') return index + 1
        escaped = !escaped && current == '\\'
        if (current != '\\') escaped = false
        index += 1
    }
    return length
}

private fun String.isJsonKey(endIndex: Int): Boolean {
    val nextIndex = indexOfFirstAfter(endIndex) { !it.isWhitespace() }
    return nextIndex < length && this[nextIndex] == ':'
}

private fun String.startsWithJsonLiteral(index: Int): Boolean = JsonLiterals.any { startsWith(it, index) }

private fun String.indexOfFirstAfter(
    startIndex: Int,
    predicate: (Char) -> Boolean,
): Int {
    var index = startIndex
    while (index < length && !predicate(this[index])) {
        index += 1
    }
    return index
}
