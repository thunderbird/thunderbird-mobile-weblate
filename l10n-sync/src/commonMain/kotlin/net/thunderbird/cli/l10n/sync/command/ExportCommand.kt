package net.thunderbird.cli.l10n.sync.command

import net.thunderbird.cli.l10n.sync.task.ExportOutput
import net.thunderbird.cli.l10n.sync.task.ExportTask
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.terminal.TerminalStatusStyle

class ExportCommand(
    private val command: Command.Export,
    private val task: ExportTask,
    private val terminal: Terminal,
) {
    suspend fun run() {
        terminal.status("📤 Starting localization export")
        terminal.line("Branch: ${command.branch}", indent = 1)
        terminal.line("L10n repo: ${command.l10nRepo}", indent = 1)
        terminal.line("Apply changes: ${command.applyChanges}", indent = 1)

        terminal.status("📄 Loading sync manifest...")
        val input = task.readInput()
        val branchInput = task.validateInput(input)

        terminal.status("🔎 Checking ${branchInput.files.size} source files for export...")
        val mergeResult = task.mergeInput(branchInput)
        mergeResult.missingFiles.forEach {
            terminal.warning("Missing translation mirror file: $it", indent = 1)
        }

        terminal.status(
            if (command.applyChanges) "🔄 Applying export changes..."
            else "🔎 Calculating export changes..."
        )
        printOutput(task.writeOutput(mergeResult, command.applyChanges))
    }

    private fun printOutput(output: ExportOutput) {
        terminal.line("Files considered: ${output.filesConsidered}", indent = 1)
        terminal.line("Files changed: ${output.changedFiles.size}", indent = 1)
        output.changedFiles.forEach { terminal.line("- ${it.relativePath}", indent = 2) }

        terminal.status("Export complete!", style = TerminalStatusStyle.SUCCESS)
        terminal.line("Branch: ${command.branch}", indent = 1)
        terminal.line("Files considered: ${output.filesConsidered}", indent = 1)
        terminal.line("Files written: ${output.filesWritten}", indent = 1)
    }
}
