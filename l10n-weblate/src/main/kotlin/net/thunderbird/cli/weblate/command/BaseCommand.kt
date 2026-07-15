package net.thunderbird.cli.weblate.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import java.io.File
import net.thunderbird.cli.weblate.CliConfig
import net.thunderbird.cli.weblate.ComponentConfigLoader
import net.thunderbird.cli.weblate.api.ComponentConfig
import net.thunderbird.cli.weblate.api.WeblateClient
import net.thunderbird.cli.weblate.project.ComponentDiscovery
import net.thunderbird.cli.weblate.project.ComponentInfo

@Suppress("TooGenericExceptionCaught")
abstract class BaseCommand(name: String) : CliktCommand(name = name) {

    internal val config by requireObject<CliConfig>()

    override fun run() {
        val defaultComponentConfig = loadComponentConfig(resolveComponentConfigFile())
        val localComponents = ComponentDiscovery(
            translationsRoot = config.projectRoot,
            config = config.toolsConfig.weblate.discovery,
        ).discover()

        val token = config.token ?: error("Weblate API token is required for this command")
        val client = WeblateClient(token = token, config = config.apiConfig, logLevel = config.logLevel)

        onRun(client, defaultComponentConfig, localComponents)
    }

    abstract fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<ComponentInfo>,
    )

    private fun resolveComponentConfigFile(): File {
        val configuredPath = config.componentConfigFile ?: config.toolsConfig.weblate.componentConfigFile
        val configuredFile = configuredPath.toProjectFile()
        if (configuredFile.exists()) return configuredFile

        val fallbackPath = System.getenv("L10N_TOOLS_DEFAULT_COMPONENT_CONFIG")
        if (!fallbackPath.isNullOrBlank()) {
            val fallbackFile = File(fallbackPath)
            if (fallbackFile.exists()) return fallbackFile
        }

        return configuredFile
    }

    private fun loadComponentConfig(file: File): ComponentConfig {
        if (!file.exists()) {
            error("Component config file not found: ${file.path}")
        }

        return try {
            ComponentConfigLoader().load(file)
        } catch (e: Exception) {
            error("Failed to load component config: ${e.message}")
        }
    }

    private fun String.toProjectFile(): File {
        val file = File(this)
        return if (file.isAbsolute) file else File(config.projectRoot, this)
    }
}
