package net.thunderbird.cli.l10n.weblate.project

import kotlinx.io.files.Path
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ResourceKind
import net.thunderbird.cli.l10n.config.isInModule
import net.thunderbird.cli.l10n.config.listRegularFilesRecursively
import net.thunderbird.cli.l10n.config.relativeTo

class ComponentDiscovery(
    private val translationsRoot: Path,
    private val config: ComponentDiscoveryConfig,
    private val ignoredModules: List<String> = emptyList(),
) {
    fun discover(): List<ComponentInfo> {
        val sourceFiles =
            translationsRoot
                .listRegularFilesRecursively(skipDirectory = ::isSkippedDirectory)
                .filter { it.name == STRINGS_FILE_NAME }
                .filter { it.parent?.name == SOURCE_VALUES_DIRECTORY }

        val moduleTypes =
            sourceFiles
                .mapNotNull { sourceFile -> sourceFile.toComponentSource() }
                .groupBy { it.first }

        val components =
            moduleTypes
                .filterKeys { modulePath -> ignoredModules.none { modulePath.isInModule(it) } }
                .flatMap { (modulePath, types) ->
                    val resourceTypes = types.map { it.second }.toSet()
                    resourceTypes.map { type ->
                        ComponentInfo(
                            slug = componentSlug(modulePath, type, resourceTypes.size > 1),
                            path = modulePath,
                            type = type,
                        )
                    }
                }
                .sortedWith(compareBy<ComponentInfo> { it.path }.thenBy { it.type.name })

        val duplicateSlugs = components.groupBy(ComponentInfo::slug).filterValues { it.size > 1 }
        require(duplicateSlugs.isEmpty()) {
            "Duplicate component slugs: " +
                duplicateSlugs.entries.joinToString { (slug, duplicates) ->
                    "$slug (${duplicates.joinToString { it.path }})"
                }
        }
        return components
    }

    private fun Path.toComponentSource(): Pair<String, ResourceKind>? {
        val relativePath = relativeTo(translationsRoot)

        return config.resources.firstNotNullOfOrNull { type ->
            relativePath.toComponentSource(ResourceFormat.forType(type))
        }
    }

    private fun String.toComponentSource(resource: ResourceFormat): Pair<String, ResourceKind>? {
        return if (endsWith(resource.sourceSuffix)) {
            substringBefore(resource.sourceSuffix) to resource.type
        } else {
            null
        }
    }

    private fun componentSlug(
        modulePath: String,
        type: ResourceKind,
        includeTypeSuffix: Boolean,
    ): String {
        val baseSlug = modulePath.replace("/", "-")
        return if (includeTypeSuffix) "$baseSlug-${type.suffix}" else baseSlug
    }

    private fun isSkippedDirectory(dir: Path): Boolean {
        return dir != translationsRoot && dir.name in SKIPPED_DIRECTORIES
    }

    private companion object {
        private const val STRINGS_FILE_NAME = "strings.xml"
        private const val SOURCE_VALUES_DIRECTORY = "values"
        val SKIPPED_DIRECTORIES =
            setOf(
                ".git",
                ".github",
                ".gradle",
                ".idea",
                ".kotlin",
                ".tmp",
                "build",
                "docs",
                "gradle",
                "scripts",
                "weblate",
            )
    }
}
