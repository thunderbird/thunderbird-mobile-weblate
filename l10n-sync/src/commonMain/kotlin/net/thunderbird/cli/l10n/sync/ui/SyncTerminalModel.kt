package net.thunderbird.cli.l10n.sync.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.L10nConfigLoader
import net.thunderbird.cli.l10n.sync.command.Command
import net.thunderbird.cli.l10n.sync.command.CommandParser
import net.thunderbird.cli.l10n.sync.command.ExportCommand
import net.thunderbird.cli.l10n.sync.command.HelpCommand
import net.thunderbird.cli.l10n.sync.command.ImportCommand
import net.thunderbird.cli.l10n.sync.io.git.DefaultGitClient
import net.thunderbird.cli.l10n.sync.task.DefaultExportTask
import net.thunderbird.cli.l10n.sync.task.DefaultImportTask
import net.thunderbird.cli.l10n.terminal.TerminalLine.Text
import net.thunderbird.cli.l10n.terminal.TerminalModel
import net.thunderbird.cli.l10n.terminal.TerminalState

class SyncTerminalModel(scope: CoroutineScope, args: Array<String>) :
    TerminalModel(TerminalState(title = APP_TITLE)) {

    init {
        scope.launch { readArgs(args) }
    }

    private suspend fun readArgs(args: Array<String>) {
        when (val command = CommandParser.parse(args)) {
            is Command.Export -> runExport(command)
            is Command.Help -> runHelp(command)
            is Command.Import -> runImport(command)
        }
    }

    private suspend fun runHelp(command: Command.Help) {
        runCommand(state = commandState(name = "Help")) { HelpCommand(command, store).run() }
    }

    private suspend fun runImport(command: Command.Import) {
        val config = L10nConfigLoader.load()
        runCommand(
            state =
                commandState(name = "Import", config = config, applyChanges = command.applyChanges)
        ) {
            val gitClient = DefaultGitClient(config)
            ImportCommand(
                    task = DefaultImportTask(config, gitClient),
                    terminal = store,
                    all = command.fullImport,
                    applyChanges = command.applyChanges,
                )
                .run()
        }
    }

    private suspend fun runExport(command: Command.Export) {
        runCommand(
            state =
                commandState(
                    name = "Export",
                    applyChanges = command.applyChanges,
                    extraLines =
                        listOf(
                            Text("Branch: ${command.branch}"),
                            Text("L10n repo: ${command.l10nRepo}"),
                            Text(""),
                        ),
                )
        ) {
            ExportCommand(
                    command = command,
                    task = DefaultExportTask(command.branch, Path(command.l10nRepo)),
                    terminal = store,
                )
                .run()
        }
    }

    private fun commandState(
        name: String,
        config: L10nConfig? = null,
        applyChanges: Boolean? = null,
        extraLines: List<Text> = emptyList(),
    ): TerminalState =
        TerminalState(
            title = APP_TITLE,
            subtitle = "Command: $name",
            lines =
                config?.let {
                    buildList {
                        add(Text("Source: ${it.project.source.repository.url}"))
                        add(Text("Project root: ${it.projectRoot}"))
                        applyChanges?.let { value -> add(Text("Apply changes: $value")) }
                        add(Text(""))
                    }
                }
                    ?: buildList {
                        applyChanges?.let { value -> add(Text("Apply changes: $value")) }
                        addAll(extraLines)
                    },
        )

    private companion object {
        const val APP_TITLE = "🌍 Thunderbird Localization Sync"
    }
}
