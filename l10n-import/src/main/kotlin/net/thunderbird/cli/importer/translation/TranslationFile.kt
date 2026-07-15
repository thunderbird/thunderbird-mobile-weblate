package net.thunderbird.cli.importer.translation

import net.thunderbird.cli.importer.git.Branch

data class TranslationFile(val filePath: String, val branch: Branch, val keys: List<TranslationKey>)
