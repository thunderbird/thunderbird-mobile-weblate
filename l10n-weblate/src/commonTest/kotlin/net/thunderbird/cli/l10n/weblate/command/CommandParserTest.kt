package net.thunderbird.cli.l10n.weblate.command

import de.infix.testBalloon.framework.core.testSuite
import io.ktor.client.plugins.logging.LogLevel
import kotlin.test.assertEquals
import kotlin.test.assertIs
import net.thunderbird.cli.l10n.weblate.command.Command.Help.HelpType

val WeblateCommandParserTests by
    testSuite("WeblateCommandParser") {
        test("parses update with global options") {
            val result =
                CommandParser.parse(
                    arrayOf("update", "--token", "secret", "--apply", "--log-level", "headers")
                )

            val command = assertIs<Command.Update>(result)
            assertEquals("secret", command.options.token)
            assertEquals(true, command.options.applyChanges)
            assertEquals(LogLevel.HEADERS, command.options.logLevel)
        }

        test("defaults to dry run") {
            val result = CommandParser.parse(arrayOf("update", "--token", "secret"))

            val command = assertIs<Command.Update>(result)
            assertEquals(false, command.options.applyChanges)
        }

        test("returns update help for missing token") {
            val result = CommandParser.parse(arrayOf("update"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.UPDATE, command.type)
            assertEquals("Missing required option for update: --token", command.message)
        }

        test("returns update help") {
            val result = CommandParser.parse(arrayOf("update", "--help"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.UPDATE, command.type)
        }

        test("returns create help") {
            val result = CommandParser.parse(arrayOf("help", "create"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.CREATE, command.type)
        }

        test("parses list") {
            val result = CommandParser.parse(arrayOf("list", "--token", "secret"))

            val command = assertIs<Command.List>(result)
            assertEquals("secret", command.options.token)
        }

        test("returns list help") {
            val result = CommandParser.parse(arrayOf("help", "list"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.LIST, command.type)
        }

        test("returns delete help") {
            val result = CommandParser.parse(arrayOf("delete", "--help"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.DELETE, command.type)
        }

        test("parses delete slug") {
            val result =
                CommandParser.parse(arrayOf("delete", "--token", "secret", "--slug", "app-common"))

            val command = assertIs<Command.Delete>(result)
            assertEquals("app-common", command.slug)
        }

        test("returns delete help for missing token") {
            val result = CommandParser.parse(arrayOf("delete", "--slug", "app-common"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.DELETE, command.type)
            assertEquals("Missing required option for delete: --token", command.message)
        }

        test("returns help for missing delete slug") {
            val result = CommandParser.parse(arrayOf("delete"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.DELETE, command.type)
            assertEquals("Missing required option for delete: --slug", command.message)
        }

        test("returns help for unknown option") {
            val result = CommandParser.parse(arrayOf("update", "--unknown"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.UPDATE, command.type)
            assertEquals("Unknown option: --unknown", command.message)
        }

        test("returns help for missing update option value") {
            val result = CommandParser.parse(arrayOf("update", "--token"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.UPDATE, command.type)
            assertEquals("Missing value for --token", command.message)
        }

        test("returns help for missing delete option value") {
            val result = CommandParser.parse(arrayOf("delete", "--slug"))

            val command = assertIs<Command.Help>(result)
            assertEquals(HelpType.DELETE, command.type)
            assertEquals("Missing value for --slug", command.message)
        }
    }
