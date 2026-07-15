package net.thunderbird.cli.l10n.sync.model

import net.thunderbird.cli.l10n.config.Branch

sealed interface L10nFile {
    val relativePath: String
    val branch: Branch

    val type: L10nFileType
}

sealed interface L10nResourceFile : L10nFile {
    val keys: List<L10nKey>
}

sealed interface L10nTextFile : L10nFile {
    val content: String
}
