package net.thunderbird.cli.weblate.command

import java.io.File
import net.thunderbird.cli.weblate.CliConfig
import net.thunderbird.cli.weblate.ComponentConfigLoader
import net.thunderbird.cli.weblate.api.ComponentConfig
import net.thunderbird.cli.weblate.api.WeblateClient
import net.thunderbird.cli.weblate.project.ComponentDiscovery
import net.thunderbird.cli.weblate.project.ComponentInfo

@Suppress("TooGenericExceptionCaught")
abstract class BaseCommand(internal val config: CliConfig) {
    fun run() {
        val defaultComponentConfig = loadComponentConfig(resolveComponentConfigFile())
        val localComponents =
            ComponentDiscovery(
                    translationsRoot = config.projectRoot,
                    config = config.toolsConfig.weblate.discovery,
                )
                .discover()

        val token = config.token ?: error("Weblate API token is required for this command")
        val client =
            WeblateClient(token = token, config = config.apiConfig, logLevel = config.logLevel)

        onRun(client, defaultComponentConfig, localComponents)
    }

    abstract fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<ComponentInfo>,
    )

    private fun resolveComponentConfigFile(): File {
        val configuredPath = config.componentConfigFile ?: DEFAULT_COMPONENT_CONFIG_FILE
        val configuredFile = configuredPath.toProjectFile()
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

    private companion object {
        const val DEFAULT_COMPONENT_CONFIG_FILE = "./l10n-component-config.json"
    }
}
