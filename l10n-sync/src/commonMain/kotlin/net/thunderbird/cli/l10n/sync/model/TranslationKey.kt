package net.thunderbird.cli.l10n.sync.model

data class TranslationKey(
    override val id: String,
    override val content: String,
    override val comments: List<String>,
) : L10nKey
