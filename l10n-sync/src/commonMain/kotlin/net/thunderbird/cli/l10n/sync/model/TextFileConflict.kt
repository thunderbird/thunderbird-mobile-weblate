package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

data class TextFileConflict(
    val filePath: String,
    val selectedBranch: Branch,
    val sources: List<TextFileSource>,
)

data class TextFileSource(val branch: Branch, val content: String)
