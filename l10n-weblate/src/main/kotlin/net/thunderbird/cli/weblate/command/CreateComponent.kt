package net.thunderbird.cli.weblate.command

import net.thunderbird.cli.weblate.CliConfig
import net.thunderbird.cli.weblate.api.Component
import net.thunderbird.cli.weblate.api.ComponentConfig
import net.thunderbird.cli.weblate.api.ComponentCreate
import net.thunderbird.cli.weblate.api.ComponentInfo
import net.thunderbird.cli.weblate.api.WeblateClient
import net.thunderbird.cli.weblate.project.ComponentInfo as LocalComponentInfo
import net.thunderbird.cli.weblate.project.ResourceFormat

class CreateComponent(config: CliConfig) : BaseCommand(config) {
    override fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<LocalComponentInfo>,
    ) {
        val allComponents = client.loadComponents()
        val defaultComponent = allComponents.find {
            it.info.slug == config.toolsConfig.weblate.defaultLinkedComponent
        }
        if (defaultComponent == null) {
            println(
                "    ❌ Could not find default component: ${config.toolsConfig.weblate.defaultLinkedComponent}"
            )
            return
        }

        val weblateSlugs = allComponents.map { it.info.slug }.toSet()

        println(
            "Found ${localComponents.size} local components with Android or Compose source strings"
        )

        val missingInWeblate = localComponents.filter { it.slug !in weblateSlugs }

        println()
        if (missingInWeblate.isNotEmpty()) {
            println("Modules missing in Weblate:")
            missingInWeblate.forEach {
                createComponentFromModule(
                    client = client,
                    module = it,
                    defaultComponent = defaultComponent,
                    defaultComponentConfig = defaultComponentConfig,
                )
            }
        } else {
            println(
                "All local components with resource strings have a corresponding component in Weblate."
            )
        }
    }

    private fun createComponentFromModule(
        client: WeblateClient,
        module: LocalComponentInfo,
        defaultComponent: Component,
        defaultComponentConfig: ComponentConfig,
    ) {
        println()
        println("  - ${module.path} (type: ${module.type})")
        println("    expected name: \"${componentName(module)}\"")
        println("    expected slug: \"${module.slug}\"")

        if (config.dryRun) {
            println("    (Dry run: would create component)")
        } else {
            val createPayload =
                createComponentPayload(
                    module = module,
                    defaultConfig = defaultComponentConfig,
                    defaultInfo = defaultComponent.info,
                )
            if (confirm("    Do you want to create this component?")) {
                println("    Creating component...")
                val success = executeCreateComponent(client = client, component = createPayload)
                if (!success) {
                    println("    Stopping execution due to failure.")
                    return
                }
            } else {
                println("    Skipped.")
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
            project = "${config.apiConfig.baseUrl}projects/${config.apiConfig.projectName}/",
            fileMask = fileMask,
            template = template,
            fileFormat = resource.fileFormat,
            category = defaultInfo.category,
            linkedComponent = defaultInfo.url,
            repo = config.toolsConfig.weblate.componentRepo,
            vcs = "github",
            mergeStyle = "merge",
            config = defaultConfig.copy(editTemplate = false),
        )
    }

    private fun componentName(module: LocalComponentInfo): String {
        return module.path.replace(oldValue = "/", newValue = ":")
    }

    @Suppress("TooGenericExceptionCaught")
    private fun executeCreateComponent(client: WeblateClient, component: ComponentCreate): Boolean {
        return try {
            val success = client.createComponent(component)
            if (success) {
                println("    ✅ Created component successfully")
            } else {
                println("    ❌ Failed to create component")
            }
            success
        } catch (e: Exception) {
            println("    ❌ Error creating component: ${e.message}")
            false
        }
    }
}
