package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

sealed interface TranslationFile : L10nFile

data class TranslationResourceFile(
    override val relativePath: String,
    override val branch: Branch,
    override val type: L10nFileType,
    override val keys: List<TranslationKey>,
) : TranslationFile, L10nResourceFile

data class TranslationTextFile(
    override val relativePath: String,
    override val branch: Branch,
    override val type: L10nFileType,
    override val content: String,
) : TranslationFile, L10nTextFile
