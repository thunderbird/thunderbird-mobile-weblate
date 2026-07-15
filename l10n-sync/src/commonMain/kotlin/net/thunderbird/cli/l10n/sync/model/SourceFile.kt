package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

sealed interface SourceFile : L10nFile

data class SourceResourceFile(
    override val relativePath: String,
    override val branch: Branch,
    override val type: L10nFileType,
    override val keys: List<SourceKey>,
) : SourceFile, L10nResourceFile

data class SourceTextFile(
    override val relativePath: String,
    override val branch: Branch,
    override val type: L10nFileType,
    override val content: String,
) : SourceFile, L10nTextFile
