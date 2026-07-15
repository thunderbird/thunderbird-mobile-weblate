package net.thunderbird.cli.l10n.weblate.command

import io.ktor.client.plugins.logging.LogLevel

data class WeblateCommandOptions(
    var token: String? = null,
    var applyChanges: Boolean = false,
    var logLevel: LogLevel = LogLevel.NONE,
)
