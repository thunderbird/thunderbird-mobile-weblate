package net.thunderbird.cli.weblate

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import io.ktor.client.plugins.logging.LogLevel
import java.io.File
import net.thunderbird.cli.l10n.config.L10nToolsConfigLoader
import net.thunderbird.cli.weblate.api.WeblateConfig

@Suppress("TooGenericExceptionCaught")
class WeblateCli : CliktCommand(
    name = "weblate",
) {
    internal val token: String? by option(
        help = "Weblate API token",
    )

    internal val componentConfigFile: String? by option(
        "--component-config-file",
        help = "Path to component config JSON",
    )

    internal val configFile: String? by option(
        "--config",
        help = "Project config file (default: l10n-tools.json)",
    )

    internal val dryRun: Boolean by option(
        help = "Dry run the command without making any changes",
    ).flag()

    internal val logLevel: LogLevel by option(
        "--log-level",
        help = "Log level for the Weblate API client",
    ).enum<LogLevel>(ignoreCase = true).default(LogLevel.NONE)

    override fun help(context: Context): String = "Weblate CLI"

    override fun run() {
        currentContext.findOrSetObject {
            val projectRoot = File(".").canonicalFile
            val toolsConfig = L10nToolsConfigLoader().load(projectRoot, configFile)
            CliConfig(
                token = token,
                componentConfigFile = componentConfigFile,
                projectRoot = projectRoot,
                toolsConfig = toolsConfig,
                apiConfig = WeblateConfig(
                    baseUrl = toolsConfig.weblate.baseUrl,
                    projectName = toolsConfig.weblate.projectSlug,
                    defaultComponent = toolsConfig.weblate.defaultLinkedComponent,
                ),
                dryRun = dryRun,
                logLevel = logLevel,
            )
        }
    }
}
