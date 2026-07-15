package net.thunderbird.cli.importer.workflow

import net.thunderbird.cli.importer.git.Branch

data class ResolvedRepositoryFile(
    val relativePath: String,
    val sourceBranch: Branch,
    val content: String,
)
