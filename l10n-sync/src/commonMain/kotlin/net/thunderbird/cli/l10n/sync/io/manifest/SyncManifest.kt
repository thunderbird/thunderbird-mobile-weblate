package net.thunderbird.cli.l10n.sync.io.manifest

import kotlinx.serialization.Serializable

@Serializable
data class SyncManifest(val version: Int, val branches: Map<String, BranchInventory>) {
    companion object {
        const val SYNC_MANIFEST_FILE = "l10n-sync-manifest.json"
    }
}

@Serializable data class BranchInventory(val files: Map<String, List<String>>)
