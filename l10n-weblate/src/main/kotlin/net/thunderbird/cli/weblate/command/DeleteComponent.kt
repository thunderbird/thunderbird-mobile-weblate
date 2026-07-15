package net.thunderbird.cli.weblate.command

import net.thunderbird.cli.weblate.CliConfig
import net.thunderbird.cli.weblate.api.ComponentConfig
import net.thunderbird.cli.weblate.api.ComponentInfo
import net.thunderbird.cli.weblate.api.WeblateClient
import net.thunderbird.cli.weblate.project.ComponentInfo as LocalComponentInfo

@Suppress("TooGenericExceptionCaught", "MemberNameEqualsClassName")
class DeleteComponent(config: CliConfig, private val slugToDelete: String) : BaseCommand(config) {
    override fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<LocalComponentInfo>,
    ) {
        val components = client.loadComponents()
        val component = components.find { it.info.slug == slugToDelete }

        if (component == null) {
            println()
            println("    ❌ Could not find component with slug: $slugToDelete")
            println()
            println("    Available slugs:")
            components.forEach { println("        ${it.info.slug}") }
            return
        }

        println(
            "Found component: ${component.info.name} (slug: ${component.info.slug} # ID: ${component.info.id})"
        )

        if (config.dryRun) {
            println("    Dry run: would delete component")
        } else {
            if (confirm("    Are you sure you want to delete this component?")) {
                executeDeleteComponent(client, component.info)
            } else {
                println("    Deletion cancelled.")
            }
        }
    }

    private fun executeDeleteComponent(client: WeblateClient, info: ComponentInfo) {
        try {
            val success = client.deleteComponent(info.url)
            if (success) {
                println("    ✅ Deleted component successfully")
            } else {
                println("    ❌ Failed to delete component: API request failed")
            }
        } catch (e: Exception) {
            println("    ❌ Failed to delete component: ${e.message}")
        }
    }
}
