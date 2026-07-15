package net.thunderbird.cli.l10n.weblate.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import net.thunderbird.cli.l10n.terminal.TerminalLine
import net.thunderbird.cli.l10n.terminal.TerminalState
import net.thunderbird.cli.l10n.terminal.TerminalStateStore

val WeblateHelpCommandTests by
    testSuite("WeblateHelpCommand") {
        test("prints unknown command help with message") {
            val store = TerminalStateStore(TerminalState("test"))

            HelpCommand(
                    Command.Help(
                        type = Command.Help.HelpType.UNKNOWN,
                        message = "Unknown command: sync",
                    ),
                    store,
                )
                .run()

            assertEquals(
                listOf(
                    TerminalLine.Text("Unknown command: sync"),
                    TerminalLine.Text(""),
                    TerminalLine.Text("Usage: weblate <command> [options]"),
                    TerminalLine.Text(""),
                    TerminalLine.Text("Commands:"),
                    TerminalLine.Text("  update    Update locally discovered components"),
                    TerminalLine.Text("  create    Create missing components"),
                    TerminalLine.Text(
                        "  list      List Weblate components and local discovery status"
                    ),
                    TerminalLine.Text("  delete    Delete a component from Weblate"),
                    TerminalLine.Text("  help      Show this help message"),
                    TerminalLine.Text(""),
                    TerminalLine.Text("Options:"),
                    TerminalLine.Text("  --token                    Weblate API token"),
                    TerminalLine.Text(
                        "  --apply                    Apply changes to Weblate (default: dry run)"
                    ),
                    TerminalLine.Text("  --log-level                Weblate API client log level"),
                    TerminalLine.Text("  --help, -h                 Show this help message"),
                ),
                store.state.value.lines,
            )
        }

        test("prints delete command help with slug option") {
            val store = TerminalStateStore(TerminalState("test"))

            HelpCommand(Command.Help(Command.Help.HelpType.DELETE), store).run()

            assertEquals(
                listOf(
                    TerminalLine.Text("Usage: weblate delete [options]"),
                    TerminalLine.Text("Options:"),
                    TerminalLine.Text("  --token                    Weblate API token"),
                    TerminalLine.Text(
                        "  --apply                    Apply changes to Weblate (default: dry run)"
                    ),
                    TerminalLine.Text("  --log-level                Weblate API client log level"),
                    TerminalLine.Text("  --help, -h                 Show this help message"),
                    TerminalLine.Text(
                        "  --slug                     Slug of the component to delete"
                    ),
                ),
                store.state.value.lines,
            )
        }
    }
