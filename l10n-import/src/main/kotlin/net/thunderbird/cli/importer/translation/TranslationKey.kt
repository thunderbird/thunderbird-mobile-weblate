package net.thunderbird.cli.importer.translation

import net.thunderbird.cli.importer.git.Branch

data class TranslationKey(
    val id: String,
    val content: String,
    val branch: Branch,
    val filePath: String,
)

data class ConsolidatedKey(
    val id: String,
    val content: String,
    val sourceFile: String,
    val presentInBranches: Set<String>,
)

data class BranchContent(val branch: String, val content: String)

data class KeyConflict(val id: String, val filePath: String, val conflicts: List<BranchContent>)
