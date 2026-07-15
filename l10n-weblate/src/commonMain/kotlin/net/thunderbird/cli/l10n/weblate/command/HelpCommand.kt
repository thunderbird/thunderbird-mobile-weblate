package net.thunderbird.cli.l10n.weblate.command

import net.thunderbird.cli.l10n.terminal.Terminal

class HelpCommand(private val command: Command.Help, private val store: Terminal) {
    fun run() {
        command.message?.let {
            store.line(it)
            store.line("")
        }

        when (command.type) {
            Command.Help.HelpType.UPDATE -> printCommandHelp("weblate update [options]")

            Command.Help.HelpType.CREATE -> printCommandHelp("weblate create [options]")

            Command.Help.HelpType.LIST -> printCommandHelp("weblate list [options]")

            Command.Help.HelpType.DELETE ->
                printCommandHelp(
                    usage = "weblate delete [options]",
                    extraOptions =
                        listOf("  --slug                     Slug of the component to delete"),
                )

            Command.Help.HelpType.UNKNOWN -> {
                store.line("Usage: weblate <command> [options]")
                store.line("")
                store.line("Commands:")
                store.line("  update    Update locally discovered components")
                store.line("  create    Create missing components")
                store.line("  list      List Weblate components and local discovery status")
                store.line("  delete    Delete a component from Weblate")
                store.line("  help      Show this help message")
                store.line("")
                store.line("Options:")
                printGlobalOptions()
            }
        }
    }

    private fun printCommandHelp(usage: String, extraOptions: List<String> = emptyList()) {
        store.line("Usage: $usage")
        store.line("Options:")
        printGlobalOptions()
        extraOptions.forEach(store::line)
    }

    private fun printGlobalOptions() {
        store.line("  --token                    Weblate API token")
        store.line("  --apply                    Apply changes to Weblate (default: dry run)")
        store.line("  --log-level                Weblate API client log level")
        store.line("  --help, -h                 Show this help message")
    }
}
