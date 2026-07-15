package net.thunderbird.cli.l10n.weblate.command

import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo

class ListCommand(config: L10nConfig, options: WeblateCommandOptions, store: Terminal) :
    BaseCommand(config, options, store) {
    override suspend fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<ComponentInfo>,
    ): ListResult {
        store.status("📦 Loading Weblate components...")
        val componentScope = loadComponentsInDefaultCategory(client)
        val weblateComponents = componentScope.components
        val weblateSlugs = weblateComponents.map { it.info.slug }.toSet()
        val localSlugs = localComponents.map { it.slug }.toSet()
        val missingInWeblate = localComponents.filter { it.slug !in weblateSlugs }
        val missingLocally = weblateComponents.filter { it.info.slug !in localSlugs }

        store.line(componentScope.loadedInCategoryMessage())
        store.line("")
        store.line("Weblate components:")
        weblateComponents
            .sortedBy { it.info.slug }
            .forEach { component ->
                val localMarker =
                    if (component.info.slug in localSlugs) {
                        "local"
                    } else {
                        "not discovered locally"
                    }
                store.line(
                    "  - ${component.info.slug} # ID: ${component.info.id} (${component.info.name}, $localMarker)"
                )
            }

        store.line("")
        store.line("Local components: ${localComponents.size}")
        store.line("Missing in Weblate: ${missingInWeblate.size}")
        missingInWeblate
            .sortedBy { it.slug }
            .forEach { component ->
                store.line("  - ${component.slug} (${component.path}, type: ${component.type})")
            }

        store.line("")
        store.line("Weblate components not discovered locally: ${missingLocally.size}")
        missingLocally
            .sortedBy { it.info.slug }
            .forEach { component ->
                store.line("  - ${component.info.slug} # ID: ${component.info.id}")
            }

        return ListResult(
            weblateComponents = weblateComponents.size,
            localComponents = localComponents.size,
            missingComponents = missingInWeblate.size,
            ignoredOutsideCategory = componentScope.ignoredComponentCount,
        )
    }
}
