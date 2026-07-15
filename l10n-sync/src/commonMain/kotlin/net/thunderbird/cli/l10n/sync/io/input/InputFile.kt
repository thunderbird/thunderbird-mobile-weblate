package net.thunderbird.cli.l10n.sync.io.input

import net.thunderbird.cli.l10n.config.Branch

data class InputFile(val relativePath: String, val sourceBranch: Branch, val content: String)
