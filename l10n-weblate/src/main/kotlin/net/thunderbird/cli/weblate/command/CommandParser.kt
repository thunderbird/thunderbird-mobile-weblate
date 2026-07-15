package net.thunderbird.cli.weblate.command

import io.ktor.client.plugins.logging.LogLevel
import net.thunderbird.cli.weblate.CliOptions

object CommandParser {
    fun parse(args: Array<String>): ParseResult {
        val options = CliOptions()
        var commandName: String? = null
        val commandArgs = mutableListOf<String>()
        val iterator = args.iterator()

        while (iterator.hasNext()) {
            val argument = iterator.next()
            if (commandName != null) {
                commandArgs += argument
                continue
            }

            when (argument) {
                "update",
                "create",
                "delete",
                "help" -> commandName = argument
                "--help",
                "-h" -> return ParseResult(options = options, command = Command.Help())
                "--token" -> options.token = iterator.nextOptionValue(argument)
                "--component-config-file" ->
                    options.componentConfigFile = iterator.nextOptionValue(argument)
                "--config" -> options.configFile = iterator.nextOptionValue(argument)
                "--dry-run" -> options.dryRun = true
                "--log-level" -> {
                    val value = iterator.nextOptionValue(argument)
                    options.logLevel =
                        LogLevel.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                            ?: return ParseResult(
                                options = options,
                                command = Command.Help(message = "Invalid log level: $value"),
                            )
                }
                else ->
                    return ParseResult(
                        options = options,
                        command = Command.Help(message = "Unknown command or option: $argument"),
                    )
            }
        }

        return ParseResult(options = options, command = parseCommand(commandName, commandArgs))
    }

    private fun parseCommand(commandName: String?, args: List<String>): Command {
        return when (commandName) {
            "update" -> parseNoOptionCommand(commandName, args) ?: Command.Update
            "create" -> parseNoOptionCommand(commandName, args) ?: Command.Create
            "delete" -> parseDeleteCommand(args)
            "help",
            null -> Command.Help()
            else -> Command.Help(message = "Unknown command: $commandName")
        }
    }

    private fun parseNoOptionCommand(commandName: String, args: List<String>): Command.Help? {
        return when {
            args.isEmpty() -> null
            args.any { it == "--help" || it == "-h" } -> Command.Help()
            else -> Command.Help(message = "Unknown option for $commandName: ${args.first()}")
        }
    }

    private fun parseDeleteCommand(args: List<String>): Command {
        var slug: String? = null
        val iterator = args.iterator()

        while (iterator.hasNext()) {
            when (val argument = iterator.next()) {
                "--help",
                "-h" -> return Command.Help()
                "--slug" -> slug = iterator.nextOptionValue(argument)
                else -> return Command.Help(message = "Unknown option for delete: $argument")
            }
        }

        return slug?.let(Command::Delete)
            ?: Command.Help(message = "Missing required option for delete: --slug")
    }

    private fun Iterator<String>.nextOptionValue(option: String): String {
        if (!hasNext()) {
            throw IllegalArgumentException("Missing value for $option")
        }
        return next()
    }
}

data class ParseResult(val options: CliOptions, val command: Command)
