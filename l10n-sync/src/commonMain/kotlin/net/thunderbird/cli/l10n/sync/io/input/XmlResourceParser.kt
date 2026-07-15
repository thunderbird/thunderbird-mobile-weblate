package net.thunderbird.cli.l10n.sync.io.input

import net.thunderbird.cli.l10n.sync.model.L10nKey
import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.TranslationKey
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlWriter
import nl.adaptivity.xmlutil.core.KtXmlReader
import nl.adaptivity.xmlutil.core.KtXmlWriter

@OptIn(ExperimentalXmlUtilApi::class)
object XmlResourceParser : ResourceParser {

    override fun parseSource(content: String, relativePath: String): List<SourceKey> {
        return parse(content, relativePath, ::SourceKey)
    }

    override fun parseTranslation(content: String, relativePath: String): List<TranslationKey> {
        return parse(content, relativePath, ::TranslationKey)
    }

    private fun <T : L10nKey> parse(
        content: String,
        relativePath: String,
        createKey: (id: String, content: String, comments: List<String>) -> T,
    ): List<T> {
        val reader = KtXmlReader(source = content, expandEntities = false, relaxed = true)
        val keys = mutableListOf<T>()
        val pendingComments = mutableListOf<String>()
        val seenKeys = mutableSetOf<String>()

        try {
            while (reader.hasNext()) {
                when (reader.next()) {
                    EventType.COMMENT -> collectResourceComment(reader, pendingComments)
                    EventType.START_ELEMENT ->
                        readResourceEntry(
                                reader = reader,
                                relativePath = relativePath,
                                pendingComments = pendingComments,
                                seenKeys = seenKeys,
                                createKey = createKey,
                            )
                            ?.let(keys::add)

                    else -> Unit
                }
            }
        } finally {
            reader.close()
        }

        return keys
    }

    private fun collectResourceComment(reader: KtXmlReader, pendingComments: MutableList<String>) {
        if (reader.depth <= RESOURCE_DEPTH) {
            pendingComments += reader.text.trim()
        }
    }

    private fun <T : L10nKey> readResourceEntry(
        reader: KtXmlReader,
        relativePath: String,
        pendingComments: MutableList<String>,
        seenKeys: MutableSet<String>,
        createKey: (id: String, content: String, comments: List<String>) -> T,
    ): T? {
        if (reader.depth != RESOURCE_ENTRY_DEPTH) return null

        val key = reader.getAttributeValue("", "name").orEmpty()
        return if (key.isNotBlank()) {
                require(seenKeys.add(key)) { "Duplicate resource key '$key' in $relativePath" }
                createKey(key, serializeElement(reader), pendingComments.toList())
            } else {
                skipElement(reader)
                null
            }
            .also { pendingComments.clear() }
    }

    private fun serializeElement(reader: KtXmlReader): String {
        val startDepth = reader.depth
        val output = StringBuilder()
        val writer = KtXmlWriter(output, false)

        try {
            writeCurrentEvent(reader, writer)
            while (reader.hasNext()) {
                val event = reader.next()
                writeCurrentEvent(reader, writer)
                if (event == EventType.END_ELEMENT && reader.depth == startDepth) {
                    break
                }
            }
        } finally {
            writer.close()
        }

        return output.toString().trim()
    }

    private fun skipElement(reader: KtXmlReader) {
        val startDepth = reader.depth
        while (reader.hasNext()) {
            val event = reader.next()
            if (event == EventType.END_ELEMENT && reader.depth == startDepth) {
                break
            }
        }
    }

    private fun writeCurrentEvent(reader: KtXmlReader, writer: XmlWriter) {
        when (reader.eventType) {
            EventType.START_ELEMENT -> {
                writer.startTag(reader.namespaceURI, reader.localName, reader.prefix)
                reader.namespaceDecls.forEach { namespace ->
                    writer.namespaceAttr(namespace.prefix, namespace.namespaceURI)
                }
                for (index in 0 until reader.attributeCount) {
                    writer.attribute(
                        reader.getAttributeNamespace(index),
                        reader.getAttributeLocalName(index),
                        reader.getAttributePrefix(index),
                        reader.getAttributeValue(index),
                    )
                }
            }

            EventType.END_ELEMENT ->
                writer.endTag(reader.namespaceURI, reader.localName, reader.prefix)

            EventType.TEXT -> writer.text(reader.text)
            EventType.CDSECT -> writer.cdsect(reader.text)
            EventType.COMMENT -> writer.comment(reader.text)
            EventType.ENTITY_REF -> writer.entityRef(reader.text)
            EventType.IGNORABLE_WHITESPACE -> writer.ignorableWhitespace(reader.text)
            EventType.PROCESSING_INSTRUCTION ->
                writer.processingInstruction(reader.piTarget, reader.piData)

            EventType.DOCDECL -> writer.docdecl(reader.text)
            else -> Unit
        }
    }

    private const val RESOURCE_DEPTH = 1
    private const val RESOURCE_ENTRY_DEPTH = 2
}
