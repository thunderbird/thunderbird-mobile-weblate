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
            error("Configuration file not found: ${file.path}")
        }

        return try {
            json.decodeFromString(L10nToolsConfig.serializer(), file.readText(Charsets.UTF_8))
        } catch (e: Exception) {
            error("Failed to load configuration file ${file.path}: ${e.message}")
        }
    }

    companion object {
        const val DEFAULT_CONFIG_FILE = "l10n-tools.json"
    }
}
