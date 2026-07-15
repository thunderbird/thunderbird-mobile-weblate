package net.thunderbird.cli.weblate.command

import de.infix.testBalloon.framework.core.testSuite
import io.ktor.client.plugins.logging.LogLevel
import kotlin.test.assertEquals
import kotlin.test.assertIs

val WeblateCommandParserTests by
    testSuite("WeblateCommandParser") {
        test("parses update with global options") {
            val result =
                CommandParser.parse(
                    arrayOf(
                        "--token",
                        "secret",
                        "--config",
                        "tools.json",
                        "--component-config-file",
                        "component.json",
                        "--dry-run",
                        "--log-level",
                        "headers",
                        "update",
                    )
                )

            assertIs<Command.Update>(result.command)
            assertEquals("secret", result.options.token)
            assertEquals("tools.json", result.options.configFile)
            assertEquals("component.json", result.options.componentConfigFile)
            assertEquals(true, result.options.dryRun)
            assertEquals(LogLevel.HEADERS, result.options.logLevel)
        }

        test("parses delete slug") {
            val result = CommandParser.parse(arrayOf("delete", "--slug", "app-common"))

            val command = assertIs<Command.Delete>(result.command)
            assertEquals("app-common", command.slug)
        }

        test("returns help for missing delete slug") {
            val result = CommandParser.parse(arrayOf("delete"))

            val command = assertIs<Command.Help>(result.command)
            assertEquals("Missing required option for delete: --slug", command.message)
        }

        test("returns help for unknown option") {
            val result = CommandParser.parse(arrayOf("--unknown"))

            val command = assertIs<Command.Help>(result.command)
            assertEquals("Unknown command or option: --unknown", command.message)
        }
    }
