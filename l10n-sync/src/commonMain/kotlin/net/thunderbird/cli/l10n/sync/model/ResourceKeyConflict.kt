package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

data class ResourceKeyResolution(
    val key: String,
    val selectedBranch: Branch,
    val selectedSourceFile: String,
    val availableIn: List<ResourceKeySource>,
)

data class ResourceKeySource(val branch: Branch, val sourceFile: String, val content: String)

data class ResourceKeyConflict(
    val filePath: String,
    val key: String,
    val selectedBranch: Branch,
    val conflictingBranch: Branch,
    val selectedContent: String,
    val conflictingContent: String,
)
