package net.thunderbird.cli.l10n.sync.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.terminal.TerminalLine
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.terminal.TerminalStateStore

val HelpCommandTests by
    testSuite("HelpCommand") {
        test("prints unknown command help with message") {
            val store = TerminalStateStore(TerminalState("test"))

            HelpCommand(
                    Command.Help(
                        type = Command.Help.HelpType.UNKNOWN,
                        message = "Unknown command: validate",
                    ),
                    store,
                )
                .run()

            assertEquals(
                listOf(
                    TerminalLine.Text("Unknown command: validate"),
                    TerminalLine.Text(""),
                    TerminalLine.Text("Usage: sync <command> [options]"),
                    TerminalLine.Text("Commands:"),
                    TerminalLine.Text("  import    Import localization files from source branches"),
                    TerminalLine.Text(
                        "  export    Export localization files to a source branch checkout"
                    ),
                    TerminalLine.Text("  help      Show this help message"),
                ),
                store.state.value.lines,
            )
        }

        test("prints import command help") {
            val store = TerminalStateStore(TerminalState("test"))

            HelpCommand(Command.Help(Command.Help.HelpType.IMPORT), store).run()

            assertEquals(
                listOf(
                    TerminalLine.Text("Usage: sync import [options]"),
                    TerminalLine.Text("Options:"),
                    TerminalLine.Text(
                        "  --all              Import both source-language files and translations"
                    ),
                    TerminalLine.Text(
                        "  --apply            Write files and clean stale files (default: dry run)"
                    ),
                    TerminalLine.Text("  --help, -h         Show this help message"),
                ),
                store.state.value.lines,
            )
        }

        test("prints export command help") {
            val store = TerminalStateStore(TerminalState("test"))

            HelpCommand(Command.Help(Command.Help.HelpType.EXPORT), store).run()

            assertEquals(
                listOf(
                    TerminalLine.Text("Usage: sync export [options]"),
                    TerminalLine.Text("Options:"),
                    TerminalLine.Text(
                        "  --branch VALUE             Export files for the target source branch"
                    ),
                    TerminalLine.Text(
                        "  --l10n-repo PATH           Read merged translations and inventory from this l10n repository"
                    ),
                    TerminalLine.Text(
                        "  --apply                    Write files to the current repository (default: dry run)"
                    ),
                    TerminalLine.Text("  --help, -h         Show this help message"),
                ),
                store.state.value.lines,
            )
        }
    }
