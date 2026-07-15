package net.thunderbird.cli.l10n.sync.model

sealed interface L10nKey {
    val id: String
    val content: String
    val comments: List<String>
}
