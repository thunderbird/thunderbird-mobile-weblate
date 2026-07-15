package net.thunderbird.cli.l10n.sync.task

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.isInModule
import net.thunderbird.cli.l10n.config.listRegularFilesRecursively
import net.thunderbird.cli.l10n.config.parentDirectoriesUntil
import net.thunderbird.cli.l10n.config.relativeTo
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.sync.support.matchesAnyPathGlob

class StaleFileCleaner(private val config: L10nConfig) {
    fun clean(currentFiles: Set<String>, applyChanges: Boolean): StaleFileCleanupResult {
        val projectRoot = config.projectRoot
        val allExistingFiles =
            projectRoot
                .listRegularFilesRecursively { directory ->
                    directory != projectRoot && directory.name in PROTECTED_DIRECTORIES
                }
                .map { file -> file.relativeTo(projectRoot) }
                .toSet()
        val staleFiles =
            findStaleFiles(allExistingFiles = allExistingFiles, currentFiles = currentFiles)
        val emptyDirectories =
            findEmptyDirectoriesAfterCleanup(
                projectRoot = projectRoot,
                existingFiles = allExistingFiles,
                staleFiles = staleFiles,
            )

        return StaleFileCleanupResult(
            filesDeleted = deleteFiles(projectRoot, staleFiles, applyChanges),
            dirsDeleted = deleteDirectories(projectRoot, emptyDirectories, applyChanges),
            staleFiles = staleFiles.sorted(),
            emptyDirectories = emptyDirectories.sorted(),
        )
    }

    private fun findStaleFiles(
        allExistingFiles: Set<String>,
        currentFiles: Set<String>,
    ): Set<String> {
        val sourcePatterns = config.project.import.sourceFilePatterns
        val nonIgnoredFiles =
            allExistingFiles.filterTo(linkedSetOf()) { path ->
                config.project.ignoredModules.none { path.isInModule(it) }
            }
        val currentSourceFiles =
            currentFiles.filter { it.matchesAnyPathGlob(sourcePatterns) }.toSet()
        val existingSourceFiles =
            nonIgnoredFiles.filterTo(linkedSetOf()) { it.matchesAnyPathGlob(sourcePatterns) }
        val existingTranslationFiles =
            nonIgnoredFiles
                .filter { relativePath ->
                    relativePath.matchesAnyPathGlob(config.project.import.translatedFilePatterns) &&
                        !relativePath.matchesAnyPathGlob(sourcePatterns)
                }
                .toSet()

        val staleSourceFiles =
            existingSourceFiles - currentSourceFiles - protectedStaleCleanupFiles()
        val staleTranslationFiles =
            existingTranslationFiles.filterTo(linkedSetOf()) { translationPath ->
                translationPath.sourcePath()?.let { sourcePath ->
                    sourcePath !in currentSourceFiles
                } == true
            }

        return staleSourceFiles + staleTranslationFiles
    }

    private fun String.sourcePath(): String? =
        when {
            "/res/values-" in this || "/composeResources/values-" in this ->
                replace(Regex("/values-[^/]+/"), "/values/")
            startsWith("app-metadata/") ->
                split('/').let { segments ->
                    segments
                        .takeIf { it.size >= APP_METADATA_FILE_PATH_SEGMENTS }
                        ?.toMutableList()
                        ?.apply { this[2] = "en-US" }
                        ?.joinToString("/")
                }
            else -> null
        }

    private fun deleteFiles(
        projectRoot: Path,
        staleFiles: Set<String>,
        applyChanges: Boolean,
    ): Int {
        var filesDeleted = 0
        for (staleFile in staleFiles) {
            val file = projectRoot.resolve(staleFile)
            if (SystemFileSystem.metadataOrNull(file)?.isRegularFile == true) {
                if (applyChanges) {
                    SystemFileSystem.delete(file)
                }
                filesDeleted++
            }
        }
        return filesDeleted
    }

    private fun deleteDirectories(
        projectRoot: Path,
        emptyDirectories: List<String>,
        applyChanges: Boolean,
    ): Int {
        var dirsDeleted = 0
        for (emptyDirectory in emptyDirectories.sortedByDescending { it.length }) {
            val directory = projectRoot.resolve(emptyDirectory)
            if (SystemFileSystem.metadataOrNull(directory)?.isDirectory == true) {
                try {
                    if (applyChanges) {
                        SystemFileSystem.delete(directory, mustExist = false)
                    }
                    dirsDeleted++
                } catch (_: Exception) {}
            }
        }
        return dirsDeleted
    }

    private fun findEmptyDirectoriesAfterCleanup(
        projectRoot: Path,
        existingFiles: Set<String>,
        staleFiles: Set<String>,
    ): List<String> {
        val remainingFiles = existingFiles - staleFiles
        val candidateDirectories =
            staleFiles
                .flatMap { staleFile ->
                    projectRoot.resolve(staleFile).parentDirectoriesUntil(projectRoot)
                }
                .distinct()
                .filter { directory ->
                    directory != projectRoot && directory.name !in PROTECTED_DIRECTORIES
                }
                .map { directory -> directory.relativeTo(projectRoot) }

        return candidateDirectories
            .filter { candidateDirectory ->
                remainingFiles.none { remainingFile ->
                    remainingFile == candidateDirectory ||
                        remainingFile.startsWith("$candidateDirectory/")
                }
            }
            .sortedByDescending { it.length }
            .filterNot { candidateDirectory ->
                candidateDirectories.any { otherDirectory ->
                    otherDirectory != candidateDirectory &&
                        otherDirectory.startsWith("$candidateDirectory/")
                }
            }
    }

    private fun protectedStaleCleanupFiles(): Set<String> =
        setOf("${config.project.weblate.defaultLinkedComponent}/src/main/res/values/strings.xml")

    private companion object {
        const val APP_METADATA_FILE_PATH_SEGMENTS = 4

        val PROTECTED_DIRECTORIES =
            setOf(
                ".git",
                ".github",
                ".gradle",
                ".idea",
                ".kotlin",
                ".tmp",
                "build",
                "gradle",
                "weblate",
            )
    }
}

data class StaleFileCleanupResult(
    val filesDeleted: Int,
    val dirsDeleted: Int,
    val staleFiles: List<String>,
    val emptyDirectories: List<String>,
)
