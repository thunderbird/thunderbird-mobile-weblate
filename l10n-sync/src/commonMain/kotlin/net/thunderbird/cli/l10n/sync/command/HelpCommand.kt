package net.thunderbird.cli.l10n.sync.command

import net.thunderbird.cli.l10n.terminal.Terminal

class HelpCommand(private val command: Command.Help, private val store: Terminal) {
    fun run() {
        command.message?.let {
            store.line(it)
            store.line("")
        }

        when (command.type) {
            Command.Help.HelpType.IMPORT ->
                printCommandHelp(
                    usage = "sync import [options]",
                    extraOptions =
                        listOf(
                            "  --all              Import both source-language files and translations",
                            "  --apply            Write files and clean stale files (default: dry run)",
                        ),
                )

            Command.Help.HelpType.EXPORT ->
                printCommandHelp(
                    usage = "sync export [options]",
                    extraOptions =
                        listOf(
                            "  --branch VALUE             Export files for the target source branch",
                            "  --l10n-repo PATH           Read merged translations and inventory " +
                                "from this l10n repository",
                            "  --apply                    Write files to the current repository (default: dry run)",
                        ),
                )

            Command.Help.HelpType.UNKNOWN -> {
                store.line("Usage: sync <command> [options]")
                store.line("Commands:")
                store.line("  import    Import localization files from source branches")
                store.line("  export    Export localization files to a source branch checkout")
                store.line("  help      Show this help message")
            }
        }
    }

    private fun printCommandHelp(usage: String, extraOptions: List<String> = emptyList()) {
        store.line("Usage: $usage")
        store.line("Options:")
        extraOptions.forEach(store::line)
        store.line("  --help, -h         Show this help message")
    }
}
