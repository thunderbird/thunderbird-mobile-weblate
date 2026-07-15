package net.thunderbird.cli.importer.command

import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.importer.command.Command.Help.HelpType

object CommandParser {
    fun parse(args: Array<String>): Command {
        return when (args.firstOrNull()) {
            "import" -> parseImportArgs(args.drop(1))
            "validate" -> parseValidateArgs(args.drop(1))
            "help", "--help", "-h" -> Command.Help(type = HelpType.UNKNOWN)
            else -> Command.Help(
                type = HelpType.UNKNOWN,
                message = if (args.isNotEmpty()) "Unknown command: ${args.first()}" else null,
            )
        }
    }

    private fun parseImportArgs(args: List<String>): Command {
        var all = false
        var sourceRepository: String? = null
        var branches: List<Branch>? = null
        var configFile: String? = null

        val iter = args.iterator()
        while (iter.hasNext()) {
            when (val flag = iter.next()) {
                "--all" -> all = true
                "--source-repo" -> sourceRepository = iter.nextValueOrNull() ?: return Command.Help(
                    type = HelpType.IMPORT,
                    message = "Missing value for $flag",
                )
                "--branches" -> branches = parseBranches(
                    iter.nextValueOrNull() ?: return Command.Help(
                        type = HelpType.IMPORT,
                        message = "Missing value for $flag",
                    ),
                )
                "--config" -> configFile = iter.nextValueOrNull() ?: return Command.Help(
                    type = HelpType.IMPORT,
                    message = "Missing value for $flag",
                )
                "--help", "-h" -> return Command.Help(type = HelpType.IMPORT)
                else -> return Command.Help(type = HelpType.IMPORT, message = "Unknown option: $flag")
            }
        }

        return Command.Import(
            all = all,
            sourceRepository = sourceRepository,
            branches = branches,
            configFile = configFile,
        )
    }

    private fun parseValidateArgs(args: List<String>): Command {
        var sourceRepository: String? = null
        var outputDir = "."
        var branches: List<Branch>? = null
        var configFile: String? = null

        val iter = args.iterator()
        while (iter.hasNext()) {
            when (val flag = iter.next()) {
                "--source-repo" -> sourceRepository = iter.nextValueOrNull() ?: return Command.Help(
                    type = HelpType.VALIDATE,
                    message = "Missing value for $flag",
                )
                "--output-dir" -> outputDir = iter.nextValueOrNull() ?: return Command.Help(
                    type = HelpType.VALIDATE,
                    message = "Missing value for $flag",
                )
                "--branches" -> branches = parseBranches(
                    iter.nextValueOrNull() ?: return Command.Help(
                        type = HelpType.VALIDATE,
                        message = "Missing value for $flag",
                    ),
                )
                "--config" -> configFile = iter.nextValueOrNull() ?: return Command.Help(
                    type = HelpType.VALIDATE,
                    message = "Missing value for $flag",
                )
                "--help", "-h" -> return Command.Help(type = HelpType.VALIDATE)
                else -> return Command.Help(type = HelpType.VALIDATE, message = "Unknown option: $flag")
            }
        }

        return Command.Validate(
            sourceRepository = sourceRepository,
            outputDir = outputDir,
            branches = branches,
            configFile = configFile,
        )
    }

    private fun parseBranches(raw: String): List<Branch> {
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map { Branch(it) }
    }

    private fun Iterator<String>.nextValueOrNull(): String? = if (hasNext()) next() else null
}
