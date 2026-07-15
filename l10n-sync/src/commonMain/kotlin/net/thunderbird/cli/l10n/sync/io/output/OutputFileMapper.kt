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
        append("<resources>\n")
        keys
            .sortedBy { key -> key.id }
            .forEach { key ->
                key.comments.forEach { comment ->
                    append("    <!-- ")
                    append(comment)
                    append(" -->\n")
                }
                append("    ")
                append(key.content.prependIndent("    ").trimStart())
                append("\n")
            }
        append("</resources>\n")
    }
}
