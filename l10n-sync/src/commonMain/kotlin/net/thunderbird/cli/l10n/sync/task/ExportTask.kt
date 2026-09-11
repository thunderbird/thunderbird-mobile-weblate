package net.thunderbird.cli.l10n.sync.task

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.sync.io.input.InputFile
import net.thunderbird.cli.l10n.sync.io.input.InputFileMapper
import net.thunderbird.cli.l10n.sync.io.manifest.BranchInventory
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest.Companion.SYNC_MANIFEST_FILE
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifestStore
import net.thunderbird.cli.l10n.sync.io.output.OutputFile
import net.thunderbird.cli.l10n.sync.io.output.OutputFileChecker
import net.thunderbird.cli.l10n.sync.io.output.OutputFileMapper
import net.thunderbird.cli.l10n.sync.io.output.OutputFileWriter
import net.thunderbird.cli.l10n.sync.model.TranslationResourceFile

interface ExportTask {
    fun readInput(): ExportInput

    suspend fun validateInput(input: ExportInput): BranchInventory

    fun mergeInput(input: BranchInventory): ExportMergeResult

    fun writeOutput(mergeResult: ExportMergeResult, applyChanges: Boolean): ExportOutput
}

class DefaultExportTask(
    private val branch: String,
    private val l10nRoot: Path,
    private val targetRoot: Path = Path("."),
    private val inputFileMapper: InputFileMapper = InputFileMapper(),
    private val targetBranchValidator: TargetBranchValidator = GitTargetBranchValidator(),
    private val staleFileCleaner: ExportStaleFileCleaner? = null,
    private val translationFileFinder: ExportTranslationFileFinder =
        ExportTranslationFileFinder(l10nRoot),
) : ExportTask {
    override fun readInput(): ExportInput {
        require(SystemFileSystem.exists(l10nRoot.resolve(SYNC_MANIFEST_FILE))) {
            "Manifest not found: ${l10nRoot.resolve(SYNC_MANIFEST_FILE)}"
        }
        return ExportInput(SyncManifestStore.read(l10nRoot))
    }

    override suspend fun validateInput(input: ExportInput): BranchInventory {
        targetBranchValidator.validate(targetRoot, branch)
        return input.manifest.branches[branch]
            ?: error(
                "Branch '$branch' not found in $SYNC_MANIFEST_FILE. " +
                    "Available branches: ${input.manifest.branches.keys.joinToString(", ")}"
            )
    }

    override fun mergeInput(input: BranchInventory): ExportMergeResult {
        input.files.keys.forEach(::requireSafeRelativePath)
        val missingFiles = mutableListOf<String>()
        val files =
            input.files.entries
                .sortedBy { it.key }
                .flatMap { (sourcePath, keys) ->
                    buildExportFiles(sourcePath, keys.toSet(), missingFiles)
                }
                .distinctBy { it.relativePath }
                .sortedBy { it.relativePath }
        return ExportMergeResult(files, missingFiles.sorted())
    }

    override fun writeOutput(mergeResult: ExportMergeResult, applyChanges: Boolean): ExportOutput {
        val changedFiles = OutputFileChecker.checkFiles(targetRoot, mergeResult.files).changedFiles
        if (applyChanges) OutputFileWriter.writeFiles(targetRoot, changedFiles)
        val staleFiles =
            staleFileCleaner
                ?.clean(
                    targetRoot = targetRoot,
                    currentFiles = mergeResult.files.mapTo(linkedSetOf()) { it.relativePath },
                    applyChanges = applyChanges,
                )
                .orEmpty()
        return ExportOutput(
            filesConsidered = mergeResult.files.size,
            changedFiles = changedFiles,
            filesWritten = if (applyChanges) changedFiles.size else 0,
            staleFiles = staleFiles,
            filesDeleted = if (applyChanges) staleFiles.size else 0,
        )
    }

    private fun buildExportFiles(
        sourcePath: String,
        keys: Set<String>,
        missingFiles: MutableList<String>,
    ): List<OutputFile> {
        if (!SystemFileSystem.exists(l10nRoot.resolve(sourcePath))) {
            missingFiles += sourcePath
            return emptyList()
        }
        return translationFileFinder.find(sourcePath).mapNotNull {
            exportFile(it, keys, missingFiles)
        }
    }

    private fun exportFile(
        relativePath: String,
        keys: Set<String>,
        missingFiles: MutableList<String>,
    ): OutputFile? =
        if (relativePath.endsWith(".xml")) {
            exportXmlFile(relativePath, keys, missingFiles)
        } else {
            exportTextFile(relativePath, missingFiles)
        }

    private fun exportXmlFile(
        relativePath: String,
        keys: Set<String>,
        missingFiles: MutableList<String>,
    ): OutputFile? {
        val file = l10nRoot.resolve(relativePath)
        if (!SystemFileSystem.exists(file)) {
            missingFiles += relativePath
            return null
        }
        val resourceFile =
            inputFileMapper.mapFile(
                InputFile(relativePath, Branch(branch), file.readText()),
                Branch(branch),
                source = false,
            ) as TranslationResourceFile
        return resourceFile.keys
            .filter { it.id in keys }
            .takeIf { it.isNotEmpty() || resourceFile.keys.isEmpty() }
            ?.let { OutputFileMapper.mapFile(resourceFile.copy(keys = it)) }
    }

    private fun exportTextFile(
        relativePath: String,
        missingFiles: MutableList<String>,
    ): OutputFile? {
        val file = l10nRoot.resolve(relativePath)
        if (!SystemFileSystem.exists(file)) {
            missingFiles += relativePath
            return null
        }
        return OutputFileMapper.mapFile(
            inputFileMapper.mapFile(
                InputFile(relativePath, Branch(branch), file.readText()),
                Branch(branch),
                source = false,
            )
        )
    }

    private fun requireSafeRelativePath(relativePath: String) {
        val normalizedPath = relativePath.replace('\\', '/')
        require(
            normalizedPath.isNotBlank() &&
                !normalizedPath.startsWith('/') &&
                !WINDOWS_ABSOLUTE_PATH.matches(normalizedPath) &&
                normalizedPath.split('/').none { it == "." || it == ".." }
        ) {
            "Invalid manifest path: $relativePath"
        }
    }

    private companion object {
        val WINDOWS_ABSOLUTE_PATH = Regex("^[A-Za-z]:/.*")
    }
}

data class ExportInput(val manifest: SyncManifest)

data class ExportMergeResult(val files: List<OutputFile>, val missingFiles: List<String>)

data class ExportOutput(
    val filesConsidered: Int,
    val changedFiles: List<OutputFile>,
    val filesWritten: Int,
    val staleFiles: List<String>,
    val filesDeleted: Int,
)
