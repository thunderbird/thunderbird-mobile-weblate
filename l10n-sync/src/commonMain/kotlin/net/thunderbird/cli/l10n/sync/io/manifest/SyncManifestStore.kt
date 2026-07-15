package net.thunderbird.cli.l10n.sync.io.manifest

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.json.Json
import net.thunderbird.cli.l10n.config.readText
import net.thunderbird.cli.l10n.config.resolve
import net.thunderbird.cli.l10n.config.writeText
import net.thunderbird.cli.l10n.sync.io.manifest.SyncManifest.Companion.SYNC_MANIFEST_FILE

object SyncManifestStore {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun read(projectRoot: Path): SyncManifest {
        return json.decodeFromString(
            SyncManifest.serializer(),
            projectRoot.resolve(SYNC_MANIFEST_FILE).readText(),
        )
    }

    fun write(projectRoot: Path, manifest: SyncManifest) {
        projectRoot.resolve(SYNC_MANIFEST_FILE).writeText(serialize(manifest))
    }

    fun hasChanged(projectRoot: Path, manifest: SyncManifest): Boolean {
        val manifestFile = projectRoot.resolve(SYNC_MANIFEST_FILE)
        return !SystemFileSystem.exists(manifestFile) ||
            manifestFile.readText() != serialize(manifest)
    }

    private fun serialize(manifest: SyncManifest): String =
        json.encodeToString(SyncManifest.serializer(), manifest) + "\n"
}
