package net.thunderbird.cli.l10n.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class L10nToolsConfig(
    val source: SourceConfig = SourceConfig(),
    val import: ImportConfig = ImportConfig(),
    val weblate: WeblateConfig = WeblateConfig(),
)

@Serializable
data class SourceConfig(
    val repository: String = "thunderbird/thunderbird-android",
    val branches: List<String> = listOf("main", "beta", "release"),
    val tmpDir: String = ".tmp",
)

@Serializable
data class ImportConfig(
    val sourceFilePatterns: List<String> = listOf(
        "**/res/values/strings.xml",
        "**/composeResources/values/strings.xml",
        "app-metadata/*/en-US/*.txt",
    ),
    val translatedFilePatterns: List<String> = listOf(
        "**/res/values*/strings.xml",
        "**/composeResources/values*/strings.xml",
        "app-metadata/**/*.txt",
    ),
    val excludedFilePatterns: List<String> = listOf(
        "app-metadata/**/changelogs/**",
    ),
    val cleanupRoots: List<String> = listOf(
        "app-common/",
        "app-k9mail/",
        "app-metadata/",
        "app-thunderbird/",
        "components/",
        "core/",
        "feature/",
        "legacy/",
        "plugins/",
    ),
)

@Serializable
data class WeblateConfig(
    val baseUrl: String = "https://hosted.weblate.org/api/",
    val projectSlug: String = "thunderbird",
    val defaultLinkedComponent: String = "app-common",
    val componentRepo: String = "weblate://thunderbird/thunderbird-android-l10n/app-common",
    val componentConfigFile: String = "./l10n-component-config.json",
    val discovery: ComponentDiscoveryConfig = ComponentDiscoveryConfig(),
)

@Serializable
data class ComponentDiscoveryConfig(
    val androidSourceSuffix: String = "/src/main/res/values/strings.xml",
    val composeSourceSuffix: String = "/src/commonMain/composeResources/values/strings.xml",
    val skippedDirectories: List<String> = listOf(
        ".git",
        ".github",
        ".gradle",
        ".idea",
        ".kotlin",
        ".memory",
        ".tmp",
        "build",
        "docs",
        "gradle",
        "scripts",
        "l10n-tools",
        "tm-weblate",
    ),
    val excludedPaths: List<String> = listOf(
        "app-ui-catalog",
        "ui-catalog",
        "openpgp-api",
        "/test/",
    ),
    val resources: List<ComponentResourceConfig> = listOf(
        ComponentResourceConfig(
            type = ResourceKind.ANDROID,
            fileMaskSuffix = "/src/main/res/values-*/strings.xml",
            templateSuffix = "/src/main/res/values/strings.xml",
            fileFormat = "aresource",
        ),
        ComponentResourceConfig(
            type = ResourceKind.COMPOSE,
            fileMaskSuffix = "/src/commonMain/composeResources/values-*/strings.xml",
            templateSuffix = "/src/commonMain/composeResources/values/strings.xml",
            fileFormat = "cmp-resource",
        ),
    ),
)

@Serializable
data class ComponentResourceConfig(
    val type: ResourceKind,
    val fileMaskSuffix: String,
    val templateSuffix: String,
    val fileFormat: String,
)

@Serializable
enum class ResourceKind {
    @SerialName("android")
    ANDROID,

    @SerialName("compose")
    COMPOSE;

    val suffix: String
        get() = when (this) {
            ANDROID -> "android"
            COMPOSE -> "compose"
        }
}
