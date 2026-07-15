package net.thunderbird.cli.l10n.weblate.command

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.weblate.api.Component
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.DefaultWeblateClient
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.api.WeblateConfig
import net.thunderbird.cli.l10n.weblate.project.ComponentConfigLoader
import net.thunderbird.cli.l10n.weblate.project.ComponentDiscovery
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo

@Suppress("TooGenericExceptionCaught")
abstract class BaseCommand(
    internal val config: L10nConfig,
    internal val options: WeblateCommandOptions,
    internal val store: Terminal,
) {
    suspend fun run(): WeblateCommandResult {
        store.status("📄 Loading component config...")
        val defaultComponentConfig = loadComponentConfig()
        store.status("🔎 Discovering local components...")
        val localComponents =
            ComponentDiscovery(
                    translationsRoot = config.projectRoot,
                    config = config.project.weblate.discovery,
                    ignoredModules = config.project.ignoredModules,
                )
                .discover()

        val token = options.token ?: error("Weblate API token is required for this command")
        store.status("🌐 Connecting to Weblate...")
        val client =
            DefaultWeblateClient(token = token, config = apiConfig(), logLevel = options.logLevel)

        return onRun(client, defaultComponentConfig, localComponents)
    }

    abstract suspend fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<ComponentInfo>,
    ): WeblateCommandResult

    internal suspend fun loadComponentsInDefaultCategory(client: WeblateClient): ComponentScope {
        val allComponents = client.loadComponents()
        val defaultComponent =
            allComponents.find { it.info.slug == config.project.weblate.defaultLinkedComponent }
                ?: error(
                    "Could not find default component: ${config.project.weblate.defaultLinkedComponent}"
                )

        val category = defaultComponent.info.category
        val componentsInCategory = allComponents.filter { it.info.category == category }

        return ComponentScope(
            defaultComponent = defaultComponent,
            components = componentsInCategory,
            ignoredComponentCount = allComponents.size - componentsInCategory.size,
        )
    }

    private fun loadComponentConfig(): ComponentConfig {
        val file = Path(config.projectRoot.toString(), DEFAULT_COMPONENT_CONFIG_FILE)
        if (!SystemFileSystem.exists(file)) {
            error("Component config file not found: $file")
        }

        return try {
            ComponentConfigLoader().load(file)
        } catch (e: Exception) {
            error("Failed to load component config: ${e.message}")
        }
    }

    private fun apiConfig(): WeblateConfig =
        WeblateConfig(
            baseUrl = config.project.weblate.baseUrl,
            projectName = config.project.weblate.projectSlug,
            defaultComponent = config.project.weblate.defaultLinkedComponent,
        )

    private companion object {
        const val DEFAULT_COMPONENT_CONFIG_FILE = "l10n-component-config.json"
    }
}

data class ComponentScope(
    val defaultComponent: Component,
    val components: List<Component>,
    val ignoredComponentCount: Int,
) {
    fun findBySlug(slug: String): Component? = components.find { it.info.slug == slug }

    fun availableSlugs(): List<String> = components.map { it.info.slug }

    fun loadedInCategoryMessage(): String =
        "Loaded ${components.size} Weblate components in category ($ignoredComponentCount outside category)"

    fun loadedInCategoryMessage(discoveredLocally: Int, skipped: Int): String =
        "Loaded ${components.size} components in category ($discoveredLocally discovered locally, $skipped skipped, $ignoredComponentCount outside category):"
}
