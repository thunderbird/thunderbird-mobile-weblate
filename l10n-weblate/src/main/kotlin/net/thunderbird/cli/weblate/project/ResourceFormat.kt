package net.thunderbird.cli.weblate.project

import net.thunderbird.cli.l10n.config.ResourceKind

data class ResourceFormat(
    val type: ResourceKind,
    val sourceSuffix: String,
    val fileMaskSuffix: String,
    val fileFormat: String,
) {
    companion object {
        fun forType(type: ResourceKind): ResourceFormat {
            return when (type) {
                ResourceKind.ANDROID ->
                    ResourceFormat(
                        type = type,
                        sourceSuffix = "/src/main/res/values/strings.xml",
                        fileMaskSuffix = "/src/main/res/values-*/strings.xml",
                        fileFormat = "aresource",
                    )

                ResourceKind.COMPOSE ->
                    ResourceFormat(
                        type = type,
                        sourceSuffix = "/src/commonMain/composeResources/values/strings.xml",
                        fileMaskSuffix = "/src/commonMain/composeResources/values-*/strings.xml",
                        fileFormat = "cmp-resource",
                    )
            }
        }
    }
}
