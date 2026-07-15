package net.thunderbird.cli.l10n.sync.io.output

import kotlinx.io.files.Path
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText

object OutputFileWriter {
    fun writeFiles(projectRoot: Path, files: List<OutputFile>) {
        for (file in files) {
            val outputFile = projectRoot.resolve(file.relativePath)
            outputFile.writeText(file.content)
        }
    }
}
