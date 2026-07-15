package net.thunderbird.cli.l10n.weblate.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.config.L10nConfigLoader
import net.thunderbird.cli.l10n.terminal.TerminalLine.Text
import net.thunderbird.cli.l10n.terminal.TerminalModel
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.weblate.command.Command
import net.thunderbird.cli.l10n.weblate.command.CommandParser
import net.thunderbird.cli.l10n.weblate.command.CreateCommand
import net.thunderbird.cli.l10n.weblate.command.DeleteCommand
import net.thunderbird.cli.l10n.weblate.command.HelpCommand
import net.thunderbird.cli.l10n.weblate.command.ListCommand
import net.thunderbird.cli.l10n.weblate.command.UpdateCommand
import net.thunderbird.cli.l10n.weblate.command.WeblateCommandOptions

class WeblateTerminalModel(scope: CoroutineScope, args: Array<String>) :
    TerminalModel(TerminalState(title = APP_TITLE)) {
    init {
        scope.launch { readArgs(args) }
    }

    private suspend fun readArgs(args: Array<String>) {
        when (val command = CommandParser.parse(args)) {
            is Command.Help -> {
                runCommand(state = commandState(name = "Help")) {
                    HelpCommand(command, store).run()
                }
            }
            is Command.Update -> {
                val config = L10nConfigLoader.load()
                runCommand(
                    state =
                        commandState(name = "Update", config = config, options = command.options)
                ) {
                    UpdateCommand(config, command.options, store).run()
                }
            }
            is Command.Create -> {
                val config = L10nConfigLoader.load()
                runCommand(
                    state =
                        commandState(name = "Create", config = config, options = command.options)
                ) {
                    CreateCommand(config, command.options, store).run()
                }
            }
            is Command.List -> {
                val config = L10nConfigLoader.load()
                runCommand(
                    state = commandState(name = "List", config = config, options = command.options)
                ) {
                    ListCommand(config, command.options, store).run()
                }
            }
            is Command.Delete -> {
                val config = L10nConfigLoader.load()
                runCommand(
                    state =
                        commandState(name = "Delete", config = config, options = command.options)
                ) {
                    DeleteCommand(config, command.options, store, command.slug).run()
                }
            }
        }
    }

    private fun commandState(
        name: String,
        config: L10nConfig? = null,
        options: WeblateCommandOptions? = null,
    ): TerminalState =
        TerminalState(
            title = APP_TITLE,
            subtitle = "Command: $name",
            lines =
                config?.let {
                    listOf(
                        Text(
                            "Weblate: ${it.project.weblate.baseUrl}projects/${it.project.weblate.projectSlug}/"
                        ),
                        Text("Apply changes: ${options?.applyChanges ?: false}"),
                        Text(""),
                    )
                } ?: emptyList(),
        )

    private companion object {
        const val APP_TITLE = "🌐 Thunderbird Weblate Manager"
    }
}
