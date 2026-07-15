package net.thunderbird.cli.importer.translation

import java.io.File

class TranslationKeyConsolidator {

    private val parser = TranslationKeyParser()
    private val conflicts = mutableListOf<KeyConflict>()

    fun consolidateKeysForFile(
        branches: List<String>,
        relativeFilePath: String,
        branchRootDirs: Map<String, File>,
    ): ConsolidatedTranslationFile? {
        if (!relativeFilePath.endsWith(".xml")) {
            return null
        }

        val consolidatedKeys = linkedMapOf<String, String>()
        val keyOrigins = mutableMapOf<String, BranchContent>()
        val presentBranches = mutableSetOf<String>()

        for (branch in branches) {
            val branchRoot = branchRootDirs[branch] ?: continue
            val filePath = File(branchRoot, relativeFilePath)

            if (!filePath.exists()) continue

            val branchKeys = parser.parseStringsXml(filePath)
            presentBranches.add(branch)

            for ((keyId, content) in branchKeys) {
                val existingContent = consolidatedKeys[keyId]
                if (existingContent != null && existingContent != content) {
                    recordConflict(
                        keyId = keyId,
                        filePath = relativeFilePath,
                        existing = keyOrigins[keyId] ?: BranchContent(branch, existingContent),
                        conflicting = BranchContent(branch, content),
                    )
                } else {
                    consolidatedKeys[keyId] = content
                    keyOrigins.putIfAbsent(keyId, BranchContent(branch, content))
                }
            }
        }

        if (consolidatedKeys.isEmpty()) return null

        return ConsolidatedTranslationFile(
            filePath = relativeFilePath,
            keys = consolidatedKeys,
            presentInBranches = presentBranches,
        )
    }

    private fun recordConflict(
        keyId: String,
        filePath: String,
        existing: BranchContent,
        conflicting: BranchContent,
    ) {
        val existingConflict = conflicts.find { it.id == keyId && it.filePath == filePath }
        val entries = existingConflict?.conflicts?.toMutableList() ?: mutableListOf(existing)

        if (existingConflict != null && entries.none { it.branch == existing.branch && it.content == existing.content }) {
            entries.add(0, existing)
        }
        if (entries.none { it.branch == conflicting.branch && it.content == conflicting.content }) {
            entries += conflicting
        }

        val conflict = KeyConflict(keyId, filePath, entries)
        conflicts.removeAll { it.id == keyId && it.filePath == filePath }
        conflicts.add(conflict)
    }

    fun getConflicts(): List<KeyConflict> = conflicts.toList()

    fun consolidateAllFiles(
        branches: List<String>,
        allFiles: Set<String>,
        branchRootDirs: Map<String, File>,
    ): ConsolidationResult {
        val resolvedFiles = mutableListOf<ConsolidatedTranslationFile>()
        for (relativeFile in allFiles.sorted()) {
            consolidateKeysForFile(branches, relativeFile, branchRootDirs)?.let {
                resolvedFiles += it
            }
        }

        return ConsolidationResult(
            filesConsolidated = resolvedFiles.size,
            filesWithConflicts = conflicts.map { it.filePath }.toSet().size,
            conflicts = conflicts.toList(),
            files = resolvedFiles,
        )
    }
}

data class ConsolidatedTranslationFile(
    val filePath: String,
    val keys: Map<String, String>,
    val presentInBranches: Set<String>,
)

data class ConsolidationResult(
    val filesConsolidated: Int,
    val filesWithConflicts: Int,
    val conflicts: List<KeyConflict>,
    val files: List<ConsolidatedTranslationFile>,
)
