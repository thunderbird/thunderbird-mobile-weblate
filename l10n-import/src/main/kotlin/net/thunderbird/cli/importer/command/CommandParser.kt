package net.thunderbird.cli.importer.command

import net.thunderbird.cli.importer.command.Command.Help.HelpType

object CommandParser {
    fun parse(args: Array<String>): Command {
        return when (args.firstOrNull()) {
            "import" -> parseImportArgs(args.drop(1))
            "validate" -> parseValidateArgs(args.drop(1))
            "help",
            "--help",
            "-h" -> Command.Help(type = HelpType.UNKNOWN)
            else ->
                Command.Help(
                    type = HelpType.UNKNOWN,
                    message = if (args.isNotEmpty()) "Unknown command: ${args.first()}" else null,
                )
        }
    }

    fun isFullImport(args: Array<String>): Boolean {
        return args.firstOrNull() == "import" && args.drop(1).contains("--all")
    }

    private fun parseImportArgs(args: List<String>): Command {
        for (flag in args) {
            when (flag) {
                "--all" -> Unit
                "--help",
                "-h" -> return Command.Help(type = HelpType.IMPORT)
                else ->
                    return Command.Help(type = HelpType.IMPORT, message = "Unknown option: $flag")
            }
        }

        return Command.Import
    }

    private fun parseValidateArgs(args: List<String>): Command {
        for (flag in args) {
            when (flag) {
                "--help",
                "-h" -> return Command.Help(type = HelpType.VALIDATE)
                else ->
                    return Command.Help(type = HelpType.VALIDATE, message = "Unknown option: $flag")
            }
        }

        return Command.Validate
    }
}
