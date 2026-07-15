package net.thunderbird.cli.l10n.sync.io.output

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve

object OutputFileChecker {
    fun checkFiles(projectRoot: Path, files: List<OutputFile>): OutputFileCheckResult {
        return OutputFileCheckResult(changedFiles = findChangedFiles(projectRoot, files))
    }

    private fun findChangedFiles(projectRoot: Path, files: List<OutputFile>): List<OutputFile> {
        return files.filter { file ->
            val outputFile = projectRoot.resolve(file.relativePath)
            !SystemFileSystem.exists(outputFile) || outputFile.readText() != file.content
        }
    }
}

data class OutputFileCheckResult(val changedFiles: List<OutputFile>)
