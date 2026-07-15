package net.thunderbird.cli.l10n.weblate.command

import net.thunderbird.cli.l10n.config.L10nConfig
import net.thunderbird.cli.l10n.terminal.Terminal
import net.thunderbird.cli.l10n.weblate.api.Component
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig
import net.thunderbird.cli.l10n.weblate.api.ComponentInfo
import net.thunderbird.cli.l10n.weblate.api.WeblateClient
import net.thunderbird.cli.l10n.weblate.project.ComponentInfo as LocalComponentInfo

@Suppress("TooGenericExceptionCaught", "MemberNameEqualsClassName")
class DeleteCommand(
    config: L10nConfig,
    options: WeblateCommandOptions,
    store: Terminal,
    private val slugToDelete: String,
) : BaseCommand(config, options, store) {
    override suspend fun onRun(
        client: WeblateClient,
        defaultComponentConfig: ComponentConfig,
        localComponents: List<LocalComponentInfo>,
    ): DeleteResult {
        store.status("📦 Loading Weblate components...")
        val componentScope = loadComponentsInDefaultCategory(client)
        val component = componentScope.findBySlug(slugToDelete)

        return if (component == null) {
            reportMissingComponent(componentScope)
        } else {
            deleteComponentIfRequested(client = client, component = component)
        }
    }

    private fun reportMissingComponent(componentScope: ComponentScope): DeleteResult {
        store.line("")
        store.error("    Could not find component with slug in category: $slugToDelete")
        store.line("")
        store.line("    Available slugs in category:")
        componentScope.availableSlugs().forEach { store.line("        $it") }
        store.line("")
        store.line(
            "    Ignored ${componentScope.ignoredComponentCount} components outside category."
        )
        return DeleteResult(
            slug = slugToDelete,
            found = false,
            deletionAttempted = false,
            deleted = false,
        )
    }

    private suspend fun deleteComponentIfRequested(
        client: WeblateClient,
        component: Component,
    ): DeleteResult {
        store.line(
            "Found component: ${component.info.name} " +
                "(slug: ${component.info.slug} # ID: ${component.info.id})"
        )

        return when {
            !options.applyChanges -> dryRunResult()
            store.confirm("    Are you sure you want to delete this component?") ->
                DeleteResult(
                    slug = slugToDelete,
                    found = true,
                    deletionAttempted = true,
                    deleted = executeDeleteComponent(client, component.info),
                )
            else -> cancelledResult()
        }
    }

    private fun dryRunResult(): DeleteResult {
        store.line("    Dry run: would delete component")
        return DeleteResult(
            slug = slugToDelete,
            found = true,
            deletionAttempted = false,
            deleted = false,
        )
    }

    private fun cancelledResult(): DeleteResult {
        store.line("    Deletion cancelled.")
        return DeleteResult(
            slug = slugToDelete,
            found = true,
            deletionAttempted = false,
            deleted = false,
        )
    }

    private suspend fun executeDeleteComponent(
        client: WeblateClient,
        info: ComponentInfo,
    ): Boolean {
        return try {
            val success = client.deleteComponent(info.url)
            if (success) {
                store.line("    ✅ Deleted component successfully")
            } else {
                store.error("    Failed to delete component: API request failed")
            }
            success
        } catch (e: Exception) {
            store.error("    Failed to delete component: ${e.message}")
            false
        }
    }
}
