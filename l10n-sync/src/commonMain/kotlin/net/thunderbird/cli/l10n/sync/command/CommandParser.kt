package net.thunderbird.cli.l10n.sync.command

import net.thunderbird.cli.l10n.sync.command.Command.Help.HelpType

object CommandParser {
    fun parse(args: Array<String>): Command {
        return when (args.firstOrNull()) {
            "import" -> parseImportArgs(args.drop(1))
            "export" -> parseExportArgs(args.drop(1))
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

    private fun parseImportArgs(args: List<String>): Command {
        var fullImport = false
        var applyChanges = false
        var help: Command.Help? = null

        for (flag in args.takeWhile { help == null }) {
            when (flag) {
                "--all" -> fullImport = true
                "--apply" -> applyChanges = true
                "--help",
                "-h" -> help = Command.Help(type = HelpType.IMPORT)
                else ->
                    help = Command.Help(type = HelpType.IMPORT, message = "Unknown option: $flag")
            }
        }

        return help ?: Command.Import(fullImport = fullImport, applyChanges = applyChanges)
    }

    private fun parseExportArgs(args: List<String>): Command {
        var branch: String? = null
        var l10nRepo: String? = null
        var applyChanges = false
        var index = 0
        var help: Command.Help? = null

        while (index < args.size && help == null) {
            when (val option = args[index]) {
                "--branch" -> {
                    branch = args.getOrNull(index + 1)
                    if (branch == null) {
                        help =
                            Command.Help(
                                type = HelpType.EXPORT,
                                message = "Missing value for --branch",
                            )
                    }
                    index += 2
                }
                "--l10n-repo" -> {
                    l10nRepo = args.getOrNull(index + 1)
                    if (l10nRepo == null) {
                        help =
                            Command.Help(
                                type = HelpType.EXPORT,
                                message = "Missing value for --l10n-repo",
                            )
                    }
                    index += 2
                }
                "--apply" -> {
                    applyChanges = true
                    index++
                }
                "--help",
                "-h" -> help = Command.Help(type = HelpType.EXPORT)
                else ->
                    help = Command.Help(type = HelpType.EXPORT, message = "Unknown option: $option")
            }
        }

        return help
            ?: validateExportArgs(branch = branch, l10nRepo = l10nRepo)
            ?: Command.Export(
                branch = branch.orEmpty(),
                l10nRepo = l10nRepo.orEmpty(),
                applyChanges = applyChanges,
            )
    }

    private fun validateExportArgs(branch: String?, l10nRepo: String?): Command.Help? =
        when {
            branch.isNullOrBlank() ->
                Command.Help(type = HelpType.EXPORT, message = "Missing required option: --branch")
            l10nRepo.isNullOrBlank() ->
                Command.Help(
                    type = HelpType.EXPORT,
                    message = "Missing required option: --l10n-repo",
                )
            else -> null
        }
}
