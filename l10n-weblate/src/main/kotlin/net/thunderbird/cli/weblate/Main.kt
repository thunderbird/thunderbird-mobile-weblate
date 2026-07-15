package net.thunderbird.cli.weblate

import java.io.File
import kotlin.system.exitProcess
import net.thunderbird.cli.l10n.config.L10nToolsConfigLoader
import net.thunderbird.cli.l10n.config.L10nToolsConfigValidator
import net.thunderbird.cli.weblate.api.WeblateConfig
import net.thunderbird.cli.weblate.command.Command
import net.thunderbird.cli.weblate.command.CommandParser
import net.thunderbird.cli.weblate.command.CreateComponent
import net.thunderbird.cli.weblate.command.DeleteComponent
import net.thunderbird.cli.weblate.command.UpdateComponent

fun main(args: Array<String>) {
    try {
        val parsed = CommandParser.parse(args)
        when (val command = parsed.command) {
            is Command.Help -> printHelp(command.message)
            Command.Update -> UpdateComponent(loadCliConfig(parsed.options)).run()
            Command.Create -> CreateComponent(loadCliConfig(parsed.options)).run()
            is Command.Delete -> DeleteComponent(loadCliConfig(parsed.options), command.slug).run()
        }
    } catch (e: IllegalArgumentException) {
        System.err.println(e.message ?: "Invalid Weblate command")
        exitProcess(1)
    } catch (e: IllegalStateException) {
        System.err.println(e.message ?: "Weblate command failed")
        exitProcess(1)
    }
}

private fun loadCliConfig(options: CliOptions): CliConfig {
    val projectRoot = File(".").canonicalFile
    val toolsConfig =
        L10nToolsConfigLoader().load(projectRoot, options.configFile).also {
            L10nToolsConfigValidator.validate(it)
        }

    return CliConfig(
        token = options.token,
        componentConfigFile = options.componentConfigFile,
        projectRoot = projectRoot,
        toolsConfig = toolsConfig,
        apiConfig =
            WeblateConfig(
                baseUrl = toolsConfig.weblate.baseUrl,
                projectName = toolsConfig.weblate.projectSlug,
                defaultComponent = toolsConfig.weblate.defaultLinkedComponent,
            ),
        dryRun = options.dryRun,
        logLevel = options.logLevel,
    )
}

private fun printHelp(message: String?) {
    if (message != null) {
        println(message)
        println()
    }

    println(
        """
        Usage: weblate [options] <command>

        Commands:
          update    Update locally discovered components
          create    Create missing components
          delete    Delete a component from Weblate
          help      Show this help message

        Options:
          --token                    Weblate API token
          --component-config-file    Path to component config JSON
          --config                   Project config file (default: l10n-tools.json)
          --dry-run                  Run without making changes
          --log-level                Weblate API client log level
          --help, -h                 Show this help message

        Delete options:
          --slug                     Slug of the component to delete
        """
            .trimIndent()
    )
}
