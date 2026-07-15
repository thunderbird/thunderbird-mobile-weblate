package net.thunderbird.cli.weblate

import io.ktor.client.plugins.logging.LogLevel
import java.io.File
import net.thunderbird.cli.l10n.config.L10nToolsConfig
import net.thunderbird.cli.weblate.api.WeblateConfig

data class CliConfig(
    val token: String?,
    val componentConfigFile: String?,
    val projectRoot: File,
    val toolsConfig: L10nToolsConfig,
    val apiConfig: WeblateConfig,
    val dryRun: Boolean,
    val logLevel: LogLevel,
)
