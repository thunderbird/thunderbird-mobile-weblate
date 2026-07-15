package net.thunderbird.cli.l10n.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L10nToolsConfig(
    val source: SourceConfig,
    val import: ImportConfig,
    val weblate: WeblateConfig,
)

@Serializable data class SourceConfig(val repository: String, val branches: List<String>)

@Serializable
data class ImportConfig(
    val sourceFilePatterns: List<String>,
    val translatedFilePatterns: List<String>,
    val excludedPaths: List<String>,
)

@Serializable
data class WeblateConfig(
    val baseUrl: String,
    val projectSlug: String,
    val defaultLinkedComponent: String,
    val componentRepo: String,
    val discovery: ComponentDiscoveryConfig,
)

@Serializable data class ComponentDiscoveryConfig(val resources: List<ResourceKind>)

@Serializable
enum class ResourceKind {
    @SerialName("android") ANDROID,
    @SerialName("compose") COMPOSE;

    val suffix: String
        get() =
            when (this) {
                ANDROID -> "android"
                COMPOSE -> "compose"
            }
}
