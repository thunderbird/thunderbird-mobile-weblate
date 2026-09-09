package net.thunderbird.cli.l10n.sync.io.output

import net.thunderbird.cli.l10n.sync.model.L10nFile
import net.thunderbird.cli.l10n.sync.model.L10nKey
import net.thunderbird.cli.l10n.sync.model.L10nResourceFile
import net.thunderbird.cli.l10n.sync.model.L10nTextFile

object OutputFileMapper {
    fun mapFiles(files: List<L10nFile>): List<OutputFile> {
        return files.map(::mapFile)
    }

    fun mapFile(file: L10nFile): OutputFile {
        return when (file) {
            is L10nResourceFile -> mapResourceFile(file)
            is L10nTextFile -> mapTextFile(file)
        }
    }

    private fun mapResourceFile(file: L10nResourceFile): OutputFile {
        return OutputFile(relativePath = file.relativePath, content = renderResource(file.keys))
    }

    private fun mapTextFile(file: L10nTextFile): OutputFile {
        return OutputFile(relativePath = file.relativePath, content = file.content)
    }

    private fun renderResource(keys: List<L10nKey>): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        if (keys.any { "<xliff:" in it.content }) {
            append("<resources xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n")
        } else {
            append("<resources>\n")
        }
        keys
            .sortedBy { key -> key.id }
            .forEach { key ->
                key.comments.forEach { comment ->
                    append("    <!-- ")
                    append(comment)
                    append(" -->\n")
                }
                append(indentResourceEntry(key.content))
                append("\n")
            }
        append("</resources>\n")
    }

    private fun indentResourceEntry(content: String): String {
        val lines = content.trim().lines()
        val continuationIndent =
            lines
                .drop(1)
                .filter { it.isNotBlank() }
                .minOfOrNull { line -> line.indexOfFirst { !it.isWhitespace() } }
                ?.coerceAtLeast(0) ?: 0
        return lines
            .mapIndexed { index, line ->
                if (index == 0) line.trimStart() else line.drop(continuationIndent)
            }
            .joinToString("\n")
            .prependIndent("    ")
    }
}
