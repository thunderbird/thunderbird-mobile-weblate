package net.thunderbird.cli.l10n.sync.task

import net.thunderbird.cli.l10n.sync.io.input.FileInputReport
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifestBuilder
import net.thunderbird.cli.l10n.sync.io.output.OutputFile
import net.thunderbird.cli.l10n.sync.io.output.OutputFileMapper
import net.thunderbird.cli.l10n.sync.model.L10nFile
import net.thunderbird.cli.l10n.sync.model.L10nTextFile
import net.thunderbird.cli.l10n.sync.model.ResourceKeyConflict
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile
import net.thunderbird.cli.l10n.sync.model.TextFileConflict
import net.thunderbird.cli.l10n.sync.model.TextFileSource

class ImportFileMerger(
    private val resourceSourceFileMerger: ResourceSourceFileMerger = ResourceSourceFileMerger()
) {
    fun merge(files: List<L10nFile>): ImportMergeResult {
        val mergedFiles = mutableListOf<L10nFile>()
        val reports = mutableListOf<FileInputReport>()
        val conflicts = mutableListOf<ResourceKeyConflict>()

        files
            .groupBy { file -> file.relativePath }
            .entries
            .sortedBy { entry -> entry.key }
            .forEach { (relativePath, filesForPath) ->
                val result = mergeFile(relativePath = relativePath, files = filesForPath)
                mergedFiles += result.files
                reports += result.reports
                conflicts += result.conflicts
            }

        return ImportMergeResult(
            files = mergedFiles,
            outputFiles = OutputFileMapper.mapFiles(mergedFiles),
            reports = reports,
            conflicts = conflicts,
            manifest = SyncManifestBuilder.build(reports),
        )
    }

    private fun mergeFile(relativePath: String, files: List<L10nFile>): FileMergeResult =
        if (!relativePath.endsWith(".xml")) {
            mergeTextFile(relativePath, files)
        } else {
            resourceSourceFileMerger
                .merge(
                    relativePath = relativePath,
                    files = files.filterIsInstance<SourceResourceFile>(),
                    detectConflicts = relativePath.isSourceResourceFile(),
                )
                ?.let { merged ->
                    val report =
                        merged
                            .takeIf { relativePath.isSourceResourceFile() }
                            ?.let { result ->
                                FileInputReport(
                                    path = relativePath,
                                    presentBranches = result.presentBranches,
                                    keyResolutions = result.keyResolutions,
                                    conflicts = result.conflicts,
                                )
                            }
                    FileMergeResult(
                        files = listOf(merged.file),
                        reports = listOfNotNull(report),
                        conflicts = merged.conflicts,
                    )
                } ?: FileMergeResult()
        }

    private fun mergeTextFile(relativePath: String, files: List<L10nFile>): FileMergeResult {
        val textFiles = files.filterIsInstance<L10nTextFile>()
        val selectedFile = files.first()
        val sources = textFiles.map { TextFileSource(branch = it.branch, content = it.content) }
        val report =
            if (sources.map { it.content }.distinct().size > 1) {
                FileInputReport(
                    path = relativePath,
                    presentBranches = files.mapTo(linkedSetOf()) { it.branch },
                    keyResolutions = emptyList(),
                    conflicts = emptyList(),
                    textFileConflict =
                        TextFileConflict(
                            filePath = relativePath,
                            selectedBranch = selectedFile.branch,
                            sources = sources,
                        ),
                )
            } else {
                null
            }

        return FileMergeResult(files = listOf(selectedFile), reports = listOfNotNull(report))
    }

    private fun String.isSourceResourceFile(): Boolean =
        endsWith("/res/values/strings.xml") || endsWith("/composeResources/values/strings.xml")
}

data class ImportMergeResult(
    val files: List<L10nFile>,
    val outputFiles: List<OutputFile>,
    val reports: List<FileInputReport>,
    val conflicts: List<ResourceKeyConflict>,
    val manifest: SyncManifest,
)

private data class FileMergeResult(
    val files: List<L10nFile> = emptyList(),
    val reports: List<FileInputReport> = emptyList(),
    val conflicts: List<ResourceKeyConflict> = emptyList(),
)
