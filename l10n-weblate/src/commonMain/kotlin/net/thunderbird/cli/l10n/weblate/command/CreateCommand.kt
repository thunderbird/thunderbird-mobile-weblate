package net.thunderbird.cli.l10n.weblate.command

import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.weblate.api.Component
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.ComponentCreate
import net.thunderbird.cli.l10n.weblate.api.ComponentInfo
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo as LocalComponentInfo
import net.thunderbird.cli.l10n.weblate.project.ResourceFormat

class CreateCommand(config: L10nConfig, options: WeblateCommandOptions, store: Terminal) :
    BaseCommand(config, options, store) {
    override suspend fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<LocalComponentInfo>,
    ): CreateResult {
        store.status("📦 Loading Weblate components...")
        val componentScope = loadComponentsInDefaultCategory(client)
        val allComponents = componentScope.components
        val defaultComponent = componentScope.defaultComponent

        val weblateSlugs = allComponents.map { it.info.slug }.toSet()

        store.line(
            "Found ${localComponents.size} local components with Android or Compose source strings"
        )
        store.line(componentScope.loadedInCategoryMessage())

        val missingInWeblate = localComponents.filter { it.slug !in weblateSlugs }

        store.line("")
        if (missingInWeblate.isNotEmpty()) {
            store.line("Modules missing in Weblate:")
            for (module in missingInWeblate) {
                val shouldContinue =
                    createComponentFromModule(
                        client = client,
                        module = module,
                        defaultComponent = defaultComponent,
                        defaultComponentConfig = defaultComponentConfig,
                    )
                if (!shouldContinue) break
            }
        } else {
            store.line(
                "All local components with resource strings have a corresponding component in Weblate."
            )
        }

        return CreateResult(
            localComponents = localComponents.size,
            missingComponents = missingInWeblate.size,
            ignoredOutsideCategory = componentScope.ignoredComponentCount,
        )
    }

    private suspend fun createComponentFromModule(
        client: WeblateClient,
        module: LocalComponentInfo,
        defaultComponent: Component,
        defaultComponentConfig: ComponentConfig,
    ): Boolean {
        store.line("")
        store.line("  - ${module.path} (type: ${module.type})")
        store.line("    expected name: \"${componentName(module)}\"")
        store.line("    expected slug: \"${module.slug}\"")

        return if (!options.applyChanges) {
            store.line("    (Dry run: would create component)")
            true
        } else {
            val createPayload =
                createComponentPayload(
                    module = module,
                    defaultConfig = defaultComponentConfig,
                    defaultInfo = defaultComponent.info,
                )
            if (store.confirm("    Do you want to create this component?")) {
                store.status("    Creating component...")
                val success = executeCreateComponent(client = client, component = createPayload)
                if (!success) {
                    store.line("    Stopping execution due to failure.")
                }
                success
            } else {
                store.line("    Skipped.")
                true
            }
        }
    }

    private fun createComponentPayload(
        module: LocalComponentInfo,
        defaultConfig: ComponentConfig,
        defaultInfo: ComponentInfo,
    ): ComponentCreate {
        val resource = ResourceFormat.forType(module.type)
        val fileMask = module.path + resource.fileMaskSuffix
        val template = module.path + resource.sourceSuffix

        return ComponentCreate(
            name = componentName(module),
            slug = module.slug,
            project =
                "${config.project.weblate.baseUrl}projects/${config.project.weblate.projectSlug}/",
            fileMask = fileMask,
            template = template,
            fileFormat = resource.fileFormat,
            category = defaultInfo.category,
            linkedComponent = defaultInfo.url,
            repo = config.project.weblate.componentRepo,
            vcs = "github",
            mergeStyle = "merge",
            config = defaultConfig.copy(editTemplate = false),
        )
    }

    private fun componentName(module: LocalComponentInfo): String {
        return module.path.replace(oldValue = "/", newValue = ":")
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun executeCreateComponent(
        client: WeblateClient,
        component: ComponentCreate,
    ): Boolean {
        return try {
            val success = client.createComponent(component)
            if (success) {
                store.line("    ✅ Created component successfully")
            } else {
                store.error("    Failed to create component")
            }
            success
        } catch (e: Exception) {
            store.error("    Error creating component: ${e.message}")
            false
        }
    }
}
