package net.thunderbird.cli.l10n.sync.task

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.L10nProjectConfig
import net.thunderbird.cli.l10n.config.isExcludedPath
import net.thunderbird.cli.l10n.config.isInModule
import net.thunderbird.cli.l10n.config.listRegularFilesRecursively
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.relativeTo
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.sync.support.matchesAnyPathGlob

class ExportStaleFileCleaner(private val config: L10nProjectConfig) {
    fun clean(targetRoot: Path, currentFiles: Set<String>, applyChanges: Boolean): List<String> {
        val sourcePatterns = config.import.sourceFilePatterns
        val translatedPatterns = config.import.translatedFilePatterns
        val existingTranslationFiles =
            targetRoot
                .listRegularFilesRecursively { directory ->
                    directory != targetRoot && directory.name in PROTECTED_DIRECTORIES
                }
                .map { file -> file.relativeTo(targetRoot) }
                .filter { path ->
                    path.matchesAnyPathGlob(translatedPatterns) &&
                        !path.matchesAnyPathGlob(sourcePatterns) &&
                        !path.isExcludedPath(config.import.excludedPaths) &&
                        config.ignoredModules.none { path.isInModule(it) }
                }
                .toSet()
        val staleFiles =
            (existingTranslationFiles - currentFiles)
                .filterNot { relativePath ->
                    relativePath.endsWith(".xml") &&
                        !NAMED_RESOURCE_ENTRY.containsMatchIn(
                            targetRoot.resolve(relativePath).readText()
                        )
                }
                .sorted()

        if (applyChanges) {
            staleFiles.forEach { relativePath ->
                SystemFileSystem.delete(targetRoot.resolve(relativePath), mustExist = false)
            }
        }
        return staleFiles
    }

    private companion object {
        val NAMED_RESOURCE_ENTRY =
            Regex("""<(?:[A-Za-z_][\w.-]*:)?[A-Za-z_][\w.-]*\s+[^>]*name\s*=""")

        val PROTECTED_DIRECTORIES =
            setOf(".git", ".github", ".gradle", ".idea", ".kotlin", ".tmp", "build", "gradle")
    }
}
