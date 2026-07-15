package net.thunderbird.cli.l10n.sync.task

import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.sync.io.git.GitClient
import net.thunderbird.cli.l10n.sync.io.input.FileInputReport
import net.thunderbird.cli.l10n.sync.io.input.L10nFileLoadResult
import net.thunderbird.cli.l10n.sync.io.input.L10nFileLoader
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifestStore
import net.thunderbird.cli.l10n.sync.io.output.OutputFile
import net.thunderbird.cli.l10n.sync.io.output.OutputFileChecker
import net.thunderbird.cli.l10n.sync.io.output.OutputFileWriter

interface ImportTask {
    suspend fun validateInput(): List<Branch>

    suspend fun readInput(all: Boolean): L10nFileLoadResult

    fun calculateChanges(input: L10nFileLoadResult): ImportChanges

    fun writeOutput(changes: ImportChanges)

    fun cleanup(changes: ImportChanges, applyChanges: Boolean): StaleFileCleanupResult
}

class DefaultImportTask(
    private val config: L10nConfig,
    private val gitClient: GitClient,
    private val fileLoader: L10nFileLoader = L10nFileLoader(gitClient, config),
    private val merger: ImportFileMerger = ImportFileMerger(),
    private val staleFileCleaner: StaleFileCleaner = StaleFileCleaner(config),
) : ImportTask {
    override suspend fun validateInput(): List<Branch> {
        val branches = config.project.source.branches
        val availableBranches = gitClient.checkAvailableBranches(branches)

        require(availableBranches.isNotEmpty()) {
            "No branches found in source repository. Requested: $branches"
        }
        require(availableBranches.size == branches.size) {
            "Some requested branches are not available in source repository: " +
                (branches - availableBranches.toSet())
        }
        return availableBranches
    }

    override suspend fun readInput(all: Boolean): L10nFileLoadResult =
        fileLoader.load(branches = config.project.source.branches, all = all)

    override fun calculateChanges(input: L10nFileLoadResult): ImportChanges {
        val mergeResult = merger.merge(input.files)
        val projectRoot = config.projectRoot
        val changedFiles =
            OutputFileChecker.checkFiles(projectRoot, mergeResult.outputFiles).changedFiles

        return ImportChanges(
            outputFiles = mergeResult.outputFiles,
            changedFiles = changedFiles,
            reports = mergeResult.reports,
            manifest = mergeResult.manifest,
            manifestChanged = SyncManifestStore.hasChanged(projectRoot, mergeResult.manifest),
        )
    }

    override fun writeOutput(changes: ImportChanges) {
        OutputFileWriter.writeFiles(config.projectRoot, changes.changedFiles)
        if (changes.manifestChanged) {
            SyncManifestStore.write(config.projectRoot, changes.manifest)
        }
    }

    override fun cleanup(changes: ImportChanges, applyChanges: Boolean): StaleFileCleanupResult =
        staleFileCleaner.clean(
            currentFiles = changes.outputFiles.map { it.relativePath }.toSet(),
            applyChanges = applyChanges,
        )
}

data class ImportChanges(
    val outputFiles: List<OutputFile>,
    val changedFiles: List<OutputFile>,
    val reports: List<FileInputReport>,
    val manifest: SyncManifest,
    val manifestChanged: Boolean,
)
