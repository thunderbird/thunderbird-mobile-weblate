package net.thunderbird.cli.l10n

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands

internal class L10nValidationCli : CliktCommand(name = "l10n-validation") {
    override fun help(context: Context) =
        "Validate localization resources and release-branch compatibility."

    init {
        subcommands(ValidateResourceChanges(), CheckBranchCompatibility())
    }

    override fun run() = Unit
}
