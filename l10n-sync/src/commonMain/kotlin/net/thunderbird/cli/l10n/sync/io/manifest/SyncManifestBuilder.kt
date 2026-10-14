package net.thunderbird.cli.l10n.sync.io.manifest

import net.thunderbird.cli.l10n.sync.io.input.FileInputReport

internal const val MANIFEST_VERSION = 1

object SyncManifestBuilder {
    fun build(reports: List<FileInputReport>): SyncManifest {
        val branchFiles = linkedMapOf<String, MutableMap<String, MutableList<String>>>()

        reports
            .sortedBy { report -> report.path }
            .forEach { report ->
                if (report.keyResolutions.isEmpty() && report.path.isBranchOwnedFile()) {
                    report.presentBranches.forEach { branch ->
                        branchFiles
                            .getOrPut(branch.value) { linkedMapOf() }
                            .getOrPut(report.path) { mutableListOf() }
                    }
                }
                report.keyResolutions
                    .sortedBy { provenance -> provenance.key }
                    .forEach { provenance ->
                        provenance.availableIn.forEach { source ->
                            branchFiles
                                .getOrPut(source.branch.value) { linkedMapOf() }
                                .getOrPut(report.path) { mutableListOf() } += provenance.key
                        }
                    }
            }

        return SyncManifest(
            version = MANIFEST_VERSION,
            branches =
                branchFiles.mapValues { (_, files) ->
                    BranchInventory(files = files.mapValues { (_, keys) -> keys.sorted() })
                },
        )
    }

    private fun String.isBranchOwnedFile(): Boolean =
        !startsWith("app-metadata/") || "/en-US/" in this
}
