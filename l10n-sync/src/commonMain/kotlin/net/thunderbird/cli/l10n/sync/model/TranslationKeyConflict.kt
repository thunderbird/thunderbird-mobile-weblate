package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

data class TranslationKeyConflict(
    val filePath: String,
    val keyId: String,
    val selected: TranslationKeySource,
    val conflicting: TranslationKeySource,
)

data class TranslationKeySource(val branch: Branch, val content: String)
