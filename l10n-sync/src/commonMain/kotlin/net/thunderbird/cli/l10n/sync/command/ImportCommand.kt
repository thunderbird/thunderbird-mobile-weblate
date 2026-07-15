package net.thunderbird.cli.l10n.sync.command

import net.thunderbird.cli.l10n.sync.io.input.FileInputReport
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest.Companion.SYNC_MANIFEST_FILE
import net.thunderbird.cli.l10n.sync.task.BranchDifferences
import net.thunderbird.cli.l10n.sync.task.ImportChanges
import net.thunderbird.cli.l10n.sync.task.ImportTask
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.terminal.TerminalStatusStyle

class ImportCommand(
    private val task: ImportTask,
    private val terminal: Terminal,
    private val all: Boolean,
    private val applyChanges: Boolean,
) {
    suspend fun run() {
        terminal.status("🚀 Starting localization import")

        terminal.status("📦 Checking available branches...")
        task.validateInput().forEach { branch ->
            terminal.line("✓ ${branch.value} is available", indent = 1)
        }

        terminal.status("📥 Fetching from branches...")
        val input = task.readInput(all)
        input.fileCountsByBranch.forEach { (branch, fileCount) ->
            terminal.line("✓ ${branch.value}: $fileCount files", indent = 1)
        }
        printBranchDifferences(input.files)

        terminal.status("🔎 Calculating import changes...")
        val changes = task.calculateChanges(input)
        printConflictsOrClean(changes.reports)

        writeOutput(changes)

        terminal.status(
            if (applyChanges) "🧹 Cleaning up stale files..." else "🔎 Checking stale files..."
        )
        val cleanup = task.cleanup(changes, applyChanges)
        terminal.line(
            "${if (applyChanges) "Removed" else "Would remove"} ${cleanup.filesDeleted} stale files",
            indent = 1,
        )
        cleanup.staleFiles.forEach { terminal.line("- $it", indent = 2) }
        terminal.line(
            "${if (applyChanges) "Removed" else "Would remove"} ${cleanup.dirsDeleted} empty directories",
            indent = 1,
        )
        cleanup.emptyDirectories.forEach { terminal.line("- $it", indent = 2) }

        printResult(changes.outputFiles.size, changes.changedFiles.size, cleanup.filesDeleted)
    }

    private fun printConflictsOrClean(reports: List<FileInputReport>) {
        val conflicts = reports.flatMap { report ->
            report.conflicts.map { conflict -> report to conflict }
        }
        val textFileConflicts = reports.mapNotNull { report -> report.textFileConflict }
        if (conflicts.isEmpty() && textFileConflicts.isEmpty()) {
            terminal.line("✓ Clean", indent = 1)
            return
        }
        if (conflicts.isNotEmpty()) {
            terminal.line("Conflicting keys: ${conflicts.size}", indent = 1)
        }
        conflicts.forEach { (report, conflict) ->
            terminal.warning(
                "${report.path}: '${conflict.key}' differs across branches; " +
                    "using ${conflict.selectedBranch.value}",
                indent = 1,
            )
            report.keyResolutions
                .singleOrNull { it.key == conflict.key }
                ?.availableIn
                ?.forEach { source ->
                    terminal.warningDetail("${source.branch.value}: ${source.content}", indent = 1)
                }
        }
        if (textFileConflicts.isNotEmpty()) {
            terminal.line("Conflicting text files: ${textFileConflicts.size}", indent = 1)
        }
        textFileConflicts.forEach { conflict ->
            terminal.warning(
                "${conflict.filePath} differs across branches; using ${conflict.selectedBranch.value}",
                indent = 1,
            )
            conflict.sources.forEach { source ->
                terminal.warningDetail("${source.branch.value}: ${source.content}", indent = 1)
            }
        }
    }

    private fun printBranchDifferences(files: List<net.thunderbird.cli.l10n.sync.model.L10nFile>) {
        val differences = BranchDifferences.calculate(files)
        if (differences.uniqueFiles.isNotEmpty()) {
            terminal.line("Branch-specific files: ${differences.uniqueFiles.size}", indent = 1)
            differences.uniqueFiles.forEach { difference ->
                terminal.line("- ${difference.branch.value}: ${difference.path}", indent = 2)
            }
        }
        if (differences.uniqueKeys.isNotEmpty()) {
            terminal.line("Branch-specific keys: ${differences.uniqueKeys.size}", indent = 1)
            differences.uniqueKeys.forEach { difference ->
                terminal.line(
                    "- ${difference.branch.value}: ${difference.path}#${difference.key}",
                    indent = 2,
                )
            }
        }
    }

    private fun writeOutput(changes: ImportChanges) {
        if (changes.changedFiles.isEmpty() && !changes.manifestChanged) {
            terminal.status("⏭ Skipping output write; no changes")
        } else if (applyChanges) {
            terminal.status("🔄 Writing ${changes.changedFiles.size} changed files and manifest...")
            task.writeOutput(changes)
            changes.changedFiles.forEach { terminal.line("- ${it.relativePath}", indent = 1) }
            if (changes.manifestChanged) terminal.line("Manifest: $SYNC_MANIFEST_FILE", indent = 1)
        } else {
            terminal.status(
                "🔄 Would write ${changes.changedFiles.size} changed files" +
                    if (changes.manifestChanged) " and manifest..." else "..."
            )
            changes.changedFiles.forEach { terminal.line("- ${it.relativePath}", indent = 1) }
            if (changes.manifestChanged) terminal.line("Manifest: $SYNC_MANIFEST_FILE", indent = 1)
        }
    }

    private fun printResult(uniqueFiles: Int, changedFiles: Int, deletedFiles: Int) {
        terminal.status("Import complete!", style = TerminalStatusStyle.SUCCESS)
        terminal.line("Total unique files: $uniqueFiles", indent = 1)
        terminal.line("Files changed: $changedFiles", indent = 1)
        terminal.line("Files cleaned: $deletedFiles", indent = 1)
    }
}
