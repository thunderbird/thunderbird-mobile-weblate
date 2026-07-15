package net.thunderbird.cli.l10n.config

import java.io.File
import kotlinx.serialization.json.Json

class L10nToolsConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(projectRoot: File, configPath: String?): L10nToolsConfig {
        val path = configPath ?: DEFAULT_CONFIG_FILE
        val file = File(path).let { if (it.isAbsolute) it else File(projectRoot, path) }

        if (!file.exists()) {
            return L10nToolsConfig()
        }

        return json.decodeFromString(L10nToolsConfig.serializer(), file.readText(Charsets.UTF_8))
    }

    companion object {
        const val DEFAULT_CONFIG_FILE = "l10n-tools.json"
    }
}
