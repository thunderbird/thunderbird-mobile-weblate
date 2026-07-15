package net.thunderbird.cli.l10n.weblate.project

import kotlinx.io.files.Path
import kotlinx.serialization.json.Json
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.weblate.api.ComponentConfig

class ComponentConfigLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(file: Path): ComponentConfig {
        val text = file.readText()
        return json.decodeFromString(ComponentConfig.serializer(), text)
    }
}
