package net.thunderbird.cli.l10n.weblate.command

import io.ktor.client.plugins.logging.LogLevel
import net.thunderbird.cli.l10n.weblate.command.Command.Help.HelpType

object CommandParser {
    fun parse(args: Array<String>): Command {
        return when (args.firstOrNull()) {
            "update" -> parseUpdateArgs(args.drop(1))
            "create" -> parseCreateArgs(args.drop(1))
            "list" -> parseListArgs(args.drop(1))
            "delete" -> parseDeleteArgs(args.drop(1))
            "help" -> parseHelpArgs(args.drop(1))
            "--help",
            "-h" -> Command.Help(type = HelpType.UNKNOWN)

            else ->
                Command.Help(
                    type = HelpType.UNKNOWN,
                    message = if (args.isNotEmpty()) "Unknown command: ${args.first()}" else null,
                )
        }
    }

    private fun parseUpdateArgs(args: List<String>): Command {
        return parseTokenCommand(
            args = args,
            commandName = "update",
            helpType = HelpType.UPDATE,
            createCommand = Command::Update,
        )
    }

    private fun parseCreateArgs(args: List<String>): Command {
        return parseTokenCommand(
            args = args,
            commandName = "create",
            helpType = HelpType.CREATE,
            createCommand = Command::Create,
        )
    }

    private fun parseListArgs(args: List<String>): Command {
        return parseTokenCommand(
            args = args,
            commandName = "list",
            helpType = HelpType.LIST,
            createCommand = Command::List,
        )
    }

    private fun parseTokenCommand(
        args: List<String>,
        commandName: String,
        helpType: HelpType,
        createCommand: (WeblateCommandOptions) -> Command,
    ): Command {
        val options = WeblateCommandOptions()

        parseOptions(args, options, helpType)?.let {
            return it
        }

        return requireToken(commandName = commandName, options = options, helpType = helpType)
            ?: createCommand(options)
    }

    private fun parseDeleteArgs(args: List<String>): Command {
        val options = WeblateCommandOptions()
        var slug: String? = null
        val iterator = args.iterator()
        var help: Command.Help? = null

        while (iterator.hasNext() && help == null) {
            when (val argument = iterator.next()) {
                "--help",
                "-h" -> help = Command.Help(type = HelpType.DELETE)

                "--slug" ->
                    slug =
                        iterator.nextOptionValue()
                            ?: missingValue(argument, HelpType.DELETE)
                                .also { help = it }
                                .let { null }

                else ->
                    parseCommonOption(argument, iterator, options, HelpType.DELETE)?.let {
                        help = it
                    }
            }
        }

        return help
            ?: validateDeleteArgs(slug)
            ?: requireToken(commandName = "delete", options = options, helpType = HelpType.DELETE)
            ?: Command.Delete(slug = slug.orEmpty(), options = options)
    }

    private fun parseOptions(
        args: List<String>,
        options: WeblateCommandOptions,
        helpType: HelpType,
    ): Command.Help? {
        val iterator = args.iterator()
        var help: Command.Help? = null

        while (iterator.hasNext() && help == null) {
            when (val argument = iterator.next()) {
                "--help",
                "-h" -> help = Command.Help(type = helpType)

                else -> parseCommonOption(argument, iterator, options, helpType)?.let { help = it }
            }
        }

        return help
    }

    private fun parseCommonOption(
        argument: String,
        iterator: Iterator<String>,
        options: WeblateCommandOptions,
        helpType: HelpType,
    ): Command.Help? =
        when (argument) {
            "--token" -> {
                options.token =
                    iterator.nextOptionValue() ?: return missingValue(argument, helpType)
                null
            }

            "--apply" -> {
                options.applyChanges = true
                null
            }

            "--log-level" -> {
                val value = iterator.nextOptionValue() ?: return missingValue(argument, helpType)
                options.logLevel =
                    parseLogLevel(value)
                        ?: return Command.Help(
                            type = helpType,
                            message = "Invalid log level: $value",
                        )
                null
            }

            else -> Command.Help(type = helpType, message = "Unknown option: $argument")
        }

    private fun parseHelpArgs(args: List<String>): Command.Help {
        return when (val commandName = args.firstOrNull()) {
            null -> Command.Help(type = HelpType.UNKNOWN)
            "update" -> Command.Help(type = HelpType.UPDATE)
            "create" -> Command.Help(type = HelpType.CREATE)
            "list" -> Command.Help(type = HelpType.LIST)
            "delete" -> Command.Help(type = HelpType.DELETE)
            else -> Command.Help(type = HelpType.UNKNOWN, message = "Unknown command: $commandName")
        }
    }

    private fun requireToken(
        commandName: String,
        options: WeblateCommandOptions,
        helpType: HelpType,
    ): Command.Help? =
        if (options.token == null) {
            Command.Help(
                type = helpType,
                message = "Missing required option for $commandName: --token",
            )
        } else {
            null
        }
}

private fun validateDeleteArgs(slug: String?): Command.Help? =
    if (slug == null) {
        Command.Help(type = HelpType.DELETE, message = "Missing required option for delete: --slug")
    } else {
        null
    }

private fun missingValue(argument: String, helpType: HelpType): Command.Help =
    Command.Help(type = helpType, message = "Missing value for $argument")

private fun parseLogLevel(value: String): LogLevel? =
    LogLevel.entries.firstOrNull { it.name.equals(value, ignoreCase = true) }

private fun Iterator<String>.nextOptionValue(): String? =
    if (hasNext()) {
        next()
    } else {
        null
    }
