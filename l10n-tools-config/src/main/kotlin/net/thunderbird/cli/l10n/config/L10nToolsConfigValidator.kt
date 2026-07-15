package net.thunderbird.cli.l10n.config

object L10nToolsConfigValidator {
    fun validate(config: L10nToolsConfig) {
        val errors = buildList {
            requireValue("source.repository", config.source.repository)
            requireList("source.branches", config.source.branches)
            requireList("import.sourceFilePatterns", config.import.sourceFilePatterns)
            requireList("import.translatedFilePatterns", config.import.translatedFilePatterns)
            requireValue("weblate.baseUrl", config.weblate.baseUrl)
            requireValue("weblate.projectSlug", config.weblate.projectSlug)
            requireValue("weblate.defaultLinkedComponent", config.weblate.defaultLinkedComponent)
            requireValue("weblate.componentRepo", config.weblate.componentRepo)
            requireList("weblate.discovery.resources", config.weblate.discovery.resources)
        }

        if (errors.isNotEmpty()) {
            error(
                "Invalid l10n tools config:\n" + errors.joinToString(separator = "\n") { "- $it" }
            )
        }
    }

    private fun MutableList<String>.requireValue(name: String, value: String) {
        if (value.isBlank() || value == PLACEHOLDER) {
            add("$name must be configured")
        }
    }

    private fun MutableList<String>.requireList(name: String, value: List<*>) {
        if (value.isEmpty()) {
            add("$name must contain at least one value")
        }
    }

    private const val PLACEHOLDER = "CHANGE-ME"
}
