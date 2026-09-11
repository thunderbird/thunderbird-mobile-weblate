package net.thunderbird.cli.l10n

import java.io.IOException
import java.io.StringReader
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException

internal class ResourceParser {
    fun validateCompose(content: String?, path: String, ref: String): List<String> =
        parse(content, path, ref).flatMap { (key, entry) ->
            buildList {
                if (entry.hasXliffMarkup) {
                    add(
                        "$path: $key uses unsupported xliff markup at $ref. " +
                            "Compose Multiplatform resources must use plain indexed placeholders."
                    )
                }
                entry.invalidComposePlaceholders.sorted().forEach { placeholder ->
                    add(
                        "$path: $key uses invalid placeholder $placeholder at $ref. " +
                            "Compose Multiplatform placeholders must use %<number>\$s or %<number>\$d."
                    )
                }
            }
        }

    fun parse(content: String?, path: String, ref: String): Map<String, ResourceEntry> {
        if (content == null) return emptyMap()

        val root =
            try {
                DocumentBuilderFactory.newInstance()
                    .apply {
                        isCoalescing = true
                        isIgnoringComments = true
                        isNamespaceAware = true
                        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                        setFeature(DISALLOW_DOCTYPE_FEATURE, true)
                    }
                    .newDocumentBuilder()
                    .parse(InputSource(StringReader(content)))
                    .documentElement
            } catch (exception: ParserConfigurationException) {
                invalidResourceFile("$path could not be parsed at $ref", exception)
            } catch (exception: SAXException) {
                invalidResourceFile("$path is not valid XML at $ref", exception)
            } catch (exception: IOException) {
                invalidResourceFile("$path could not be read at $ref", exception)
            }

        return buildMap {
            val children = root.childNodes
            for (index in 0 until children.length) {
                val child = children.item(index)
                if (child.nodeType == Node.ELEMENT_NODE) {
                    val element = child as Element
                    if (element.hasAttribute("name")) {
                        val key = "${element.tagName}:${element.getAttribute("name")}"
                        val text = element.textContent
                        put(
                            key,
                            ResourceEntry(
                                serialized = serializeElement(element),
                                placeholders =
                                    PLACEHOLDER_PATTERN.findAll(text).map { it.value }.toSet(),
                                pluralQuantities = extractPluralQuantities(element),
                                hasXliffMarkup = containsXliffMarkup(element),
                                invalidComposePlaceholders =
                                    extractInvalidComposePlaceholders(text),
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun serializeElement(element: Element): String = buildString {
        append('<')
        append(element.tagName)
        val attributes = buildList {
            for (index in 0 until element.attributes.length) {
                add(element.attributes.item(index))
            }
        }
        for (attribute in attributes.sortedBy { it.nodeName }) {
            append(' ')
            append(attribute.nodeName)
            append("=\"")
            append(attribute.nodeValue)
            append('"')
        }
        append('>')
        val children = element.childNodes
        for (index in 0 until children.length) {
            val child = children.item(index)
            when (child.nodeType) {
                Node.ELEMENT_NODE -> append(serializeElement(child as Element))

                Node.TEXT_NODE,
                Node.CDATA_SECTION_NODE -> append(child.nodeValue)
            }
        }
        append("</")
        append(element.tagName)
        append('>')
    }

    private fun containsXliffMarkup(element: Element): Boolean {
        val children = element.childNodes
        return element.namespaceURI == XLIFF_NAMESPACE ||
            element.tagName.startsWith("xliff:") ||
            (0 until children.length).any { index ->
                val child = children.item(index)
                child.nodeType == Node.ELEMENT_NODE && containsXliffMarkup(child as Element)
            }
    }

    private fun extractInvalidComposePlaceholders(text: String): Set<String> =
        COMPOSE_PLACEHOLDER_CANDIDATE_PATTERN.findAll(text)
            .map { it.value }
            .filterNot(COMPOSE_PLACEHOLDER_PATTERN::matches)
            .toSet()

    private fun extractPluralQuantities(element: Element): Set<String> {
        if (element.tagName != "plurals") return emptySet()

        return buildSet {
            val children = element.childNodes
            for (index in 0 until children.length) {
                val child = children.item(index)
                if (child.nodeType == Node.ELEMENT_NODE) {
                    val item = child as Element
                    if (item.tagName == "item" && item.hasAttribute("quantity")) {
                        add(item.getAttribute("quantity"))
                    }
                }
            }
        }
    }

    private fun invalidResourceFile(message: String, cause: Exception): Nothing =
        throw InvalidResourceFile("$message: ${cause.message}", cause)

    private companion object {
        const val DISALLOW_DOCTYPE_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl"
        const val XLIFF_NAMESPACE = "urn:oasis:names:tc:xliff:document:1.2"
        val PLACEHOLDER_PATTERN = Regex("""%(?:\d+\$)?[-#+ 0,(<]*\d*(?:\.\d+)?[a-zA-Z]""")
        val COMPOSE_PLACEHOLDER_PATTERN = Regex("""%[1-9]\d*\${'$'}[ds]""")
        val COMPOSE_PLACEHOLDER_CANDIDATE_PATTERN =
            Regex("""%(?:[A-Za-z]|[0-9${'$'}#+\-.,(<]+[A-Za-z]?)""")
    }
}

internal data class ResourceEntry(
    val serialized: String,
    val placeholders: Set<String>,
    val pluralQuantities: Set<String>,
    val hasXliffMarkup: Boolean,
    val invalidComposePlaceholders: Set<String>,
)

internal class InvalidResourceFile(message: String, cause: Throwable) :
    IllegalArgumentException(message, cause)
