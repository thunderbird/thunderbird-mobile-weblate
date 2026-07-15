package net.thunderbird.cli.l10n.sync.io.input

import net.thunderbird.cli.l10n.sync.model.SourceKey
import net.thunderbird.cli.l10n.sync.model.TranslationKey

interface ResourceParser {
    fun parseSource(content: String, relativePath: String): List<SourceKey>

    fun parseTranslation(content: String, relativePath: String): List<TranslationKey>
}
