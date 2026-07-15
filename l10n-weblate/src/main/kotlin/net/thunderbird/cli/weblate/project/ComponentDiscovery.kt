package net.thunderbird.cli.weblate.project

import java.io.File
import net.thunderbird.cli.l10n.config.ComponentDiscoveryConfig
import net.thunderbird.cli.l10n.config.ResourceKind

class ComponentDiscovery(
    private val translationsRoot: File,
    private val config: ComponentDiscoveryConfig,
) {
    fun discover(): List<ComponentInfo> {
        val sourceFiles =
            translationsRoot
                .walkTopDown()
                .onEnter { dir -> !isSkippedDirectory(dir) }
                .filter { it.isFile && it.name == STRINGS_FILE_NAME }
                .filter { it.parentFile.name == SOURCE_VALUES_DIRECTORY }
                .toList()

        val moduleTypes =
            sourceFiles
                .mapNotNull { sourceFile -> sourceFile.toComponentSource() }
                .groupBy { it.first }

        return moduleTypes
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
    }

    private fun File.toComponentSource(): Pair<String, ResourceKind>? {
        val relativePath = relativeTo(translationsRoot).invariantSeparatorsPath

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

    private fun isSkippedDirectory(dir: File): Boolean {
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
