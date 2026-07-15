package net.thunderbird.cli.importer.translation

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TranslationKeyParser {

    fun parseStringsXml(file: File): Map<String, String> {
        if (!file.exists()) return emptyMap()

        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(file)

        val keys = mutableMapOf<String, String>()
        val stringNodes = doc.getElementsByTagName("string")

        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val element = node as org.w3c.dom.Element
            val id = element.getAttribute("name")
            val content = element.textContent

            if (id.isNotBlank()) {
                if (keys.containsKey(id)) {
                    throw IllegalStateException("Duplicate string key '$id' in ${file.path}")
                }
                keys[id] = content
            }
        }

        return keys
    }

    fun writeStringsXml(file: File, keys: Map<String, String>) {
        val content = renderStringsXml(keys)
        file.parentFile?.mkdirs()
        file.writeText(content, Charsets.UTF_8)
    }

    fun renderStringsXml(keys: Map<String, String>): String = buildXmlContent(keys)

    private fun buildXmlContent(keys: Map<String, String>): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<resources>\n")

        for ((id, content) in keys.entries.sortedBy { it.key }) {
            val escaped = escapeXmlValue(content)
            sb.append("    <string name=\"$id\">$escaped</string>\n")
        }

        sb.append("</resources>\n")
        return sb.toString()
    }

    private fun escapeXmlValue(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
