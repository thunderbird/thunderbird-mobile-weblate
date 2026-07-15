package net.thunderbird.cli.l10n.sync.command

import de.infix.testBalloon.framework.core.testSuite
import kotlin.test.assertEquals
import kotlin.test.assertIs

val CommandParserTests by
    testSuite("CommandParser") {
        test("parses import as dry run by default") {
            val result = CommandParser.parse(arrayOf("import"))

            val command = assertIs<Command.Import>(result)
            assertEquals(false, command.fullImport)
            assertEquals(false, command.applyChanges)
        }

        test("parses import apply and all options") {
            val result = CommandParser.parse(arrayOf("import", "--apply", "--all"))

            val command = assertIs<Command.Import>(result)
            assertEquals(true, command.fullImport)
            assertEquals(true, command.applyChanges)
        }

        test("parses export options") {
            val result =
                CommandParser.parse(
                    arrayOf(
                        "export",
                        "--branch",
                        "release",
                        "--l10n-repo",
                        "../tfa-l10n",
                        "--apply",
                    )
                )

            val command = assertIs<Command.Export>(result)
            assertEquals("release", command.branch)
            assertEquals("../tfa-l10n", command.l10nRepo)
            assertEquals(true, command.applyChanges)
        }

        test("requires export branch") {
            val result = CommandParser.parse(arrayOf("export", "--l10n-repo", "../tfa-l10n"))

            val command = assertIs<Command.Help>(result)
            assertEquals(Command.Help.HelpType.EXPORT, command.type)
            assertEquals("Missing required option: --branch", command.message)
        }

        test("returns unknown command help for validate") {
            val result = CommandParser.parse(arrayOf("validate"))

            val command = assertIs<Command.Help>(result)
            assertEquals(Command.Help.HelpType.UNKNOWN, command.type)
            assertEquals("Unknown command: validate", command.message)
        }
    }
