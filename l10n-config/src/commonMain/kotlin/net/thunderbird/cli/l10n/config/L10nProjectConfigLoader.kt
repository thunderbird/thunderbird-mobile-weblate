package net.thunderbird.cli.l10n.config

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private const val CONFIG_FILE = "l10n-config.json"

internal object L10nProjectConfigLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun load(projectRoot: Path, configPath: String? = null): L10nProjectConfig {
        val file = Path(projectRoot.toString(), configPath ?: CONFIG_FILE)

        if (!SystemFileSystem.exists(file)) {
            error("Configuration file not found: $file")
        }

        val config =
            try {
                json.decodeFromString(L10nProjectConfig.serializer(), readText(file))
            } catch (e: SerializationException) {
                error("Failed to load configuration file $file: ${e.message}")
            }

        L10nProjectConfigValidator.validate(config)
        return config
    }

    private fun readText(file: Path): String {
        val source = SystemFileSystem.source(file).buffered()
        return try {
            source.readString()
        } finally {
            source.close()
        }
    }
}
