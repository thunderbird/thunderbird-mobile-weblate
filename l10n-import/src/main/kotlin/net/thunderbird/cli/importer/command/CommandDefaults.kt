package net.thunderbird.cli.importer.command

import net.thunderbird.cli.importer.git.Branch
import net.thunderbird.cli.l10n.config.L10nToolsConfig

object CommandDefaults {
    fun apply(command: Command, config: L10nToolsConfig): Command {
        val configuredBranches = config.source.branches.map { Branch(it) }

        return when (command) {
            is Command.Import -> command.copy(
                sourceRepository = command.sourceRepository ?: config.source.repository,
                branches = command.branches ?: configuredBranches,
            )

            is Command.Validate -> command.copy(
                sourceRepository = command.sourceRepository ?: config.source.repository,
                branches = command.branches ?: configuredBranches,
            )

            is Command.Help -> command
        }
    }
}
