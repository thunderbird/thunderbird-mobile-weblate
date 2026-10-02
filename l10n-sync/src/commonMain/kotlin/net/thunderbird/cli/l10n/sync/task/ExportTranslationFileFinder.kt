package net.thunderbird.cli.l10n.sync.task

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.relativeTo
import net.thunderbird.cli.l10n.config.resolve

class ExportTranslationFileFinder(private val l10nRoot: Path) {
    fun find(sourcePath: String): List<String> =
        if (sourcePath.endsWith(".xml")) {
            resourceTranslationFilesFor(sourcePath)
        } else if (sourcePath.startsWith("app-metadata/") && "/en-US/" in sourcePath) {
            metadataTranslationFilesFor(sourcePath)
        } else {
            emptyList()
        }

    private fun resourceTranslationFilesFor(sourcePath: String): List<String> =
        l10nRoot
            .resolve(sourcePath)
            .parent
            ?.parent
            ?.takeIf { SystemFileSystem.exists(it) }
            ?.let { directory ->
                SystemFileSystem.list(directory)
                    .filter {
                        SystemFileSystem.metadataOrNull(it)?.isDirectory == true &&
                            it.name.startsWith("values-")
                    }
                    .map { it.resolve("strings.xml") }
                    .filter { SystemFileSystem.exists(it) }
                    .map { it.relativeTo(l10nRoot) }
                    .sorted()
            }
            .orEmpty()

    private fun metadataTranslationFilesFor(sourcePath: String): List<String> {
        val sourceFile = l10nRoot.resolve(sourcePath)
        val applicationDirectory = sourceFile.parent?.parent ?: return emptyList()
        return SystemFileSystem.list(applicationDirectory)
            .filter {
                SystemFileSystem.metadataOrNull(it)?.isDirectory == true && it.name != "en-US"
            }
            .map { it.resolve(sourceFile.name) }
            .filter { SystemFileSystem.exists(it) }
            .map { it.relativeTo(l10nRoot) }
            .sorted()
    }
}
