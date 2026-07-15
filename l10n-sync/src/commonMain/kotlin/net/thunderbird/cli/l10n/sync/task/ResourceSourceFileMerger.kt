package net.thunderbird.cli.l10n.sync.task

import net.thunderbird.cli.l10n.config.Branch
import net.thunderbird.cli.l10n.sync.model.ResourceKeyConflict
import net.thunderbird.cli.l10n.sync.model.ResourceKeyResolution
import net.thunderbird.cli.l10n.sync.model.ResourceKeySource
import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.SourceResourceFile

class ResourceSourceFileMerger {
    fun merge(
        relativePath: String,
        files: List<SourceResourceFile>,
        detectConflicts: Boolean = true,
    ): ResourceSourceFileMergeResult? {
        val keysById = linkedMapOf<String, SelectedSourceKey>()
        val conflicts = mutableListOf<ResourceKeyConflict>()
        val presentBranches = linkedSetOf<Branch>()
        val sourcesByKey = linkedMapOf<String, MutableList<ResourceKeySource>>()

        files.forEach { file ->
            presentBranches += file.branch
            file.keys.forEach { candidate ->
                sourcesByKey.getOrPut(candidate.id) { mutableListOf() } +=
                    ResourceKeySource(
                        branch = file.branch,
                        sourceFile = relativePath,
                        content = candidate.content,
                    )
                val existing = keysById[candidate.id]
                if (existing == null) {
                    keysById[candidate.id] =
                        SelectedSourceKey(key = candidate, branch = file.branch)
                } else if (detectConflicts && existing.key.content != candidate.content) {
                    conflicts +=
                        ResourceKeyConflict(
                            filePath = relativePath,
                            key = candidate.id,
                            selectedBranch = existing.branch,
                            conflictingBranch = file.branch,
                            selectedContent = existing.key.content,
                            conflictingContent = candidate.content,
                        )
                }
            }
        }

        if (keysById.isEmpty()) return null

        val mergedFile =
            SourceResourceFile(
                relativePath = relativePath,
                branch = presentBranches.first(),
                type = files.first().type,
                keys = keysById.values.map { sourceKey -> sourceKey.key }.sortedBy { key -> key.id },
            )

        return ResourceSourceFileMergeResult(
            file = mergedFile,
            conflicts = conflicts,
            presentBranches = presentBranches,
            keyResolutions =
                keysById.values
                    .sortedBy { sourceKey -> sourceKey.key.id }
                    .map { sourceKey ->
                        ResourceKeyResolution(
                            key = sourceKey.key.id,
                            selectedBranch = sourceKey.branch,
                            selectedSourceFile = relativePath,
                            availableIn = sourcesByKey[sourceKey.key.id].orEmpty(),
                        )
                    },
        )
    }

    private data class SelectedSourceKey(val key: SourceKey, val branch: Branch)
}

data class ResourceSourceFileMergeResult(
    val file: SourceResourceFile,
    val conflicts: List<ResourceKeyConflict>,
    val presentBranches: Set<Branch>,
    val keyResolutions: List<ResourceKeyResolution>,
)
