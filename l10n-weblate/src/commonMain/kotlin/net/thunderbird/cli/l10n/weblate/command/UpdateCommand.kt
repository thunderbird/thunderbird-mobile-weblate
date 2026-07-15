package net.thunderbird.cli.l10n.weblate.command

import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.weblate.api.Component
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.ComponentPatch
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.project.ComponentConfigDiff
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo

@Suppress("TooGenericExceptionCaught")
class UpdateCommand(config: L10nConfig, options: WeblateCommandOptions, store: Terminal) :
    BaseCommand(config, options, store) {
    override suspend fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<ComponentInfo>,
    ): UpdateResult {
        store.status("📦 Loading Weblate components...")
        val componentScope = loadComponentsInDefaultCategory(client)
        val allComponents = componentScope.components
        val localSlugs = localComponents.map { it.slug }.toSet()
        val (managed, skipped) = allComponents.partition { it.info.slug in localSlugs }

        store.line(
            componentScope.loadedInCategoryMessage(
                discoveredLocally = managed.size,
                skipped = skipped.size,
            )
        )

        managed.forEach { component ->
            store.line("")
            store.line(
                "- ${component.info.name} (slug: ${component.info.slug} # ID: ${component.info.id}) "
            )
            store.line("")
            processComponent(component, defaultComponentConfig, client)
            store.line("")
        }

        if (skipped.isNotEmpty()) {
            store.line("-------")
            store.line("")
            store.line("Skipped components (not discovered locally):")
            store.line("")
            skipped.forEach { store.line("${it.info.slug} # ID: ${it.info.id}") }
            store.line("")
        }

        return UpdateResult(
            managedComponents = managed.size,
            skippedComponents = skipped.size,
            ignoredOutsideCategory = componentScope.ignoredComponentCount,
        )
    }

    @Suppress("NestedBlockDepth")
    private suspend fun processComponent(
        component: Component,
        componentConfig: ComponentConfig,
        client: WeblateClient,
    ) {
        val diffs =
            ComponentConfigDiff.computeConfigDiff(
                expected = componentConfig,
                actual = component.config,
                indentLevel = 1,
            )

        if (diffs.isEmpty()) {
            store.line("  ✅ Config matches common config")
        } else {
            store.warning("  Config differs:")
            store.line("")
            diffs.forEach { store.line("     $it") }
            if (options.applyChanges) {
                try {
                    val result =
                        client.patchComponent(
                            component.info.url,
                            ComponentPatch(
                                category = component.info.category,
                                linkedComponent = component.info.linkedComponent,
                                config = componentConfig,
                                locked = component.info.locked,
                            ),
                        )
                    if (result) {
                        store.line("    ✅ Updated component config successfully")
                    } else {
                        store.error("    Failed to update component config: API request failed")
                    }
                } catch (e: Exception) {
                    store.error("    Failed to update component config: ${e.message}")
                }
            }
        }
    }
}
