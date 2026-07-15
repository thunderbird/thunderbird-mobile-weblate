package net.thunderbird.cli.l10n.config

import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L10nProjectConfig(
    val source: SourceConfig,
    val import: ImportConfig,
    val weblate: WeblateConfig,
    val ignoredModules: List<String> = emptyList(),
)

@Serializable data class SourceConfig(val repository: Repository, val branches: List<Branch>)

@Serializable @JvmInline value class Repository(val url: String)

@Serializable
@JvmInline
value class Branch(val value: String) {
    init {
        require(value.isNotBlank()) { "Branch name cannot be blank" }
    }
}

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
